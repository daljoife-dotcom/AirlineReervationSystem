package com.example.airlinereervationsystem.controller;

import com.example.airlinereervationsystem.database.DBConnection;
import com.example.airlinereervationsystem.models.UserSession;
import com.example.airlinereervationsystem.models.Flight;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;

public class PassengerDashboardController {

    @FXML private Label lblWelcomePassenger;
    @FXML private TextField txtSearchDestination;
    @FXML private DatePicker dpSearchDate;

    @FXML private TableView<Flight> passengerFlightTable;
    @FXML private TableColumn<Flight, String> colFlightNum;
    @FXML private TableColumn<Flight, String> colDestination;
    @FXML private TableColumn<Flight, LocalDateTime> colDate;
    @FXML private TableColumn<Flight, Double> colPrice;
    @FXML private TableColumn<Flight, Integer> colSeats;

    private ObservableList<Flight> flightsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. عرض اسم المسافر الحالي في الترحيب
        if (UserSession.getLoggedInUser() != null) {
            lblWelcomePassenger.setText("مرحباً بك، " + UserSession.getLoggedInUser().getFullName());
        }

        // 2. إعداد أعمدة الجدول وربطها بخصائص كلاس Flight
        colFlightNum.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getFlightNumber()));
        colDestination.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDestination()));
        colDate.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getFlightDate()));
        colPrice.setCellValueFactory(cellData ->new SimpleObjectProperty<>(cellData.getValue().getPrice()));
        colSeats.setCellValueFactory(cellData ->new SimpleObjectProperty<>(cellData.getValue().getAvailableSeats()));

        // 3. جلب البيانات من السيرفر لأول مرة
        loadFlightsData(null, null);

        // 4. جعل الجدول يتحدث تلقائياً أثناء كتابة المسافر في خانة البحث
        txtSearchDestination.textProperty().addListener((observable, oldValue, newValue) -> {
            loadFlightsData(newValue, dpSearchDate.getValue() != null ? Date.valueOf(dpSearchDate.getValue()) : null);
        });
    }

    /**
     * جلب الرحلات من قاعدة البيانات مع دعم الفلترة والبحث
     */
    private void loadFlightsData(String destinationKeyword, Date dateFilter) {
        flightsList.clear();
        StringBuilder query = new StringBuilder("SELECT * FROM Flights WHERE AvailableSeats > 0");

        if (destinationKeyword != null && !destinationKeyword.isEmpty()) {
            query.append(" AND Destination LIKE ?");
        }
        if (dateFilter != null) {
            query.append(" AND CAST(FlightDate AS DATE) = ?");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            if (destinationKeyword != null && !destinationKeyword.isEmpty()) {
                stmt.setString(paramIndex++, "%" + destinationKeyword + "%");
            }
            if (dateFilter != null) {
                stmt.setDate(paramIndex, dateFilter);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    flightsList.add(new Flight(
                            rs.getInt("FlightID"),
                            rs.getString("FlightNumber"),
                            rs.getString("Destination"),
                            rs.getTimestamp("FlightDate").toLocalDateTime(),
                            rs.getDouble("Price"),
                            rs.getInt("AircraftID"),
                            rs.getInt("AvailableSeats")
                    ));
                }
            }
            passengerFlightTable.setItems(flightsList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * فلترة مخصصة عند تغيير التاريخ عبر أداة DatePicker
     */
    @FXML
    void handleDateSearch(ActionEvent event) {
        loadFlightsData(txtSearchDestination.getText(), dpSearchDate.getValue() != null ? Date.valueOf(dpSearchDate.getValue()) : null);
    }

    /**
     * معالجة الضغط على زر "الانتقال لتأكيد الحجز والدفع"
     */
    @FXML
    void handleGoToBooking(ActionEvent event) {
        Flight selectedFlight = passengerFlightTable.getSelectionModel().getSelectedItem();

        if (selectedFlight == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("تنبيه");
            alert.setHeaderText(null);
            alert.setContentText("يرجى اختيار الرحلة المطلوبة من الجدول أولاً للمتابعة!");
            alert.showAndWait();
            return;
        }

        // تفعيل الرابط المشترك: حفظ معرف الرحلة المختار في الجلسة لتستلمه الشاشة القادمة
        UserSession.setSelectedFlightId(selectedFlight.getFlightId());

        // الانتقال لشاشة الفاتورة والدفع
        navigate(event, "/View/BookingPaymentView.fxml", "تأكيد حجز التذكرة وإتمام الدفع المالي");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        UserSession.clearSession();
        navigate(event, "/View/LoginView.fxml", "بوابة الدخول - نظام حجز الطيران");
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
}
