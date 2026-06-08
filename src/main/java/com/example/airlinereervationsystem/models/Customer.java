package com.example.airlinereervationsystem.models;

public class Customer extends User {
    // يمكن إضافة خصائص فريدة للمسافر مستقبلاً مثل رقم الجواز أو الهاتف

    public Customer(int userId, String fullName, String email, String passwordHash) {
        super(userId, fullName, email, passwordHash, "Customer");
    }

    @Override
    public void displayDashboardMessage() {
        System.out.println("مرحباً بك يا " + fullName + " في لوحة حجز الرحلات الخاصة بك.");
    }
}