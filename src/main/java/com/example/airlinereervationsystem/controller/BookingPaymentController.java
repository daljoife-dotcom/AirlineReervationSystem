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

/**
 * Controller class handling flight reservation confirmation and payment processing.
 * This critical architectural component enforces strict multi-step Database Transactions (ACID)
 * to maintain synchronization across reservations, monetary records, and aircraft seating inventories.
 * <p>
 * <b>Key Software Engineering Concepts Demonstrated:</b>
 * <ul>
 * <li><b>Database Transactions (ACID Compliance):</b> Disables auto-commit to cluster multiple
 * modifications into a single atomic block, triggering rollbacks upon any mid-operation exception.</li>
 * <li><b>Generated Key Retrieval:</b> Dynamically captures auto-incremented database identifiers
 * at runtime to build relational links between foreign records.</li>
 * <li><b>State Management Integration:</b> Interacts with {@link UserSession} to contextually pull
 * user identity and targeted operational metadata across views.</li>
 * </ul>
 *
 * @author Donia Ahmed
 * @version 2.0 (Sprint 2)
 */
public class BookingPaymentController {

    // ربط عناصر واجهة المستخدم لعرض تفاصيل الفاتورة وخيارات الدفع الالكتروني
    @FXML private Label lblFlightSummary;
    @FXML private Label lblTotalAmount;
    @FXML private ComboBox<String> cbPaymentMethod;

    private int currentFlightId;
    private double currentPrice;

    /**
     * Initializes the controller automatically upon screen layout loading.
     * Populates payment gateways, fetches shared session tokens, and requests flight details.
     */
    @FXML
    public void initialize() {
        // 1. تعبئة خيارات الدفع الإلكتروني والمحلي المتنوعة في الـ ComboBox
        cbPaymentMethod.setItems(FXCollections.observableArrayList("موبي كاش", "يُسر باي", "لاي باي", "نقدي", "تحويل", "بطاقة", "الصحاري باي"));
        cbPaymentMethod.getSelectionModel().selectFirst(); // تحديد الخيار الأول كافتراضي لتسهيل تجربة المستخدم

        // 2. إدارة الحالة (State Management): جلب معرّف الرحلة التي اختارها المسافر من الشاشة السابقة عبر الجلسة العامة
        currentFlightId = UserSession.getSelectedFlightId();

        if (currentFlightId > 0) {
            loadFlightSummaryDetails(); // تحميل وعرض الفاتورة إذا كان المعرف صالحاً
        }
    }

    /**
     * Queries the database for the selected flight's contextual details and serves the receipt preview.
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

                    // تحديث نصوص الواجهة لعرض ملخص الرحلة والمبلغ المطلوب بدقة
                    lblFlightSummary.setText("الرحلة رقم: " + flightNum + " | الوجهة: " + destination + " | التاريخ: " + date);
                    lblTotalAmount.setText(String.format("%.2f $", currentPrice));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the comprehensive transactional process of confirming a booking, logging payments,
     * and reducing available seat counts within a single atomic database context.
     * * @param event the triggered ActionEvent from the confirmation UI button
     */
    @FXML
    void handleConfirmAndPay(ActionEvent event) {
        // التحقق من أمان الجلسة: التأكد من أن المستخدم مسجل الدخول قبل إجراء أي عملية مالية
        if (UserSession.getLoggedInUser() == null) {
            showAlert(Alert.AlertType.ERROR, "خطأ", "انتهت الجلسة، يرجى إعادة تسجيل الدخول.");
            return;
        }

        int customerId = UserSession.getLoggedInUser().getUserId();
        String method = cbPaymentMethod.getValue();

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();

            // 🌟 [هندسة برمجية متقدمة]: إلغاء الالتزام التلقائي لبدء معاملة مركبة (Transaction)
            // نضمن من خلالها تنفيذ الخطوات (أ، ب، ج) معاً بنجاح، أو تراجع النظام عن كل ما تم في حال حدوث أي خطأ حمايةً للبيانات.
            conn.setAutoCommit(false);

            // -----------------------------------------------------------------
            // الخطوة أ: إدراج سجل الحجز في جدول Reservations وجلب المعرف المولد تلقائياً (ReservationID)
            // -----------------------------------------------------------------
            String insertResSql = "INSERT INTO Reservations (CustomerID, FlightID, BookingDate, ReservationStatus) VALUES (?, ?, GETDATE(), 'Confirmed')";
            int generatedReservationId = -1;

            // استخدام RETURN_GENERATED_KEYS لجلب رقم المفتاح الأساسي الذي سينتجه السيرفر فوراً لقيد الحجز الحالي
            try (PreparedStatement stmtRes = conn.prepareStatement(insertResSql, Statement.RETURN_GENERATED_KEYS)) {
                stmtRes.setInt(1, customerId);
                stmtRes.setInt(2, currentFlightId);
                stmtRes.executeUpdate();

                try (ResultSet keys = stmtRes.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedReservationId = keys.getInt(1); // جلب معرّف الحجز لاستخدامه في جدول المدفوعات كـ Foreign Key
                    }
                }
            }

            // -----------------------------------------------------------------
            // الخطوة ب: إدراج الفاتورة المالية في جدول المدفوعات (Payments) وربطها برقم الحجز المولد أعلاه
            // -----------------------------------------------------------------
            String insertPaySql = "INSERT INTO Payments (ReservationID, Amount, PaymentDate, PaymentMethod) VALUES (?, ?, GETDATE(), ?)";
            try (PreparedStatement stmtPay = conn.prepareStatement(insertPaySql)) {
                stmtPay.setInt(1, generatedReservationId);
                stmtPay.setDouble(2, currentPrice);
                stmtPay.setString(3, method);
                stmtPay.executeUpdate();
            }

            // -----------------------------------------------------------------
            // الخطوة ج: تحديث المقاعد المتاحة للرحلة وإنقاصها بمقدار مقعد واحد (-1) بشكل متزامن وآمن
            // -----------------------------------------------------------------
            String updateSeatsSql = "UPDATE Flights SET AvailableSeats = AvailableSeats - 1 WHERE FlightID = ? AND AvailableSeats > 0";
            try (PreparedStatement stmtSeats = conn.prepareStatement(updateSeatsSql)) {
                stmtSeats.setInt(1, currentFlightId);
                int rowsUpdated = stmtSeats.executeUpdate();

                // البرمجة الدفاعية: إذا لم يتأثر أي سطر، فهذا يعني أن المقاعد نفدت بالكامل في نفس اللحظة من مسافر آخر
                if (rowsUpdated == 0) {
                    throw new SQLException("عذراً، نفدت المقاعد المتاحة على هذه الرحلة في هذه اللحظة!");
                }
            }

            // 🌟 اعتماد الحفظ النهائي (Commit) في قاعدة البيانات بعد التأكد من نجاح الخطوات الثلاث كاملة وبلا أخطاء
            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "تم الحجز بنجاح", "تهانينا! تم تأكيد حجزك وإصدار تذكرتك بنجاح.");

            // توجيه المسافر إلى لوحة التحكم الخاصة به بعد النجاح
            navigate(event, "/View/PassengerDashboard.fxml", "لوحة تحكم المسافر - حجز الرحلات");

        } catch (SQLException e) {
            // 🌟 التراجع الآمن (Rollback): إلغاء كافة التعديلات والخطوات السابقة فوراً في حال فشل أي جزء من العملية
            // يمنع هذا السلوك حدوث أي اتساق خاطئ للبيانات أو خصم مالي عشوائي
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            showAlert(Alert.AlertType.ERROR, "فشل الحجز", "حدث خطأ أثناء معالجة الحجز: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // إغلاق الاتصال يدوياً وبشكل احترافي في كتلة الـ finally لضمان تحرير موارد الشبكة دائماً
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Cancels the booking flow and navigates the passenger back to their selection dashboard safely.
     */
    @FXML
    void handleCancelBooking(ActionEvent event) {
        navigate(event, "/View/PassengerDashboard.fxml", "لوحة تحكم المسافر - حجز الرحلات");
    }

    /**
     * Shared utility routine handling JavaFX structural stage window scene updates.
     */
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

    /**
     * Dispatches visual alert notification components to keep the customer updated contextually.
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}