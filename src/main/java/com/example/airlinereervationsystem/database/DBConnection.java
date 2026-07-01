package com.example.airlinereervationsystem.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility infrastructure class responsible for establishing database connectivity.
 * Acts as the centralized persistence layer gateway, encapsulating driver registration,
 * credential management, and transactional channel initialization for Microsoft SQL Server.
 * <p>
 * <b>Key Software Engineering Concepts Demonstrated:</b>
 * <ul>
 * <li><b>Centralized Configuration Management:</b> Decouples database connection parameters
 * (URL, credentials, encryption flags) from functional control and view components.</li>
 * <li><b>Dynamic Class Loading (Reflection):</b> Explicitly initializes the required SQL Server JDBC
 * Driver class via Java reflection runtime binding.</li>
 * <li><b>Granular Fault Isolation:</b> Separates driver localization errors from structural SQL network
 * transmission failures to streamline production debugging.</li>
 * </ul>
 *
 * @author Donia Ahmed
 * @version 2.0 (Sprint 2)
 */
public class DBConnection {

    // إعدادات نص الاتصال بالـ JDBC الخاص بسيرفر SQL Server مع تفعيل التشفير والأمان
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=AirlineReservationSystem;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "12345678"; // بيانات الاعتماد وكلمة المرور الخاصة بقاعدة البيانات

    /**
     * Instantiates and registers a secure socket connection channel to the SQL Server target catalog.
     * Designed to complement try-with-resources blocks within calling controllers for optimal resource pooling.
     * * @return an active {@link Connection} database object session, or {@code null} if initialization fails.
     */
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // 1. [هندسة برمجية - الديناميكية]: تحميل وتسجيل الدريفر الخاص بـ SQL Server في الذاكرة عبر الانعكاس (Reflection)
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // 2. فتح قناة اتصال نشطة وآمنة مع السيرفر باستخدام المعايير المحددة أعلاه
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("تم الاتصال بقاعدة البيانات بنجاح!");

        } catch (ClassNotFoundException e) {
            // البرمجة الدفاعية: عزل خطأ فقدان مكتبة الدريفر (JAR Dependency) عن أخطاء السيرفر نفسه لسهولة التشخيص
            System.err.println("خطأ: لم يتم العثور على مكتبة SQL Server Driver! يرجى التحقق من ملف الـ dependencies.");
            e.printStackTrace();
        } catch (SQLException e) {
            // معالجة استثناءات فشل الشبكة، أو خطأ في الصلاحيات، أو عدم جاهزية السيرفر لاستقبال الطلبات
            System.err.println("خطأ أثناء محاولة الاتصال بقاعدة البيانات! تحقق من تشغيل السيرفر وصحة نص الاتصال.");
            e.printStackTrace();
        }
        return connection; // إعادة كائن الاتصال ليتم استهلاكه وإغلاقه في كتل الـ Controller
    }
}