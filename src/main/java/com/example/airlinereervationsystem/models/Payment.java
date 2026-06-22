package com.example.airlinereervationsystem.models;

import java.time.LocalDateTime;

/**
 * Represents a financial Payment transaction within the Airline Reservation System.
 * This class tracks transaction identities, linked bookings, monetary amounts,
 * execution timestamps, and payment modalities.
 * <p>
 * <b>OOP Concepts Demonstrated:</b>
 * <ul>
 * <li><b>Encapsulation:</b> Employs strict {@code private} visibility modifiers for financial
 * data security, exposing state alterations exclusively via public accessors.</li>
 * <li><b>Entity Association:</b> Establishes a structural connection with the Reservation
 * entity using {@code reservationId} as a relational reference.</li>
 * </ul>
 *
 * @author Donia Ahmed
 * @version 2.0 (Sprint 2)
 */
public class Payment {

    // تطبيق مبدأ الكبسلة (Encapsulation) لحماية البيانات المالية الحساسة من التلاعب الخارجي
    private int paymentId;
    private int reservationId; // مفهوم الاقتران (Association): يربط الفاتورة برقم الحجز الخاص بالمسافر
    private double amount;     // المبلغ المالي المدفوع للتذكرة
    private LocalDateTime paymentDate; // توثيق زمن وطابع وقت العملية المالية بدقة عالية
    private String paymentMethod;     // طريقة الدفع (مثال: Credit Card, PayPal, Cash)

    /**
     * Constructs a new Payment transaction record with audited financial metadata.
     *
     * @param paymentId     the unique transaction reference identifier in the database
     * @param reservationId the target reservation ID this payment settles
     * @param amount        the total monetary value processed
     * @param paymentDate   the explicit date and time when the payment occurred
     * @param paymentMethod the platform or instrument used for payment routing
     */
    public Payment(int paymentId, int reservationId, double amount, LocalDateTime paymentDate, String paymentMethod) {
        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
    }

    // ==========================================
    // دوال الكبسلة العامة (Public Getters & Setters)
    // لضمان الوصول الآمن والمنظم لبيانات الفواتير والمعاملات
    // ==========================================

    /**
     * Gets the unique payment identifier.
     * @return the integer payment ID
     */
    public int getPaymentId() { return paymentId; }

    /**
     * Sets the unique payment identifier.
     * @param paymentId the new payment ID to set
     */
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    /**
     * Gets the associated reservation identifier.
     * @return the reservation ID linked to this payment
     */
    public int getReservationId() { return reservationId; }

    /**
     * Sets the associated reservation identifier.
     * @param reservationId the new reservation ID to bind
     */
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    /**
     * Gets the transaction amount.
     * @return the double value of the amount paid
     */
    public double getAmount() { return amount; }

    /**
     * Sets the transaction amount.
     * @param amount the new monetary amount to set
     */
    public void setAmount(double amount) { this.amount = amount; }

    /**
     * Gets the timestamp of when the payment was processed.
     * @return the LocalDateTime payment timestamp
     */
    public LocalDateTime getPaymentDate() { return paymentDate; }

    /**
     * Sets the timestamp of when the payment was processed.
     * @param paymentDate the new LocalDateTime timestamp to set
     */
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    /**
     * Gets the payment method description.
     * @return the string representing payment method
     */
    public String getPaymentMethod() { return paymentMethod; }

    /**
     * Sets the payment method description.
     * @param paymentMethod the new payment method string to set
     */
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}