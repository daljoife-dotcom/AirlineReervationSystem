package com.example.airlinereervationsystem.models;

public class Aircraft {
    private int aircraftId;
    private String aircraftModel;
    private int totalCapacity;

    public Aircraft(int aircraftId, String aircraftModel, int totalCapacity) {
        this.aircraftId = aircraftId;
        this.aircraftModel = aircraftModel;
        this.totalCapacity = totalCapacity;
    }

    // Getters and Setters
    public int getAircraftId() { return aircraftId; }
    public void setAircraftId(int aircraftId) { this.aircraftId = aircraftId; }

    public String getAircraftModel() { return aircraftModel; }
    public void setAircraftModel(String aircraftModel) { this.aircraftModel = aircraftModel; }

    public int getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }
}
