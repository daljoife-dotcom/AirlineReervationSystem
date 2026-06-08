package com.example.airlinereervationsystem.models;
import java.time.LocalDateTime;

public class Flight {
    private int flightId;
    private String flightNumber;
    private String destination;
    private LocalDateTime flightDate;
    private double price;
    private int aircraftId; // الربط مع جدول الطائرات
    private int availableSeats;

    public Flight(int flightId, String flightNumber, String destination, LocalDateTime flightDate, double price, int aircraftId, int availableSeats) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.destination = destination;
        this.flightDate = flightDate;
        this.price = price;
        this.aircraftId = aircraftId;
        this.availableSeats = availableSeats;
    }

    // Getters and Setters
    public int getFlightId() { return flightId; }
    public void setFlightId(int flightId) { this.flightId = flightId; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public LocalDateTime getFlightDate() { return flightDate; }
    public void setFlightDate(LocalDateTime flightDate) { this.flightDate = flightDate; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getAircraftId() { return aircraftId; }
    public void setAircraftId(int aircraftId) { this.aircraftId = aircraftId; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
}
