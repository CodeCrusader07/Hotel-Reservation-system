import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class HotelReservationSystem {
    private final Scanner scanner = new Scanner(System.in);
    private final Hotel hotel = new Hotel("Taj Hotel");

    public static void main(String[] args) {
        new HotelReservationSystem().start();
    }

    private void start() {
        seedRooms();
        System.out.println("       " + hotel.getName());

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ", 0, 5);
            switch (choice) {
                case 1: searchRooms(); break;
                case 2: makeReservation(); break;
                case 3: viewBookings(); break;
                case 4: cancelBooking(); break;
                case 5: hotel.displayAllRooms(); break;
                case 0:
                    running = false;
                    System.out.println("Thank you for using " + hotel.getName() + "!");
                    break;
                default: break;
            }
        }
        scanner.close();
    }

    private void printMenu() {
        System.out.println("\nMAIN MENU");
        System.out.println("1. Search available rooms");
        System.out.println("2. Make a reservation");
        System.out.println("3. View all bookings");
        System.out.println("4. Cancel a booking");
        System.out.println("5. View all rooms");
        System.out.println("0. Exit");
    }

    private void seedRooms() {
        hotel.addRoom(new Room(101, RoomType.SINGLE, new BigDecimal("1500.00")));
        hotel.addRoom(new Room(102, RoomType.SINGLE, new BigDecimal("1500.00")));
        hotel.addRoom(new Room(103, RoomType.DOUBLE, new BigDecimal("2500.00")));
        hotel.addRoom(new Room(104, RoomType.DOUBLE, new BigDecimal("2500.00")));
        hotel.addRoom(new Room(201, RoomType.DELUXE, new BigDecimal("4000.00")));
        hotel.addRoom(new Room(202, RoomType.DELUXE, new BigDecimal("4000.00")));
        hotel.addRoom(new Room(301, RoomType.SUITE, new BigDecimal("6500.00")));
    }

    private void searchRooms() {
        System.out.println("\nSEARCH AVAILABLE ROOMS");
        LocalDate[] dates = readDates();
        RoomType type = readRoomType();
        List<Room> available = hotel.searchAvailableRooms(dates[0], dates[1], type);
        displayRooms(available);
    }

    private void makeReservation() {
        System.out.println("\nNEW RESERVATION");
        LocalDate[] dates = readDates();
        RoomType type = readRoomType();
        List<Room> available = hotel.searchAvailableRooms(dates[0], dates[1], type);

        if (available.isEmpty()) {
            System.out.println("Sorry, no rooms of this type are available for those dates.");
            return;
        }

        System.out.println("\nAvailable rooms:");
        displayRooms(available);
        int roomNumber = readInt("Enter the room number you want to book: ", 1, 9999);
        Room room = hotel.findRoom(roomNumber);

        if (room == null || room.getType() != type ||
                !hotel.isRoomAvailable(room, dates[0], dates[1])) {
            System.out.println("That room is not available. Please search again.");
            return;
        }

        System.out.println("\nCUSTOMER DETAILS");
        String name = readNonEmpty("Customer name: ");
        String phone = readNonEmpty("Phone number: ");
        String email = readNonEmpty("Email address: ");
        Customer customer = new Customer(name, phone, email);

        long nights = dates[1].toEpochDay() - dates[0].toEpochDay();
        BigDecimal total = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        System.out.println("\n--- BOOKING SUMMARY ---");
        System.out.println("Room: " + room.getRoomNumber() + " (" + room.getType() + ")");
        System.out.println("Stay: " + DateUtil.format(dates[0]) + " to " + DateUtil.format(dates[1]) + " (" + nights + " nights)");
        System.out.println("Price per night: " + money(room.getPricePerNight()));
        System.out.println("Total amount: " + money(total));

        System.out.println("\nPayment method:");
        System.out.println("1. Card (simulation)");
        System.out.println("2. Cash (pay at hotel)");
        int paymentChoice = readInt("Choose payment method: ", 1, 2);
        Payment payment;

        if (paymentChoice == 1) {
            String cardholder = readNonEmpty("Cardholder name: ");
            String lastFour = readDigits("Enter last 4 digits of card (do not enter full card number): ", 4);
            payment = new CardPayment(cardholder, lastFour);
        } else {
            payment = new CashPayment();
        }

        if (!readYesNo("Confirm reservation? (y/n): ")) {
            System.out.println("Reservation was not created.");
            return;
        }

        if (!hotel.isRoomAvailable(room, dates[0], dates[1])) {
            System.out.println("Sorry, this room has just been booked. Please try another room.");
            return;
        }

        if (!payment.process(total)) {
            System.out.println("Payment was not successful. Booking cancelled.");
            return;
        }

        Booking booking = hotel.createBooking(customer, room, dates[0], dates[1], payment, total);
        printConfirmation(booking);
    }

    private void viewBookings() {
        System.out.println("\nALL BOOKINGS");
        List<Booking> bookings = hotel.getBookings();
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        for (Booking booking : bookings) {
            System.out.println(booking);
        }
    }

    private void cancelBooking() {
        System.out.println("\nCANCEL BOOKING");
        String id = readNonEmpty("Enter booking ID: ");
        if (hotel.cancelBooking(id)) {
            System.out.println("Booking " + id + " has been cancelled.");
        } else {
            System.out.println("Booking not found or it has already been cancelled.");
        }
    }

    private void printConfirmation(Booking booking) {
        System.out.println("       BOOKING CONFIRMED");
        System.out.println("Booking ID : " + booking.getBookingId());
        System.out.println("Customer   : " + booking.getCustomer().getName());
        System.out.println("Phone      : " + booking.getCustomer().getPhone());
        System.out.println("Room       : " + booking.getRoom().getRoomNumber()
                + " (" + booking.getRoom().getType() + ")");
        System.out.println("Check-in   : " + DateUtil.format(booking.getCheckIn()));
        System.out.println("Check-out  : " + DateUtil.format(booking.getCheckOut()));
        System.out.println("Amount     : " + money(booking.getTotalAmount()));
        System.out.println("Payment    : " + booking.getPayment().getMethodName());
        System.out.println("Status     : " + booking.getStatus());
        System.out.println("Please keep your booking ID for reference.");
    }

    private void displayRooms(List<Room> rooms) {
        if (rooms.isEmpty()) {
            System.out.println("No matching rooms available.");
            return;
        }
        System.out.printf("%-12s %-12s %-18s%n", "Room No.", "Type", "Price/Night");
        for (Room room : rooms) {
            System.out.printf("%-12d %-12s %-18s%n",
                    room.getRoomNumber(), room.getType(), money(room.getPricePerNight()));
        }
    }

    private LocalDate[] readDates() {
        while (true) {
            LocalDate checkIn = readDate("Check-in date (DD/MM/YYYY): ");
            LocalDate checkOut = readDate("Check-out date (DD/MM/YYYY): ");
            if (checkIn.isBefore(LocalDate.now())) {
                System.out.println("Check-in cannot be in the past.");
            } else if (!checkOut.isAfter(checkIn)) {
                System.out.println("Check-out must be after check-in.");
            } else {
                return new LocalDate[] { checkIn, checkOut };
            }
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalDate.parse(scanner.nextLine().trim(), DateUtil.FORMAT);
            } catch (DateTimeParseException ex) {
                System.out.println("Invalid date. Please use DD/MM/YYYY.");
            }
        }
    }

    private RoomType readRoomType() {
        while (true) {
            System.out.println("Room types: 1. SINGLE  2. DOUBLE  3. DELUXE  4. SUITE");
            int choice = readInt("Choose room type: ", 1, 4);
            switch (choice) {
                case 1: return RoomType.SINGLE;
                case 2: return RoomType.DOUBLE;
                case 3: return RoomType.DELUXE;
                case 4: return RoomType.SUITE;
                default: break;
            }
        }
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Please enter a number from " + min + " to " + max + ".");
        }
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be empty.");
        }
    }

    private String readDigits(String prompt, int length) {
        while (true) {
            String value = readNonEmpty(prompt);
            if (value.matches("\\d{" + length + "}")) return value;
            System.out.println("Please enter exactly " + length + " digits.");
        }
    }

    private boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (value.equalsIgnoreCase("y")) return true;
            if (value.equalsIgnoreCase("n")) return false;
            System.out.println("Please enter y or n.");
        }
    }

    private String money(BigDecimal amount) {
        return "INR " + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

class DateUtil {
    static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    static String format(LocalDate date) {
        return date.format(FORMAT);
    }
}

enum RoomType {
    SINGLE, DOUBLE, DELUXE, SUITE
}

enum BookingStatus {
    CONFIRMED, CANCELLED
}

class Room {
    private final int roomNumber;
    private final RoomType type;
    private final BigDecimal pricePerNight;

    public Room(int roomNumber, RoomType type, BigDecimal pricePerNight) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
    }

    public int getRoomNumber() { return roomNumber; }
    public RoomType getType() { return type; }
    public BigDecimal getPricePerNight() { return pricePerNight; }
}

class Customer {
    private final String name;
    private final String phone;
    private final String email;

    public Customer(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
}

abstract class Payment {
    public abstract boolean process(BigDecimal amount);
    public abstract String getMethodName();
}

class CardPayment extends Payment {
    private final String cardholder;
    private final String lastFourDigits;

    public CardPayment(String cardholder, String lastFourDigits) {
        this.cardholder = cardholder;
        this.lastFourDigits = lastFourDigits;
    }

    @Override
    public boolean process(BigDecimal amount) {
        System.out.println("Processing simulated card payment for " + cardholder
                + " (card ending " + lastFourDigits + ")...");
        System.out.println("Payment simulation successful: INR " + amount.toPlainString());
        return true;
    }

    @Override
    public String getMethodName() {
        return "Card ending " + lastFourDigits + " (simulated)";
    }
}

class CashPayment extends Payment {
    @Override
    public boolean process(BigDecimal amount) {
        System.out.println("Cash payment selected. Please pay INR "
                + amount.toPlainString() + " at the hotel.");
        return true;
    }

    @Override
    public String getMethodName() {
        return "Cash at hotel";
    }
}

class Booking {
    private final String bookingId;
    private final Customer customer;
    private final Room room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final Payment payment;
    private final BigDecimal totalAmount;
    private BookingStatus status;

    public Booking(Customer customer, Room room, LocalDate checkIn, LocalDate checkOut,
                   Payment payment, BigDecimal totalAmount) {
        this.bookingId = "HTL-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();
        this.customer = customer;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.payment = payment;
        this.totalAmount = totalAmount;
        this.status = BookingStatus.CONFIRMED;
    }

    public String getBookingId() { return bookingId; }
    public Customer getCustomer() { return customer; }
    public Room getRoom() { return room; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public Payment getPayment() { return payment; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BookingStatus getStatus() { return status; }
    public void cancel() { status = BookingStatus.CANCELLED; }

    @Override
    public String toString() {
        return "ID: " + bookingId + " | Customer: " + customer.getName()
                + " | Room: " + room.getRoomNumber() + " (" + room.getType() + ")"
                + " | " + DateUtil.format(checkIn) + " to " + DateUtil.format(checkOut)
                + " | Amount: INR " + totalAmount.toPlainString()
                + " | Status: " + status;
    }
}

class Hotel {
    private final String name;
    private final List<Room> rooms = new ArrayList<Room>();
    private final List<Booking> bookings = new ArrayList<Booking>();

    public Hotel(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public Room findRoom(int roomNumber) {
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber) return room;
        }
        return null;
    }

    public List<Room> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut,
                                           RoomType type) {
        List<Room> result = new ArrayList<Room>();
        for (Room room : rooms) {
            if (room.getType() == type && isRoomAvailable(room, checkIn, checkOut)) {
                result.add(room);
            }
        }
        return result;
    }

    public boolean isRoomAvailable(Room room, LocalDate checkIn, LocalDate checkOut) {
        for (Booking booking : bookings) {
            if (booking.getRoom().getRoomNumber() == room.getRoomNumber()
                    && booking.getStatus() == BookingStatus.CONFIRMED) {
                boolean overlaps = checkIn.isBefore(booking.getCheckOut())
                        && booking.getCheckIn().isBefore(checkOut);
                if (overlaps) return false;
            }
        }
        return true;
    }

    public Booking createBooking(Customer customer, Room room, LocalDate checkIn,
                                 LocalDate checkOut, Payment payment, BigDecimal total) {
        Booking booking = new Booking(customer, room, checkIn, checkOut, payment, total);
        bookings.add(booking);
        return booking;
    }

    public List<Booking> getBookings() {
        return new ArrayList<Booking>(bookings);
    }

    public boolean cancelBooking(String bookingId) {
        for (Booking booking : bookings) {
            if (booking.getBookingId().equalsIgnoreCase(bookingId)
                    && booking.getStatus() == BookingStatus.CONFIRMED) {
                booking.cancel();
                return true;
            }
        }
        return false;
    }

    public void displayAllRooms() {
        System.out.println("\nHOTEL ROOMS");
        for (Room room : rooms) {
            System.out.println("Room " + room.getRoomNumber() + " | " + room.getType()
                    + " | INR " + room.getPricePerNight().toPlainString() + " per night");
        }
    }
}
