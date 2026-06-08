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

public class LoginController {

    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Button btnRegister;
    @FXML private Button btnToggleView;

    private boolean isLoginMode = true; // للتبديل بين وضع الدخول والتسجيل

    @FXML
    public void initialize() {
        // في البداية، نخفي حقل الاسم الكامل لأن وضع الدخول هو الافتراضي
        txtFullName.setVisible(false);
        txtFullName.setManaged(false);
        btnRegister.setVisible(false);
        btnRegister.setManaged(false);
    }

    /**
     * التبديل بين واجهة تسجيل الدخول وواجهة إنشاء حساب جديد
     */
    @FXML
    void handleToggleView(ActionEvent event) {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            txtFullName.setVisible(false);
            txtFullName.setManaged(false);
            btnLogin.setVisible(true);
            btnLogin.setManaged(true);
            btnRegister.setVisible(false);
            btnRegister.setManaged(false);
            btnToggleView.setText("ليس لديك حساب؟ سجل الآن كمسافر");
        } else {
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
     * عملية تسجيل الدخول وفحص نوع المستخدم
     */
    @FXML
    void handleLogin(ActionEvent event) {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "تنبيه", "يرجى ملء جميع الحقول المطلوبة!");
            return;
        }

        String sql = "SELECT * FROM SystemUsers WHERE Email = ? AND PasswordHash = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password); // في المشاريع المتقدمة نستخدم تشفير، هنا نتحقق مباشرة

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("UserID");
                    String name = rs.getString("FullName");
                    String type = rs.getString("UserType");

                    // توجيه المستخدم وحفظه في الجلسة بناءً على نوعه
                    if (type.equalsIgnoreCase("Admin")) {
                        UserSession.setLoggedInUser(new Admin(id, name, email, password));
                        showAlert(Alert.AlertType.INFORMATION, "نجاح الدخول", "مرحباً بك يا مسؤول النظام: " + name);
                        navigate(event, "/View/AdminDashboard.fxml", "لوحة تحكم المسؤول - إدارة الرحلات");
                    } else {
                        UserSession.setLoggedInUser(new Customer(id, name, email, password));
                        showAlert(Alert.AlertType.INFORMATION, "نجاح الدخول", "رحلة سعيدة ومرحباً بك: " + name);
                        navigate(event, "/View/PassengerDashboard.fxml", "لوحة تحكم المسافر - حجز الرحلات");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "خطأ في الدخول", "البريد الإلكتروني أو كلمة المرور غير صحيحة!");
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "خطأ في السيرفر", "فشل الاتصال بقاعدة البيانات: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * عملية إنشاء حساب جديد كمسافر (Customer)
     */
    @FXML
    void handleRegister(ActionEvent event) {
        String name = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "تنبيه", "يرجى ملء الاسم والبريد وكلمة المرور!");
            return;
        }

        String sql = "INSERT INTO SystemUsers (FullName, Email, PasswordHash, UserType) VALUES (?, ?, ?, 'Customer')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);

            stmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "تم التسجيل", "تم إنشاء حسابك بنجاح كمسافر! يمكنك الآن تسجيل الدخول.");

            // العودة لوضع الدخول تلقائياً
            handleToggleView(null);

        } catch (SQLException e) {
            if (e.getErrorCode() == 2627 || e.getMessage().contains("UNIQUE")) {
                showAlert(Alert.AlertType.WARNING, "تنبيه", "هذا البريد الإلكتروني مسجل مسبقاً بالنظام!");
            } else {
                showAlert(Alert.AlertType.ERROR, "خطأ", "فشل إنشاء الحساب: " + e.getMessage());
            }
        }
    }

    private void navigate(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "خطأ تنقل", "لم نتمكن من فتح الواجهة المطلوبة: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
