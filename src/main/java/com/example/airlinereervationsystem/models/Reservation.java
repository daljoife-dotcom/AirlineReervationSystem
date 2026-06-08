package com.example.airlinereervationsystem.models;
import java.time.LocalDateTime;

public class Reservation {
    private int reservationId;
    private int customerId;
    private int flightId;
    private LocalDateTime bookingDate;
    private String status;

    public Reservation(int reservationId, int customerId, int flightId, LocalDateTime bookingDate, String status) {
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.flightId = flightId;
        this.bookingDate = bookingDate;
        this.status = status;
    }

    // Getters and Setters
    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getFlightId() { return flightId; }
    public void setFlightId(int flightId) { this.flightId = flightId; }

    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}