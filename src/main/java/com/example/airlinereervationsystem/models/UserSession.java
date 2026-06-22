package com.example.airlinereervationsystem.models;

/**
 * Manages the global state and active session data for the Airline Reservation System.
 * This utility class utilizes static members to maintain session persistence
 * (e.g., the currently authenticated user and the actively selected flight)
 * across various JavaFX scenes and controllers.
 * <p>
 * <b>OOP & Design Concepts Demonstrated:</b>
 * <ul>
 * <li><b>Static Class Pattern:</b> Uses {@code static} fields and methods to allow global
 * access to session states without requiring class instantiation.</li>
 * <li><b>Encapsulation:</b> Session data fields are securely hidden using {@code private static}
 * modifiers and exposed safely through public accessors.</li>
 * </ul>
 *
 * @author Donia Ahmed
 * @version 2.0 (Sprint 2)
 */
public class UserSession {

    // تطبيق مبدأ الكبسلة (Encapsulation) على مستوى الصف (Class-level Scope):
    // استخدام المتغيرات الساكنة (static) لحفظ حالة واحدة مشتركة للنظام بالكامل أثناء التشغيل،
    // وجعلها private لمنع التلاعب بجلسة المغادرة أو الدخول بشكل عشوائي.
    private static User loggedInUser;
    private static int selectedFlightId;

    /**
     * Clears all active session attributes, effectively logging out the current user
     * and resetting any temporary flight selections.
     */
    public static void clearSession() {
        loggedInUser = null;
        selectedFlightId = 0;
    }

    // ==========================================
    // دوال الكبسلة الساكنة (Public Static Getters & Setters)
    // للوصول الآمن للبيانات والتحكم بالجلسة من أي شاشة (Controller) داخل النظام
    // ==========================================

    /**
     * Stores the currently authenticated user into the global session.
     * @param user the polymorphic {@link User} object (can be an Admin or Customer)
     */
    public static void setLoggedInUser(User user) { loggedInUser = user; }

    /**
     * Retrieves the currently active user in the session.
     * <p>
     * <b>OOP Note:</b> تعيد هذه الدالة كائن من نوع {@code User} (الكلاس الأب)،
     * مما يسمح للنظام بمعاملة المستخدم الحالي بمرونة وتحديد صلاحياته ديناميكياً (Polymorphism).
     * @return the logged-in {@link User} object, or null if no active session exists
     */
    public static User getLoggedInUser() { return loggedInUser; }

    /**
     * Gets the ID of the flight actively selected by the customer for booking.
     * @return the integer selected flight ID
     */
    public static int getSelectedFlightId() { return selectedFlightId; }

    /**
     * Globally sets or updates the flight identifier chosen during the flight search process.
     * @param flightId the target flight database reference identifier
     */
    public static void setSelectedFlightId(int flightId) { selectedFlightId = flightId; }
}