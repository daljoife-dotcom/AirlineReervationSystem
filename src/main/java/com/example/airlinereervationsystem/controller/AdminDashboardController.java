package com.example.airlinereervationsystem.controller;

import com.example.airlinereervationsystem.database.DBConnection;
import com.example.airlinereervationsystem.models.Aircraft;
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

public class AdminDashboardController {

    @FXML private TableView<Flight> adminFlightTable;
    @FXML private TableColumn<Flight, String> colFlightNum;
    @FXML private TableColumn<Flight, String> colDestination;
    @FXML private TableColumn<Flight, LocalDateTime> colDate;
    @FXML private TableColumn<Flight, Double> colPrice;
    @FXML private TableColumn<Flight, Integer> colSeats;
    //colAircraftId

    @FXML private TextField txtAdminFlightNum;
    @FXML private TextField txtAdminDestination;
    @FXML private TextField txtAdminPrice;
    @FXML private DatePicker dpAdminDate;
    @FXML private ComboBox<Aircraft> cbSelectAircraft; // لعرض قائمة أرقام الطائرات المتاحة

    private ObservableList<Flight> allFlights = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // إعداد أعمدة الجدول
        colFlightNum.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getFlightNumber()));
        colDestination.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDestination()));
        colDate.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getFlightDate()));
        colPrice.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrice()));
        colSeats.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAvailableSeats()));

        // تحميل البيانات
        loadAllFlights();
        loadAircrafts();

        // مستمع نقرات الفأرة على الجدول: عند اختيار رحلة يتم تعبئة الحقول تلقائياً لتسهيل التعديل
        adminFlightTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtAdminFlightNum.setText(newSelection.getFlightNumber());
                txtAdminDestination.setText(newSelection.getDestination());
                txtAdminPrice.setText(String.valueOf(newSelection.getPrice()));
                dpAdminDate.setValue(newSelection.getFlightDate().toLocalDate());
                cbSelectAircraft.getSelectionModel().select(Integer.valueOf(newSelection.getAircraftId()));
            }
        });
    }

    private void loadAllFlights() {
        allFlights.clear();
        String sql = "SELECT * FROM Flights";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                allFlights.add(new Flight(
                        rs.getInt("FlightID"),
                        rs.getString("FlightNumber"),
                        rs.getString("Destination"),
                        rs.getTimestamp("FlightDate").toLocalDateTime(),
                        rs.getDouble("Price"),
                        rs.getInt("AircraftID"),
                        rs.getInt("AvailableSeats")
                ));
            }
            adminFlightTable.setItems(allFlights);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAircrafts() {
        ObservableList<Aircraft> aircraftList = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Aircraft"; // جلب السطر بالكامل (المعرف، الموديل، السعة)

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // إضافة كائن الطائرة بالكامل إلى القائمة
                aircraftList.add(new Aircraft(
                        rs.getInt("AircraftID"),
                        rs.getString("AircraftModel"),
                        rs.getInt("TotalCapacity")
                ));
            }

            cbSelectAircraft.setItems(aircraftList);

            // ✨ هنا السحر: إخبار الـ ComboBox أن يعرض نص الموديل فقط في الواجهة
            cbSelectAircraft.setConverter(new javafx.util.StringConverter<Aircraft>() {
                @Override
                public String toString(Aircraft aircraft) {
                    // إذا كان الكائن ليس فارغاً، اعرض الموديل، وإلا اعرض نصاً فارغاً
                    return aircraft == null ? "" : aircraft.getAircraftModel();
                }

                @Override
                public Aircraft fromString(String string) {
                    return null; // لا نحتاجها لأن القائمة للقراءة والاختيار فقط
                }
            });

            if (!aircraftList.isEmpty()) {
                cbSelectAircraft.getSelectionModel().selectFirst();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleAddFlight(ActionEvent event) {
        String num = txtAdminFlightNum.getText().trim();
        String dest = txtAdminDestination.getText().trim();
        String priceStr = txtAdminPrice.getText().trim();

        if (num.isEmpty() || dest.isEmpty() || priceStr.isEmpty() || dpAdminDate.getValue() == null || cbSelectAircraft.getValue() == null) {
            showAlert("تنبيه", "يرجى ملء كافة تفاصيل الرحلة الجديدة!");
            return;
        }
        Aircraft selectedAircraft = cbSelectAircraft.getValue();
        if (selectedAircraft == null) {
            showAlert("تنبيه", "يرجى اختيار طائرة من القائمة!");
            return;
        }
        // جلب السعة الكلية للطائرة المحددة تلقائياً لتكون هي عدد المقاعد المتاحة في البداية
        int aircraftId = selectedAircraft.getAircraftId();
        int capacity = 150; // سعة افتراضية في حال عدم التمكن من جلبها
        String capSql = "SELECT TotalCapacity FROM Aircraft WHERE AircraftID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmtCap = conn.prepareStatement(capSql)) {
            stmtCap.setInt(1, aircraftId);
            try (ResultSet rs = stmtCap.executeQuery()) {
                if (rs.next()) capacity = rs.getInt("TotalCapacity");
            }
        } catch (SQLException e) { e.printStackTrace(); }

        String insertSql = "INSERT INTO Flights (FlightNumber, Destination, FlightDate, Price, AircraftID, AvailableSeats) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {

            stmt.setString(1, num);
            stmt.setString(2, dest);
            stmt.setTimestamp(3, Timestamp.valueOf(dpAdminDate.getValue().atStartOfDay()));
            stmt.setDouble(4, Double.parseDouble(priceStr));
            stmt.setInt(5, aircraftId);
            stmt.setInt(6, capacity);

            stmt.executeUpdate();
            loadAllFlights();
            clearFields();
        } catch (SQLException e) {
            showAlert("خطأ في الإدخال", "فشل إضافة الرحلة: " + e.getMessage());
        }
    }

    @FXML
    void handleUpdateFlight(ActionEvent event) {
        Flight selected = adminFlightTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("تنبيه", "اختر الرحلة المراد تعديلها من الجدول أولاً!");
            return;
        }
        Aircraft selectedAircraft = cbSelectAircraft.getValue();
        if (selectedAircraft == null) {
            showAlert("تنبيه", "يرجى اختيار طائرة من القائمة!");
            return;
        }
        String updateSql = "UPDATE Flights SET FlightNumber=?, Destination=?, FlightDate=?, Price=?, AircraftID=? WHERE FlightID=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {

            stmt.setString(1, txtAdminFlightNum.getText());
            stmt.setString(2, txtAdminDestination.getText());
            stmt.setTimestamp(3, Timestamp.valueOf(dpAdminDate.getValue().atStartOfDay()));
            stmt.setDouble(4, Double.parseDouble(txtAdminPrice.getText()));
            stmt.setInt(5, selectedAircraft.getAircraftId());
            stmt.setInt(6, selected.getFlightId());

            stmt.executeUpdate();
            loadAllFlights();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleDeleteFlight(ActionEvent event) {
        Flight selected = adminFlightTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("تنبيه", "اختر الرحلة المراد حذفها نهائياً!");
            return;
        }

        String deleteSql = "DELETE FROM Flights WHERE FlightID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setInt(1, selected.getFlightId());
            stmt.executeUpdate();
            loadAllFlights();
            clearFields();
        } catch (SQLException e) {
            showAlert("قيد الحجز", "لا يمكن حذف الرحلة لأن هناك ركاب قاموا بحجز مقاعد عليها بالفعل!");
        }
    }

    @FXML void handleNavToAircrafts(ActionEvent event) { navigate(event, "/View/AircraftManagementView.fxml", "إدارة أسطول الطائرات"); }
    @FXML void handleNavToReservations(ActionEvent event) { navigate(event, "/View/ReservationManagementView.fxml", "شاشة مراقبة المدفوعات المالية"); }
    @FXML void handleAdminLogout(ActionEvent event) { UserSession.clearSession(); navigate(event, "/View/LoginView.fxml", "بوابة الدخول"); }

    private void clearFields() {
        txtAdminFlightNum.clear();
        txtAdminDestination.clear();
        txtAdminPrice.clear();
        dpAdminDate.setValue(null);
    }

    private void navigate(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
