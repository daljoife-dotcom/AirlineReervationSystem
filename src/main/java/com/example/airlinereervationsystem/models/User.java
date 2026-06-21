package com.example.airlinereervationsystem.models;

/**
 * Represents the abstract base class for all users in the Airline Reservation System.
 * This class serves as the foundation for user management and demonstrates core
 * Advanced OOP concepts including:
 * <ul>
 * <li><b>Abstraction:</b> Declared as an abstract class that cannot be instantiated directly.</li>
 * <li><b>Inheritance:</b> Acts as a superclass for concrete roles like Admin and Customer.</li>
 * <li><b>Encapsulation:</b> Uses protected fields and wraps them in public getters and setters.</li>
 * <li><b>Polymorphism:</b> Defines an abstract method for role-specific behaviors.</li>
 * </ul>
 *
 * @author Donia Ahmed
 * @version 2.0 (Sprint 2)
 */
public abstract class User {

    // استخدام مستوى الوصول المحمي (protected) لتمكين الكلاسات الابنة (Subclasses)
    // من وراثة هذه الخصائص مباشرة، مع منع الكلاسات الخارجية من التلاعب بها (مفهوم الوراثة والكبسلة)
    protected int userId;
    protected String fullName;
    protected String email;
    protected String passwordHash;
    protected String userType; // يحدد صلاحية المستخدم في النظام (مثال: Admin أو Customer)

    /**
     * Constructs a new User instance with the required personal and security credentials.
     *
     * @param userId       the unique identifier for the user in the database
     * @param fullName     the full name of the user
     * @param email        the unique email address used as the login credential
     * @param passwordHash the securely hashed password for authentication
     * @param userType     the role or account type of the user
     */
    public User(int userId, String fullName, String email, String passwordHash, String userType) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.userType = userType;
    }

    /**
     * Displays a dynamic and tailored dashboard greeting message based on the user's role.
     * <p>
     * <b>OOP Note (Polymorphism):</b> هذه دالة تجريدية (Abstract Method) لا تحتوي على كود هنا،
     * بل تُجبر كلاس الـ Admin وكلاس الـ Customer على إعادة تعريفها (Override) لتقديم سلوك مختلف
     * بناءً على نوع المستخدم عند تشغيل النظام.
     */
    public abstract void displayDashboardMessage();

    // ==========================================
    // مفهوم الكبسلة (Encapsulation): الدوال العامة (Getters & Setters)
    // لحماية البيانات والتحكم في كيفية قراءتها أو تعديلها من خارج الكلاس
    // ==========================================

    /**
     * Gets the unique user ID.
     * @return the integer user ID
     */
    public int getUserId() { return userId; }

    /**
     * Sets the unique user ID.
     * @param userId the new user ID to set
     */
    public void setUserId(int userId) { this.userId = userId; }

    /**
     * Gets the full name of the user.
     * @return the string representing full name
     */
    public String getFullName() { return fullName; }

    /**
     * Sets the full name of the user.
     * @param fullName the new full name to set
     */
    public void setFullName(String fullName) { this.fullName = fullName; }

    /**
     * Gets the email address of the user.
     * @return the string email
     */
    public String getEmail() { return email; }

    /**
     * Sets the email address of the user.
     * @param email the new email to set
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Gets the securely hashed password.
     * @return the hashed password string
     */
    public String getPasswordHash() { return passwordHash; }

    /**
     * Sets the securely hashed password.
     * @param passwordHash the new hashed password to set
     */
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    /**
     * Gets the user role type (Admin/Customer).
     * @return the user type string
     */
    public String getUserType() { return userType; }

    /**
     * Sets the user role type (Admin/Customer).
     * @param userType the new user type to set
     */
    public void setUserType(String userType) { this.userType = userType; }
}