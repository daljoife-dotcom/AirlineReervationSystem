package com.example.airlinereervationsystem.controller;

import com.example.airlinereervationsystem.database.DBConnection;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
 * Controller class managing the global Financial Monitoring and Operational Auditing Dashboard.
 * Consolidates distributed business data across multiple entities into unified tabular reporting views.
 * <p>
 * <b>Key Software Engineering Concepts Demonstrated:</b>
 * <ul>
 * <li><b>Multi-Table Relational Joins:</b> Compiles cross-domain metrics by merging four tables
 * (Reservations, Users, Flights, Payments) via advanced SQL INNER and LEFT JOIN statements.</li>
 * <li><b>Data Transfer Object (DTO) Pattern:</b> Utilizes a tailored static nested class
 * {@link ReservationReportRow} to aggregate decoupled attributes into an immutable presentation model.</li>
 * <li><b>Defensive Null Safeguarding:</b> Sanitizes nullable currency fields generated from
 * outer relational boundaries before inflating active JavaFX properties.</li>
 * </ul>
 *
 * @author Donia Ahmed
 * @version 2.0 (Sprint 2)
 */
public class ReservationManagementController {

    // ربط جدول عرض التقارير الشاملة وأعمدته المخصصة لكائن التقرير الهجين
    @FXML private TableView<ReservationReportRow> globalReservationsTable;
    @FXML private TableColumn<ReservationReportRow, Integer> colResId;
    @FXML private TableColumn<ReservationReportRow, String> colPassengerName;
    @FXML private TableColumn<ReservationReportRow, String> colFlightNum;
    @FXML private TableColumn<ReservationReportRow, Double> colPaidAmount;
    @FXML private TableColumn<ReservationReportRow, String> colPayMethod;
    @FXML private TableColumn<ReservationReportRow, String> colStatus;

    // قائمة مراقبة (ObservableList) لتحديث جدول التقارير المالية والتشغيلية فورياً عند أي تغيير في السيرفر
    private ObservableList<ReservationReportRow> reportList = FXCollections.observableArrayList();

    /**
     * Initializes the reporting controller layer dynamically. Maps heterogeneous
     * property attributes to structural TableColumn nodes and triggers relational database syncing.
     */
    @FXML
    public void initialize() {
        // إعداد وتوصيل أعمدة الجدول بخصائص كلاس التقرير الداخلي (Data Binding)
        // استخدام .asObject() لتحويل الخصائص الرقمية البدائية بسلاسة إلى كائنات ملائمة لأعمدة الجدول
        colResId.setCellValueFactory(cellData -> cellData.getValue().resIdProperty().asObject());
        colPassengerName.setCellValueFactory(cellData -> cellData.getValue().passengerNameProperty());
        colFlightNum.setCellValueFactory(cellData -> cellData.getValue().flightNumProperty());
        colPaidAmount.setCellValueFactory(cellData -> cellData.getValue().paidAmountProperty().asObject());
        colPayMethod.setCellValueFactory(cellData -> cellData.getValue().payMethodProperty());
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // استدعاء دالة بناء التقرير المدمج فور تحميل الشاشة
        loadReservationsReport();
    }

    /**
     * Executes a composite SQL query utilizing multi-table joins to harvest
     * financial transactions and passenger assignments into a single synchronized dataset.
     */
    private void loadReservationsReport() {
        reportList.clear(); // تنظيف الذاكرة المؤقتة لمنع تكرار السطور عند إعادة التحميل

        // 🌟 [هندسة برمجية متقدمة]: استعلام مركب يدمج 4 جداول مختلفة لبناء تقرير مالي وتشغيلي موحد لمدير النظام
        String sql = "SELECT r.ReservationID, u.FullName, f.FlightNumber, p.Amount, p.PaymentMethod, r.ReservationStatus " +
                "FROM Reservations r " +
                "INNER JOIN SystemUsers u ON r.CustomerID = u.UserID " +
                "INNER JOIN Flights f ON r.FlightID = f.FlightID " +
                "LEFT JOIN Payments p ON r.ReservationID = p.ReservationID"; // استخدام LEFT JOIN لضمان ظهور الحجوزات حتى لو لم يتم دفعها بعد

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // البرمجة الدفاعية: التحقق من طريقة الدفع لتجنب استلام قيم فارغة (Null Values) واستبدالها بنص توضيحي "N/A"
                reportList.add(new ReservationReportRow(
                        rs.getInt("ReservationID"),
                        rs.getString("FullName"),
                        rs.getString("FlightNumber"),
                        rs.getDouble("Amount"),
                        rs.getString("PaymentMethod") != null ? rs.getString("PaymentMethod") : "N/A",
                        rs.getString("ReservationStatus")
                ));
            }
            globalReservationsTable.setItems(reportList); // ربط البيانات المركبة بالواجهة الرسومية

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates a targeted passenger reservation record state to 'Cancelled' via a persistent database call.
     * * @param event the structural UI context triggering ActionEvent
     */
    @FXML
    void handleCancelPassengerReservation(ActionEvent event) {
        // جلب سطر التقرير المحدد من قبل مدير النظام
        ReservationReportRow selected = globalReservationsTable.getSelectionModel().getSelectedItem();

        // التحقق الحمائي (Input Validation): منع التعديل في حال عدم اختيار أي حجز من الجدول
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "يرجى اختيار سجل حجز لإلغائه!", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        String sql = "UPDATE Reservations SET ReservationStatus = 'Cancelled' WHERE ReservationID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, selected.getResId());
            stmt.executeUpdate(); // تنفيذ أمر تحديث الحالة في قاعدة البيانات

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "تم إلغاء حجز الراكب بنجاح وتحديث السيرفر.", ButtonType.OK);
            alert.showAndWait();

            loadReservationsReport(); // إعادة بناء وتحميل التقرير فورياً ليعكس الحالة الجديدة بالجدول

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the visual stage frame context back to the primary administrative flight operations control room.
     * * @param event the structural UI context triggering ActionEvent
     */
    @FXML
    void handleBackFromReservations(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/AdminDashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("لوحة تحكم المسؤول - إدارة الرحلات");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Static nested helper class acting as a structural Data Transfer Object (DTO).
     * Tailored strictly to map and transform composite query attributes into observable properties
     * bound directly into the parent JavaFX TableView container.
     */
    public static class ReservationReportRow {
        private final SimpleIntegerProperty resId;
        private final SimpleStringProperty passengerName;
        private final SimpleStringProperty flightNum;
        private final SimpleDoubleProperty paidAmount;
        private final SimpleStringProperty payMethod;
        private final SimpleStringProperty status;

        /**
         * Core model wrapper constructor configuring standalone UI reactive variables.
         */
        public ReservationReportRow(int resId, String passengerName, String flightNum, double paidAmount, String payMethod, String status) {
            this.resId = new SimpleIntegerProperty(resId);
            this.passengerName = new SimpleStringProperty(passengerName);
            this.flightNum = new SimpleStringProperty(flightNum);
            this.paidAmount = new SimpleDoubleProperty(paidAmount);
            this.payMethod = new SimpleStringProperty(payMethod);
            this.status = new SimpleStringProperty(status);
        }

        // دوال جلب القيم (Getters) ودوال جلب الخصائص التفاعلية لربط أعمدة الـ JavaFX
        public int getResId() { return resId.get(); }
        public SimpleIntegerProperty resIdProperty() { return resId; }
        public String getPassengerName() { return passengerName.get(); }
        public SimpleStringProperty passengerNameProperty() { return passengerName; }
        public String getFlightNum() { return flightNum.get(); }
        public SimpleStringProperty flightNumProperty() { return flightNum; }
        public double getPaidAmount() { return paidAmount.get(); }
        public SimpleDoubleProperty paidAmountProperty() { return paidAmount; }
        public String getPayMethod() { return payMethod.get(); }
        public SimpleStringProperty payMethodProperty() { return payMethod; }
        public String getStatus() { return status.get(); }
        public SimpleStringProperty statusProperty() { return status; }
    }
}