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

public class ReservationManagementController {

    @FXML private TableView<ReservationReportRow> globalReservationsTable;
    @FXML private TableColumn<ReservationReportRow, Integer> colResId;
    @FXML private TableColumn<ReservationReportRow, String> colPassengerName;
    @FXML private TableColumn<ReservationReportRow, String> colFlightNum;
    @FXML private TableColumn<ReservationReportRow, Double> colPaidAmount;
    @FXML private TableColumn<ReservationReportRow, String> colPayMethod;
    @FXML private TableColumn<ReservationReportRow, String> colStatus;

    private ObservableList<ReservationReportRow> reportList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // إعداد الأعمدة باستخدام خصائص كلاس التقرير الداخلي
        colResId.setCellValueFactory(cellData -> cellData.getValue().resIdProperty().asObject());
        colPassengerName.setCellValueFactory(cellData -> cellData.getValue().passengerNameProperty());
        colFlightNum.setCellValueFactory(cellData -> cellData.getValue().flightNumProperty());
        colPaidAmount.setCellValueFactory(cellData -> cellData.getValue().paidAmountProperty().asObject());
        colPayMethod.setCellValueFactory(cellData -> cellData.getValue().payMethodProperty());
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        loadReservationsReport();
    }

    /**
     * جلب التقرير المالي والتشغيلي المدمج عبر عمل Inner Joins
     */
    private void loadReservationsReport() {
        reportList.clear();

        String sql = "SELECT r.ReservationID, u.FullName, f.FlightNumber, p.Amount, p.PaymentMethod, r.ReservationStatus " +
                "FROM Reservations r " +
                "INNER JOIN SystemUsers u ON r.CustomerID = u.UserID " +
                "INNER JOIN Flights f ON r.FlightID = f.FlightID " +
                "LEFT JOIN Payments p ON r.ReservationID = p.ReservationID";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reportList.add(new ReservationReportRow(
                        rs.getInt("ReservationID"),
                        rs.getString("FullName"),
                        rs.getString("FlightNumber"),
                        rs.getDouble("Amount"),
                        rs.getString("PaymentMethod") != null ? rs.getString("PaymentMethod") : "N/A",
                        rs.getString("ReservationStatus")
                ));
            }
            globalReservationsTable.setItems(reportList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * إلغاء حجز مسافر وتحديث حالته وتأثيره
     */
    @FXML
    void handleCancelPassengerReservation(ActionEvent event) {
        ReservationReportRow selected = globalReservationsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "يرجى اختيار سجل حجز لإلغائه!", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        String sql = "UPDATE Reservations SET ReservationStatus = 'Cancelled' WHERE ReservationID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, selected.getResId());
            stmt.executeUpdate();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "تم إلغاء حجز الراكب بنجاح وتحديث السيرفر.", ButtonType.OK);
            alert.showAndWait();

            loadReservationsReport(); // إعادة تحميل الجدول

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
     * كلاس داخلي (Helper Data Structure) مخصص فقط لتركيب وعرض صف التقرير المدمج في الجدول
     */
    public static class ReservationReportRow {
        private final SimpleIntegerProperty resId;
        private final SimpleStringProperty passengerName;
        private final SimpleStringProperty flightNum;
        private final SimpleDoubleProperty paidAmount;
        private final SimpleStringProperty payMethod;
        private final SimpleStringProperty status;

        public ReservationReportRow(int resId, String passengerName, String flightNum, double paidAmount, String payMethod, String status) {
            this.resId = new SimpleIntegerProperty(resId);
            this.passengerName = new SimpleStringProperty(passengerName);
            this.flightNum = new SimpleStringProperty(flightNum);
            this.paidAmount = new SimpleDoubleProperty(paidAmount);
            this.payMethod = new SimpleStringProperty(payMethod);
            this.status = new SimpleStringProperty(status);
        }

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
