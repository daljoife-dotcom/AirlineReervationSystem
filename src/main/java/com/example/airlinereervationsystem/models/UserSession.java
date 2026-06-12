package com.example.airlinereervationsystem.models;

public class UserSession { // كلاس جلسة المستخدم
    private static User loggedInUser;
    private static int selectedFlightId;

    public static void clearSession() { loggedInUser = null; selectedFlightId = 0; }

    // getter , setter
    public static void setLoggedInUser(User user) { loggedInUser = user; }
    public static User getLoggedInUser() { return loggedInUser; }



    public static int getSelectedFlightId() { return selectedFlightId; }
    public static void setSelectedFlightId(int flightId) { selectedFlightId = flightId; }
}