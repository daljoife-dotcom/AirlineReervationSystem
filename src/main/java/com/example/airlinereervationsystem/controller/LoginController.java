package com.example.airlinereervationsystem.controller;

import com.example.airlinereervationsystem.database.DBConnection;
import com.example.airlinereervationsystem.models.UserSession;
import com.example.airlinereervationsystem.models.Admin;
import com.example.airlinereervationsystem.models.Customer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controller class responsible for managing user Authentication, Registration, and Scene navigation.
 * This class coordinates the interaction between the Login view interface and the Database persistence layer.
 * <p>
 * <b>Key Software Engineering & OOP Implementations:</b>
 * <ul>
 * <li><b>MVC Architecture:</b> Acts as a Controller capturing UI events and translating them into Model states.</li>
 * <li><b>Advanced Exception Handling:</b> Employs Try-With-Resources blocks to guarantee safe resource closure (Connections, Statements, ResultSets).</li>
 * <li><b>Polymorphism:</b> Demonstrates runtime binding by instantiating either {@link Admin} or {@link Customer} based on database criteria.</li>
 * <li><b>Input Validation:</b> Performs basic client-side verification to filter out empty/malformed records before querying.</li>
 * </ul>
 *
 * @author Donia Ahmed
 * @version 2.0 (Sprint 2)
 */
public class LoginController {

    // ربط عناصر واجهة المستخدم (FXML Components) بالتعليمات البرمجية
    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Button btnRegister;
    @FXML private Button btnToggleView;

    // متغير منطقي لتحديد الوضع الحالي للشاشة (تسجيل دخول أم إنشاء حساب) لتبديل العناصر ديناميكياً
    private boolean isLoginMode = true;

    /**
     * Initializes the controller automatically after the FXML file has been loaded.
     * Sets up the initial default state for the interface login view.
     */
    @FXML
    public void initialize() {
        // تهيئة الواجهة: إخفاء حقول الاسم وزر التسجيل لأن وضع تسجيل الدخول هو الافتراضي عند تشغيل الشاشة
        // خاصية setManaged(false) تضمن إلغاء حجز مساحة العنصر في الواجهة لتبدو متناسقة
        txtFullName.setVisible(false);
        txtFullName.setManaged(false);
        btnRegister.setVisible(false);
        btnRegister.setManaged(false);
    }

    /**
     * Toggles the interface dynamically between Login mode and Register/Sign-up mode.
     * Alters visibility attributes and button label prompts contextually.
     * * @param event the structural action event triggered by the toggle button click
     */
    @FXML
    void handleToggleView(ActionEvent event) {
        isLoginMode = !isLoginMode; // عكس الحالة الحالية للواجهة

        if (isLoginMode) {
            // تفعيل وضع تسجيل الدخول وإخفاء عناصر إنشاء الحساب
            txtFullName.setVisible(false);
            txtFullName.setManaged(false);
            btnLogin.setVisible(true);
            btnLogin.setManaged(true);
            btnRegister.setVisible(false);
            btnRegister.setManaged(false);
            btnToggleView.setText("ليس لديك حساب؟ سجل الآن كمسافر");
        } else {
            // تفعيل وضع إنشاء الحساب الجديد وإظهار حقل الاسم والزر المخصص
            txtFullName.setVisible(true);
            txtFullName.setManaged(true);
            btnLogin.setVisible(false);
            btnLogin.setManaged(false);
            btnRegister.setVisible(true);
            btnRegister.setManaged(true);
            btnToggleView.setText("لديك حساب بالفعل؟ سجل دخولك");
        }
    }

    /**
     * Validates credentials and processes the authentication request against the database records.
     * Incorporates polymorphic instantiation to handle user roles properly.
     * * @param event the structural action event triggered by clicking the Login button
     */
    @FXML
    void handleLogin(ActionEvent event) {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        // 1. التحقق من المدخلات (Input Validation): منع الاستعلام ببيانات فارغة لحماية النظام
        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "تنبيه", "يرجى ملء جميع الحقول المطلوبة!");
            return;
        }

        // استخدام الاستعلامات المجهزة (Parameterized Queries) لمنع هجمات الاختراق من نوع SQL Injection
        String sql = "SELECT * FROM SystemUsers WHERE Email = ? AND PasswordHash = ?";

        // 2. معالجة الاستثناءات (Exception Handling): استخدام try-with-resources لفتح وإغلاق الموارد تلقائياً
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("UserID");
                    String name = rs.getString("FullName");
                    String type = rs.getString("UserType");

                    // 3. تعدد الأشكال (Polymorphism) وإدارة الجلسة العامة (State Management):
                    // بناء الكائن وتخزينه في الـ Session بناءً على نوعه القادم من قاعدة البيانات لتبني صلاحياته لاحقاً
                    if (type.equalsIgnoreCase("Admin")) {
                        UserSession.setLoggedInUser(new Admin(id, name, email, password));
                        showAlert(Alert.AlertType.INFORMATION, "نجاح الدخول", "مرحباً بك يا مسؤول النظام: " + name);
                        // التنقل إلى لوحة تحكم الإدارة
                        navigate(event, "/View/AdminDashboard.fxml", "لوحة تحكم المسؤول - إدارة الرحلات");
                    } else {
                        UserSession.setLoggedInUser(new Customer(id, name, email, password));
                        showAlert(Alert.AlertType.INFORMATION, "نجاح الدخول", "رحلة سعيدة ومرحباً بك: " + name);
                        // التنقل إلى لوحة تحكم المسافرين
                        navigate(event, "/View/PassengerDashboard.fxml", "لوحة تحكم المسافر - حجز الرحلات");
                    }
                } else {
                    // رسالة توضح فشل التحقق في حال عدم تطابق البيانات
                    showAlert(Alert.AlertType.ERROR, "خطأ في الدخول", "البريد الإلكتروني أو كلمة المرور غير صحيحة!");
                }
            }
        } catch (SQLException e) {
            // معالجة خطأ الاتصال بالسيرفر وإظهار رسالة صديقة للمستخدم منعا لانهيار التطبيق
            showAlert(Alert.AlertType.ERROR, "خطأ في السيرفر", "فشل الاتصال بقاعدة البيانات: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the creation and database insertion of a new Customer user record.
     * Ensures exception handling filters specific unique constraint violations.
     * * @param event the structural action event triggered by clicking the Register button
     */
    @FXML
    void handleRegister(ActionEvent event) {
        String name = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        // التحقق من أن جميع حقول التسجيل ممتلئة قبل المعالجة
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "تنبيه", "يرجى ملء الاسم والبريد وكلمة المرور!");
            return;
        }

        // الاستعلام الخاص بإضافة حساب جديد بنوع افتراضي 'Customer'
        String sql = "INSERT INTO SystemUsers (FullName, Email, PasswordHash, UserType) VALUES (?, ?, ?, 'Customer')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);

            stmt.executeUpdate(); // تنفيذ الحفظ في قاعدة البيانات
            showAlert(Alert.AlertType.INFORMATION, "تم التسجيل", "تم إنشاء حسابك بنجاح كمسافر! يمكنك الآن تسجيل الدخول.");

            // العودة التلقائية لواجهة تسجيل الدخول لتسهيل تجربة المستخدم (UX)
            handleToggleView(null);

        } catch (SQLException e) {
            // معالجة ذكية للخطأ في حال كان البريد الإلكتروني مسجلاً مسبقاً (Unique Constraint Violation)
            // رمز الخطأ 2627 شهير في SQL Server للحقول الفريدة
            if (e.getErrorCode() == 2627 || e.getMessage().contains("UNIQUE")) {
                showAlert(Alert.AlertType.WARNING, "تنبيه", "هذا البريد الإلكتروني مسجل مسبقاً بالنظام!");
            } else {
                showAlert(Alert.AlertType.ERROR, "خطأ", "فشل إنشاء الحساب: " + e.getMessage());
            }
        }
    }

    /**
     * Shared utility function to load and navigate between different JavaFX FXML screens.
     * * @param event    the action event supplying the current Stage context
     * @param fxmlPath the target FXML file path location
     * @param title    the header title string to display on the navigated window stage
     */
    private void navigate(ActionEvent event, String fxmlPath, String title) {
        try {
            // تحميل ملف الـ FXML الجديد ديناميكياً
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // جلب الـ Stage الحالي المستضيف للنافذة وتحديث الـ Scene الخاص به
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            // معالجة استثناء أخطاء الإدخال والإخراج عند فقدان ملف الواجهة أو خطأ في المسار
            showAlert(Alert.AlertType.ERROR, "خطأ تنقل", "لم نتمكن من فتح الواجهة المطلوبة: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Builds and serves standardized desktop alert notification dialogues to the end user.
     * * @param alertType the defined structural categorization of the alert (ERROR, INFO, WARNING)
     * @param title     the context window title
     * @param content   the textual main description core message to be displayed
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // إلغاء النص الفرعي العلوي لتوفير مظهر بسيط ومنظم
        alert.setContentText(content);
        alert.showAndWait(); // إيقاف تنفيذ الكود مؤقتاً حتى يقوم المستخدم بضغط زر الإغلاق/الموافقة
    }
}