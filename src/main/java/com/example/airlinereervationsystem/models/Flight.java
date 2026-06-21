package com.example.airlinereervationsystem.models;

import java.time.LocalDateTime;

/**
 * Represents a Flight entity in the Airline Reservation System.
 * This class handles flight details, scheduling, pricing, and available seating.
 * <p>
 * <b>OOP Concepts Demonstrated:</b>
 * <ul>
 * <li><b>Encapsulation:</b> All attributes are marked as {@code private} to prevent
 * direct external manipulation and ensure data validity through controlled access.</li>
 * <li><b>Entity Association:</b> Demonstrates a relational connection to the {@link Aircraft}
 * class using {@code aircraftId} as a foreign key reference.</li>
 * </ul>
 *
 * @author Donia Ahmed
 * @version 2.0 (Sprint 2)
 */
public class Flight {

    // تطبيق مبدأ الكبسلة (Encapsulation) بحماية خصائص الرحلة بجعلها private
    private int flightId;
    private String flightNumber; // رقم الرحلة المميز (مثال: MS777)
    private String destination;   // الوجهة المقصودة للرحلة
    private LocalDateTime flightDate; // استخدام حزمة java.time الحديثة لتوثيق تاريخ ووقت الرحلة بدقة
    private double price;         // سعر تذكرة الرحلة
    private int aircraftId;      // مفهوم الاقتران (Association): يمثل معرف الطائرة المخصصة لهذه الرحلة
    private int availableSeats;   // عدد المقاعد المتاحة المتبقية على متن الرحلة

    /**
     * Constructs a new Flight instance with comprehensive scheduling and capacity data.
     *
     * @param flightId       the unique identifier for the flight in the database
     * @param flightNumber   the alphanumeric designated flight number
     * @param destination    the target airport or city destination
     * @param flightDate     the scheduled departure date and time
     * @param price          the cost of a standard ticket for this flight
     * @param aircraftId     the foreign key ID linking to the assigned aircraft
     * @param availableSeats the initial or remaining number of bookable seats
     */
    public Flight(int flightId, String flightNumber, String destination, LocalDateTime flightDate, double price, int aircraftId, int availableSeats) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.destination = destination;
        this.flightDate = flightDate;
        this.price = price;
        this.aircraftId = aircraftId;
        this.availableSeats = availableSeats;
    }

    // ==========================================
    // دوال الكبسلة العامة (Public Getters & Setters)
    // للتحكم الآمن في قراءة وتعديل بيانات الرحلة الجوية
    // ==========================================

    /**
     * Gets the unique flight ID.
     * @return the integer flight ID
     */
    public int getFlightId() { return flightId; }

    /**
     * Sets the unique flight ID.
     * @param flightId the new flight ID to set
     */
    public void setFlightId(int flightId) { this.flightId = flightId; }

    /**
     * Gets the alphanumeric flight number.
     * @return the flight number string
     */
    public String getFlightNumber() { return flightNumber; }

    /**
     * Sets the alphanumeric flight number.
     * @param flightNumber the new flight number to set
     */
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    /**
     * Gets the flight destination.
     * @return the destination string
     */
    public String getDestination() { return destination; }

    /**
     * Sets the flight destination.
     * @param destination the new destination to set
     */
    public void setDestination(String destination) { this.destination = destination; }

    /**
     * Gets the scheduled flight date and time.
     * @return the LocalDateTime object of the flight
     */
    public LocalDateTime getFlightDate() { return flightDate; }

    /**
     * Sets the scheduled flight date and time.
     * @param flightDate the new LocalDateTime to set
     */
    public void setFlightDate(LocalDateTime flightDate) { this.flightDate = flightDate; }

    /**
     * Gets the ticket price for the flight.
     * @return the double value of price
     */
    public double getPrice() { return price; }

    /**
     * Sets the ticket price for the flight.
     * @param price the new ticket price to set
     */
    public void setPrice(double price) { this.price = price; }

    /**
     * Gets the linked aircraft ID associated with this flight.
     * @return the integer aircraft ID
     */
    public int getAircraftId() { return aircraftId; }

    /**
     * Sets the linked aircraft ID associated with this flight.
     * @param aircraftId the new aircraft ID to set
     */
    public void setAircraftId(int aircraftId) { this.aircraftId = aircraftId; }

    /**
     * Gets the number of currently available seats.
     * @return the remaining available seats count
     */
    public int getAvailableSeats() { return availableSeats; }

    /**
     * Sets the number of available seats.
     * @param availableSeats the new available seats count to set
     */
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
}