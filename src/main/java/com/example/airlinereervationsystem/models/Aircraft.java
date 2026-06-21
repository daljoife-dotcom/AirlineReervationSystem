package com.example.airlinereervationsystem.models;

/**
 * Represents an Aircraft entity in the Airline Reservation System.
 * This class encapsulates the physical attributes of an airplane, including its
 * unique database ID, model identification, and maximum passenger capacity.
 * <p>
 * <b>OOP Concepts Demonstrated:</b>
 * <ul>
 * <li><b>Encapsulation:</b> All class attributes are strictly defined as {@code private}
 * to protect data integrity, exposing control only via public getters and setters.</li>
 * </ul>
 *
 * @author Donia Ahmed
 * @version 2.0 (Sprint 2)
 */
public class Aircraft {

    // تطبيق مبدأ الكبسلة (Encapsulation): جعل الخصائص مخفية (private) لمنع
    // التعديل العشوائي أو المباشر عليها من خارج الكلاس لحماية سلامة البيانات.
    private int aircraftId;
    private String aircraftModel;
    private int totalCapacity;

    /**
     * Constructs a new Aircraft instance with specified structural and operational details.
     *
     * @param aircraftId     the unique identifier for the aircraft in the database
     * @param aircraftModel  the commercial or technical model name (e.g., "Boeing 737")
     * @param totalCapacity  the maximum number of passenger seats available on board
     */
    public Aircraft(int aircraftId, String aircraftModel, int totalCapacity) {
        this.aircraftId = aircraftId;
        this.aircraftModel = aircraftModel;
        this.totalCapacity = totalCapacity;
    }

    // ==========================================
    // دوال الكبسلة العامة (Public Getters & Setters)
    // للتحكم الآمن في قراءة وتحديث بيانات الطائرة
    // ==========================================

    /**
     * Gets the unique aircraft ID.
     * @return the integer aircraft ID
     */
    public int getAircraftId() { return aircraftId; }

    /**
     * Sets the unique aircraft ID.
     * @param aircraftId the new aircraft ID to set
     */
    public void setAircraftId(int aircraftId) { this.aircraftId = aircraftId; }

    /**
     * Gets the commercial model name of the aircraft.
     * @return the string representing aircraft model
     */
    public String getAircraftModel() { return aircraftModel; }

    /**
     * Sets the commercial model name of the aircraft.
     * @param aircraftModel the new aircraft model to set
     */
    public void setAircraftModel(String aircraftModel) { this.aircraftModel = aircraftModel; }

    /**
     * Gets the total seat capacity of the aircraft.
     * @return the maximum passenger capacity
     */
    public int getTotalCapacity() { return totalCapacity; }

    /**
     * Sets the total seat capacity of the aircraft.
     * @param totalCapacity the new passenger capacity to set
     */
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }
}