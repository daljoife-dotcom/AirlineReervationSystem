package com.example.airlinereervationsystem.models;
// كلاس المسؤول admin يقوم بالورثاة من كلاس المستخدم User
public class Admin extends User {

    // Constructor
    public Admin(int userId, String fullName, String email, String passwordHash) {
        super(userId, fullName, email, passwordHash, "Admin");
    }

    @Override
    public void displayDashboardMessage() {
        System.out.println("تنبيه الإدارة: مرحباً بالمسؤول " + fullName + ". لديك الصلاحية الكاملة لتعديل الرحلات.");
    }
}