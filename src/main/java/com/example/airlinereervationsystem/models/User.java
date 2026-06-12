package com.example.airlinereervationsystem.models;
// إنشاء كلاس من نوع abstract لنقوم بالوراثة منه
public abstract class User {
    protected int userId;
    protected String fullName;
    protected String email;
    protected String passwordHash;
    protected String userType; // تحديد هل المستخدم admin او Customer

    // Constructor
    public User(int userId, String fullName, String email, String passwordHash, String userType) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.userType = userType;
    }

    // دالة تجريدية لإظهار تعدد الأشكال (Polymorphism)
    public abstract void displayDashboardMessage();

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
}
