package com.example.airlinereervationsystem.models;

/**
 * Represents an Administrative user in the Airline Reservation System.
 * This class inherits from the abstract class {@link User} and provides specific
 * implementation for administrative dashboard behaviors.
 * <p>
 * <b>OOP Concepts Demonstrated:</b>
 * <ul>
 * <li><b>Inheritance:</b> Extends the {@code User} class to reuse common properties and methods.</li>
 * <li><b>Polymorphism (Method Overriding):</b> Implements the abstract {@code displayDashboardMessage} method.</li>
 * </ul>
 *
 * @author Donia Ahmed
 * @version 2.0 (Sprint 2)
 */
public class Admin extends User {

    /**
     * Constructs a new Admin instance and initializes the base User fields.
     * <p>
     * <b>Code Note:</b> يتم تمرير القيمة الثابتة {@code "Admin"} تلقائياً كنوع للمستخدِم
     * إلى الكلاس الأب لتخفيف العبء عند إنشاء كائن جديد من هذا الكلاس.
     *
     * @param userId       the unique identifier for the administrator
     * @param fullName     the full name of the administrator
     * @param email        the email address used for admin authentication
     * @param passwordHash the securely hashed password
     */
    public Admin(int userId, String fullName, String email, String passwordHash) {
        // استدعاء مشيّد الكلاس الأب (User Constructor) لتمرير البيانات وتخزينها
        super(userId, fullName, email, passwordHash, "Admin");
    }

    /**
     * Displays an administration-specific welcome alert message on the console/dashboard.
     * <p>
     * <b>OOP Note (Polymorphism):</b> استخدام الوسم {@code @Override} يضمن إعادة تعريف
     * الدالة التجريدية القادمة من الكلاس الأب بشكل صحيح، لتقوم بطباعة رسالة مخصصة تليق بصلاحيات المسؤول.
     */
    @Override
    public void displayDashboardMessage() {
        System.out.println("تنبيه الإدارة: مرحباً بالمسؤول " + fullName + ". لديك الصلاحية الكاملة لتعديل الرحلات.");
    }
}