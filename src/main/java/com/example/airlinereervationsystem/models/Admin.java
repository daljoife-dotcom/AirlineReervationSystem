package com.example.airlinereervationsystem.models;

public class Admin extends User {

    public Admin(int userId, String fullName, String email, String passwordHash) {
        super(userId, fullName, email, passwordHash, "Admin");
    }

    @Override
    public void displayDashboardMessage() {
        System.out.println("تنبيه الإدارة: مرحباً بالمسؤول " + fullName + ". لديك الصلاحية الكاملة لتعديل الرحلات.");
    }
}