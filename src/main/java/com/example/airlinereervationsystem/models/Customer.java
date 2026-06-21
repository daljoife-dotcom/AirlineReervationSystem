package com.example.airlinereervationsystem.models;

/**
 * Represents a Customer/Passenger user in the Airline Reservation System.
 * This class inherits from the abstract class {@link User} and provides specific
 * implementation for customer-facing dashboard behaviors.
 * <p>
 * <b>OOP Concepts Demonstrated:</b>
 * <ul>
 * <li><b>Inheritance:</b> Extends the {@code User} class to leverage shared attributes and methods.</li>
 * <li><b>Polymorphism (Method Overriding):</b> Overrides {@code displayDashboardMessage} to show a tailored greeting.</li>
 * </ul>
 *
 * @author Donia Ahmed
 * @version 2.0 (Sprint 2)
 */
public class Customer extends User {

    // ملاحظة هندسية: يمكن إضافة خصائص فريدة للمسافر مستقبلاً هنا (مثل: passportNumber أو phoneNumber)
    // تماشياً مع مبدأ الكبسلة (Encapsulation) والتوسعية في المستقبل لخدمة عمليات الحجز.

    /**
     * Constructs a new Customer instance and initializes the base User fields.
     * <p>
     * <b>Code Note:</b> يتم تمرير القيمة الثابتة {@code "Customer"} تلقائياً كنوع للمستخدِم
     * إلى مشيّد الكلاس الأب لتحديد دور الحساب وصلاحياته في النظام بشكل صارم وآمن.
     *
     * @param userId       the unique identifier for the customer
     * @param fullName     the full name of the customer
     * @param email        the email address used for customer login
     * @param passwordHash the securely hashed password
     */
    public Customer(int userId, String fullName, String email, String passwordHash) {
        // استدعاء مشيّد الكلاس الأب (User Constructor) وتمرير البيانات الأساسية إليه لتهيئتها
        super(userId, fullName, email, passwordHash, "Customer");
    }

    /**
     * Displays a customer-specific welcome greeting message on the console/dashboard.
     * <p>
     * <b>OOP Note (Polymorphism):</b> تم استخدام الوسم {@code @Override} لإعادة تعريف الدالة
     * التجريدية القادمة من الكلاس الأب {@code User}. عند استدعاء هذه الدالة عبر كائن من نوع Customer،
     * سيتم تنفيذ هذا السلوك المخصص للمسافرين بدلاً من المسؤولين ديناميكياً أثناء تشغيل النظام.
     */
    @Override
    public void displayDashboardMessage() {
        System.out.println("مرحباً بك يا " + fullName + " في لوحة حجز الرحلات الخاصة بك.");
    }
}