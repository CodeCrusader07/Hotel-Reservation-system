# Hotel Reservation System

A console-based hotel reservation system written in Java. It lets a user search for available rooms, make reservations, view and cancel bookings, and browse all rooms of the Taj Hotel from a simple text menu.

## Features

- Search available rooms by date range and room type
- Make a reservation with customer details and a payment method
- Automatic total calculation based on the number of nights
- Card (simulated) and cash payment options
- Unique booking IDs (for example HTL-1A2B3C4D)
- View all bookings with their status
- Cancel a confirmed booking by its ID
- View the full list of hotel rooms and prices
- Overlap detection so a room cannot be double-booked
- Input validation for dates, numbers, empty fields and card digits
## How to Run

1. Save the source file as HotelReservationSystem.java.
2. Compile it:
   javac HotelReservationSystem.java
3. Run it:
   java HotelReservationSystem

## How to Make a Reservation

1. Choose option `2` from the main menu.
2. Enter the check-in and check-out dates.
3. Select a room type.
4. Pick a room number from the list of available rooms.
5. Enter the customer name, phone number and email address.
6. Review the booking summary and choose a payment method:
   - Card (simulation): enter the cardholder name and only the last 4 digits of the card.
   - Cash: pay at the hotel.
7. Confirm with `y` to receive a booking confirmation and booking ID.

## Sample Session


Check-in date (DD/MM/YYYY): 10/12/2026
Check-out date (DD/MM/YYYY): 13/12/2026
Room types: 1. SINGLE  2. DOUBLE  3. DELUXE  4. SUITE
Choose room type: 2

Room No.     Type         Price/Night
103          DOUBLE       INR 2500.00
104          DOUBLE       INR 2500.00

## Project Structure

All classes are in a single file for easy compilation.

 Class                    -> Purpose                                                        
 HotelReservationSystem -> Entry point, menu handling and user input                      
 Hotel                  -> Stores rooms and bookings, checks availability, cancels bookingsf 
 Room                   -> Room number, type and price per night                          
 Customer               -> Customer name, phone and email                                 
 Booking                -> A reservation with ID, dates, payment, amount and status       
 Payment                -> Abstract base class for payment methods                        
 CardPayment            -> Simulated card payment (stores only the last 4 digits)         
 CashPayment            -> Pay-at-hotel option                                            
 DateUtil               -> DD/MM/YYYY date formatting and parsing                         
 RoomType               -> SINGLE, DOUBLE, DELUXE, SUITE                    
 BookingStatus          -> CONFIRMED, CANCELLED                                 

## Notes and Limitations

- Data is stored in memory only. All bookings are lost when the program exits.
- Card payment is a simulation. No real payment is processed, and the full card number is never requested or stored.
- Rooms are pre-loaded in the `seedRooms()` method. Edit it to change rooms or prices.
- Cancelling a booking frees the room for those dates again, and no refund logic is included.
