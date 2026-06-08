package com.example.airlinereervationsystem.database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=AirlineReservationSystem;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = ""; // كلمة مرور قاعدة البيانات

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // تحميل الدريفر الخاص بـ SQL Server
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("تم الاتصال بقاعدة البيانات بنجاح!");
        } catch (ClassNotFoundException e) {
            System.err.println("خطأ: لم يتم العثور على مكتبة SQL Server Driver!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("خطأ أثناء محاولة الاتصال بقاعدة البيانات!");
            e.printStackTrace();
        }
        return connection;
    }
}