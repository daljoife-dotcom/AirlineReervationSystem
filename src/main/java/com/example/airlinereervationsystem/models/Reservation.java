package com.example.airlinereervationsystem.models;

import java.time.LocalDateTime;

/**
 * Represents a flight Reservation entity in the Airline Reservation System.
 * This class acts as the core relational bridge connecting a specific customer
 * to a specific flight, tracking booking timestamps and reservation statuses.
 * <p>
 * <b>OOP Concepts Demonstrated:</b>
 * <ul>
 * <li><b>Encapsulation:</b> Enforces strict data hiding using {@code private} fields,
 * regulating data access and mutations through public getters and setters.</li>
 * <li><b>Entity Association:</b> Demonstrates multi-entity linkage by combining
 * references to both {@link Customer} (via customerId) and {@link Flight} (via flightId).</li>
 * </ul>
 *
 * @author Donia Ahmed
 * @version 2.0 (Sprint 2)
 */
public class Reservation {

    // تطبيق مبدأ الكبسلة (Encapsulation) لحماية حقول الحجز من التلاعب الخارجي المباشر
    private int reservationId;
    private int customerId;      // مفهوم الاقتران (Association): يربط الحجز بالمسافر صاحب الطلب
    private int flightId;        // مفهوم الاقتران (Association): يربط الحجز بالرحلة الجوية المختارة
    private LocalDateTime bookingDate; // توثيق تاريخ ووقت إتمام عملية الحجز بدقة
    private String status;       // حالة الحجز الحالية (مثال: Pending, Confirmed, Canceled)

    /**
     * Constructs a new Reservation record with complete transactional mapping.
     *
     * @param reservationId the unique tracking identifier for the reservation in the database
     * @param customerId    the unique key identifier of the passenger making the booking
     * @param flightId      the unique key identifier of the reserved flight
     * @param bookingDate   the explicit timestamp capturing when the reservation was created
     * @param status        the initial operational status of the booking
     */
    public Reservation(int reservationId, int customerId, int flightId, LocalDateTime bookingDate, String status) {
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.flightId = flightId;
        this.bookingDate = bookingDate;
        this.status = status;
    }

    // ==========================================
    // دوال الكبسلة العامة (Public Getters & Setters)
    // للوصول الآمن والمنظم لبيانات وحالات حجوزات الطيران
    // ==========================================

    /**
     * Gets the unique reservation identifier.
     * @return the integer reservation ID
     */
    public int getReservationId() { return reservationId; }

    /**
     * Sets the unique reservation identifier.
     * @param reservationId the new reservation ID to set
     */
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    /**
     * Gets the associated customer identifier.
     * @return the integer customer ID
     */
    public int getCustomerId() { return customerId; }

    /**
     * Sets the associated customer identifier.
     * @param customerId the new customer ID to bind to this booking
     */
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    /**
     * Gets the associated flight identifier.
     * @return the integer flight ID
     */
    public int getFlightId() { return flightId; }

    /**
     * Sets the associated flight identifier.
     * @param flightId the new flight ID to bind to this booking
     */
    public void setFlightId(int flightId) { this.flightId = flightId; }

    /**
     * Gets the timestamp when the booking was registered.
     * @return the LocalDateTime object representing booking date
     */
    public LocalDateTime getBookingDate() { return bookingDate; }

    /**
     * Sets the timestamp when the booking was registered.
     * @param bookingDate the new LocalDateTime timestamp to set
     */
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }

    /**
     * Gets the current life-cycle status of the reservation.
     * @return the status string
     */
    public String getStatus() { return status; }

    /**
     * Sets the life-cycle status of the reservation.
     * @param status the new operational status string to set
     */
    public void setStatus(String status) { this.status = status; }
}