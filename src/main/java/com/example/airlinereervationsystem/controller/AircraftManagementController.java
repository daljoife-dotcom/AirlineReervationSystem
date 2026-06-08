package com.example.airlinereervationsystem.controller;

import com.example.airlinereervationsystem.database.DBConnection;
import com.example.airlinereervationsystem.models.Aircraft;
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

public class AircraftManagementController {

    @FXML private TextField txtAircraftModel;
    @FXML private TextField txtTotalCapacity;

    @FXML private TableView<Aircraft> aircraftTable;
    @FXML private TableColumn<Aircraft, Integer> colId;
    @FXML private TableColumn<Aircraft, String> colModel;
    @FXML private TableColumn<Aircraft, Integer> colCapacity;

    private ObservableList<Aircraft> aircraftList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // ربط أعمدة الجدول بخصائص كلاس Aircraft
        colId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAircraftId()));
        colModel.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAircraftModel()));
        colCapacity.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalCapacity()));

        loadAircraftData();
    }

    /**
     * جلب أسطول الطائرات من قاعدة البيانات
     */
    private void loadAircraftData() {
        aircraftList.clear();
        String sql = "SELECT * FROM Aircraft";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                aircraftList.add(new Aircraft(
                        rs.getInt("AircraftID"),
                        rs.getString("AircraftModel"),
                        rs.getInt("TotalCapacity")
                ));
            }
            aircraftTable.setItems(aircraftList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * إضافة طائرة جديدة للأسطول
     */
    @FXML
    void handleAddAircraft(ActionEvent event) {
        String model = txtAircraftModel.getText().trim();
        String capacityStr = txtTotalCapacity.getText().trim();

        if (model.isEmpty() || capacityStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "تنبيه", "يرجى إدخال موديل الطائرة وسعتها الإجمالية!");
            return;
        }

        String sql = "INSERT INTO Aircraft (AircraftModel, TotalCapacity) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, model);
            stmt.setInt(2, Integer.parseInt(capacityStr));
            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "نجاح", "تم إضافة الطائرة الجديدة بنجاح إلى الأسطول.");
            loadAircraftData(); // تحديث الجدول
            txtAircraftModel.clear();
            txtTotalCapacity.clear();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "خطأ", "فشل إضافة الطائرة: " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "خطأ في الإدخال", "يرجى كتابة رقم صحيح في خانة السعة!");
        }
    }

    /**
     * حذف طائرة محددة
     */
    @FXML
    void handleDeleteAircraft(ActionEvent event) {
        Aircraft selected = aircraftTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "تنبيه", "يرجى تحديد الطائرة المراد حذفها من الجدول أولاً!");
            return;
        }

        String sql = "DELETE FROM Aircraft WHERE AircraftID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, selected.getAircraftId());
            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "نجاح", "تم حذف الطائرة بنجاح.");
            loadAircraftData();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "قيد التشغيل", "لا يمكن حذف هذه الطائرة لارتباطها برحلات مجدولة حالياً في النظام!");
        }
    }

    @FXML
    void handleBackToFlights(ActionEvent event) {
        navigate(event, "/View/AdminDashboard.fxml", "لوحة تحكم المسؤول - إدارة الرحلات");
    }

    private void navigate(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
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
