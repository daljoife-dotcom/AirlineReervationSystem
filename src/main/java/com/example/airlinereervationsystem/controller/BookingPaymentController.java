package com.example.airlinereervationsystem.controller;

import com.example.airlinereervationsystem.database.DBConnection;
import com.example.airlinereervationsystem.models.UserSession;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class BookingPaymentController {

    @FXML private Label lblFlightSummary;
    @FXML private Label lblTotalAmount;
    @FXML private ComboBox<String> cbPaymentMethod;

    private int currentFlightId;
    private double currentPrice;

    @FXML
    public void initialize() {
        // 1. تعبئة خيارات الدفع في الـ ComboBox
        cbPaymentMethod.setItems(FXCollections.observableArrayList("موبي كاش", "يُسر باي", "لاي باي", "نقدي", "تحويل", "بطاقة", "الصحاري باي"));
        cbPaymentMethod.getSelectionModel().selectFirst();

        // 2. جلب معرف الرحلة التي اختارها المسافر من الشاشة السابقة عبر الـ Session
        currentFlightId = UserSession.getSelectedFlightId();

        if (currentFlightId > 0) {
            loadFlightSummaryDetails();
        }
    }

    /**
     * جلب تفاصيل الرحلة المختارة وعرض الفاتورة للمسافر
     */
    private void loadFlightSummaryDetails() {
        String sql = "SELECT FlightNumber, Destination, FlightDate, Price FROM Flights WHERE FlightID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentFlightId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String flightNum = rs.getString("FlightNumber");
                    String destination = rs.getString("Destination");
                    Timestamp date = rs.getTimestamp("FlightDate");
                    currentPrice = rs.getDouble("Price");

                    lblFlightSummary.setText("الرحلة رقم: " + flightNum + " | الوجهة: " + destination + " | التاريخ: " + date);
                    lblTotalAmount.setText(String.format("%.2f $", currentPrice));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * تأكيد الحجز المالي وحفظ البيانات في جدولي الحجوزات والمدفوعات معاً (Transaction)
     */
    @FXML
    void handleConfirmAndPay(ActionEvent event) {
        if (UserSession.getLoggedInUser() == null) {
            showAlert(Alert.AlertType.ERROR, "خطأ", "انتهت الجلسة، يرجى إعادة تسجيل الدخول.");
            return;
        }

        int customerId = UserSession.getLoggedInUser().getUserId();
        String method = cbPaymentMethod.getValue();

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // بدء المعاملة المركبة (Transaction) لضمان الأمان المالي

            // الخطوة أ: إدراج الحجز في جدول Reservations وجلب المعرف التلقائي
            String insertResSql = "INSERT INTO Reservations (CustomerID, FlightID, BookingDate, ReservationStatus) VALUES (?, ?, GETDATE(), 'Confirmed')";
            int generatedReservationId = -1;

            try (PreparedStatement stmtRes = conn.prepareStatement(insertResSql, Statement.RETURN_GENERATED_KEYS)) {
                stmtRes.setInt(1, customerId);
                stmtRes.setInt(2, currentFlightId);
                stmtRes.executeUpdate();

                try (ResultSet keys = stmtRes.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedReservationId = keys.getInt(1);
                    }
                }
            }

            // الخطوة ب: إدراج الفاتورة في جدول Payments
            String insertPaySql = "INSERT INTO Payments (ReservationID, Amount, PaymentDate, PaymentMethod) VALUES (?, ?, GETDATE(), ?)";
            try (PreparedStatement stmtPay = conn.prepareStatement(insertPaySql)) {
                stmtPay.setInt(1, generatedReservationId);
                stmtPay.setDouble(2, currentPrice);
                stmtPay.setString(3, method);
                stmtPay.executeUpdate();
            }

            // الخطوة ج: تقليل المقاعد المتاحة للرحلة بمقدار مقعد واحد (-1)
            String updateSeatsSql = "UPDATE Flights SET AvailableSeats = AvailableSeats - 1 WHERE FlightID = ? AND AvailableSeats > 0";
            try (PreparedStatement stmtSeats = conn.prepareStatement(updateSeatsSql)) {
                stmtSeats.setInt(1, currentFlightId);
                int rowsUpdated = stmtSeats.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new SQLException("عذراً، نفدت المقاعد المتاحة على هذه الرحلة في هذه اللحظة!");
                }
            }

            // إذا نجحت جميع العمليات الثلاثة، نعتمد الحفظ النهائي في السيرفر
            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "تم الحجز بنجاح", "تهانينا! تم تأكيد حجزك وإصدار تذكرتك بنجاح.");

            // العودة للوحة التحكم
            navigate(event, "/View/PassengerDashboard.fxml", "لوحة تحكم المسافر - حجز الرحلات");

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } // إلغاء كل شيء فوراً في حال حدوث أي خلل
            }
            showAlert(Alert.AlertType.ERROR, "فشل الحجز", "حدث خطأ أثناء معالجة الحجز: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    @FXML
    void handleCancelBooking(ActionEvent event) {
        navigate(event, "/View/PassengerDashboard.fxml", "لوحة تحكم المسافر - حجز الرحلات");
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
