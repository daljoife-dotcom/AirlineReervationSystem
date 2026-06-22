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
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

/**
 * Controller class responsible for managing the Aircraft Fleet inventory.
 * Provides user interface interactions for listing, adding, and removing aircraft items
 * while maintaining strict database integrity constraints.
 * <p>
 * <b>Key Software Engineering Concepts Demonstrated:</b>
 * <ul>
 * <li><b>Defensive Programming:</b> Validates fields and captures multiple specific exception types
 * (e.g., parsing errors vs. database communication faults).</li>
 * <li><b>Reactive Data Synchronization:</b> Connects UI components dynamically to model alterations using {@link ObservableList}.</li>
 * <li><b>Referential Integrity Safeguarding:</b> Gracefully catches foreign key constraint violations during deletions.</li>
 * </ul>
 *
 * @author Donia Ahmed
 * @version 2.0 (Sprint 2)
 */
public class AircraftManagementController {

    // ربط عناصر حقول الإدخال النصية لبيانات الطائرة
    @FXML private TextField txtAircraftModel;
    @FXML private TextField txtTotalCapacity;

    // ربط مكونات الجدول (TableView) وأعمدته المخصصة لكائن الـ Aircraft
    @FXML private TableView<Aircraft> aircraftTable;
    @FXML private TableColumn<Aircraft, Integer> colId;
    @FXML private TableColumn<Aircraft, String> colModel;
    @FXML private TableColumn<Aircraft, Integer> colCapacity;

    // قائمة مراقبة (ObservableList) لإدارة تحديث بيانات الجدول فورياً عند التعديل أو الإضافة
    private ObservableList<Aircraft> aircraftList = FXCollections.observableArrayList();

    /**
     * Initializes the controller layer automatically after the FXML markup is loaded.
     * Sets up UI data binding properties and initiates data fetching routines.
     */
    @FXML
    public void initialize() {
        // ربط أعمدة الجدول بخصائص كلاس Aircraft باستخدام خاصية الأغراض البسيطة (SimpleObjectProperty) والتعبيرات اللمداوية
        colId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAircraftId()));
        colModel.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAircraftModel()));
        colCapacity.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalCapacity()));

        // استدعاء دالة تحميل البيانات الأولية لملء الجدول فور فتح الشاشة
        loadAircraftData();
    }

    /**
     * Queries the underlying database for all recorded aircraft fleet rows and maps them to the visual table component.
     */
    private void loadAircraftData() {
        aircraftList.clear(); // تنظيف القائمة لمنع تكرار البيانات المضافة سابقاً عند التحديث
        String sql = "SELECT * FROM Aircraft";

        // استخدام try-with-resources لإغلاق الاتصال التلقائي والحفاظ على أداء السيرفر
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // بناء كائن الطائرة من البيانات القادمة من قاعدة البيانات وإضافته إلى قائمة المراقبة
                aircraftList.add(new Aircraft(
                        rs.getInt("AircraftID"),
                        rs.getString("AircraftModel"),
                        rs.getInt("TotalCapacity")
                ));
            }
            aircraftTable.setItems(aircraftList); // ربط القائمة المحدثة بالجدول المعروض للمستخدم

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Validates client inputs and dispatches a secure parametrized SQL command to insert a new aircraft into the system.
     * Uses defensive catch routing for database and format exceptions.
     * * @param event the structural UI context triggering ActionEvent
     */
    @FXML
    void handleAddAircraft(ActionEvent event) {
        String model = txtAircraftModel.getText().trim();
        String capacityStr = txtTotalCapacity.getText().trim();

        // 1. التحقق من المدخلات (Input Validation): التحقق من الحقول الفارغة قبل الاتصال بقاعدة البيانات
        if (model.isEmpty() || capacityStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "تنبيه", "يرجى إدخال موديل الطائرة وسعتها الإجمالية!");
            return;
        }

        String sql = "INSERT INTO Aircraft (AircraftModel, TotalCapacity) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, model);
            // تحويل النص المدخل إلى رقم صحيح
            stmt.setInt(2, Integer.parseInt(capacityStr));
            stmt.executeUpdate(); // تنفيذ استعلام الإدخال

            showAlert(Alert.AlertType.INFORMATION, "نجاح", "تم إضافة الطائرة الجديدة بنجاح إلى الأسطول.");
            loadAircraftData(); // إعادة تحميل البيانات لتحديث الجدول فورياً في الواجهة

            // تنظيف حقول الإدخال لتسهيل الاستخدام التالي (UX Maintenance)
            txtAircraftModel.clear();
            txtTotalCapacity.clear();

        } catch (SQLException e) {
            // معالجة استثناءات فشل الاتصال أو القيود داخل قاعدة البيانات
            showAlert(Alert.AlertType.ERROR, "خطأ", "فشل إضافة الطائرة: " + e.getMessage());
        } catch (NumberFormatException e) {
            // البرمجة الدفاعية: التقاط خطأ التحويل النصي لمنع انهيار النظام إذا أدخل المستخدم حروفاً في حقل السعة الرقمي
            showAlert(Alert.AlertType.ERROR, "خطأ في الإدخال", "يرجى كتابة رقم صحيح في خانة السعة!");
        }
    }

    /**
     * Deletes a targeted aircraft row from the database based on the TableView active selection.
     * Enforces foreign key mapping restrictions safely.
     * * @param event the structural UI context triggering ActionEvent
     */
    @FXML
    void handleDeleteAircraft(ActionEvent event) {
        // جلب كائن الطائرة المحدد بواسطة المستخدم من الجدول
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
            loadAircraftData(); // تحديث القائمة بعد الحذف الناجح

        } catch (SQLException e) {
            // معالجة تكامل البيانات المرجعية (Referential Integrity): منع الحذف إذا كانت الطائرة مرتبطة برحلات (Foreign Key Constraint)
            showAlert(Alert.AlertType.ERROR, "قيد التشغيل", "لا يمكن حذف هذه الطائرة لارتباطها برحلات مجدولة حالياً في النظام!");
        }
    }

    /**
     * Switches the active scene back to the main administrative dashboard.
     */
    @FXML
    void handleBackToFlights(ActionEvent event) {
        navigate(event, "/View/AdminDashboard.fxml", "لوحة تحكم المسؤول - إدارة الرحلات");
    }

    /**
     * Utility method handling generic scene swaps and FXML loading.
     */
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

    /**
     * Serves uniform messaging panels to the administrative client interface.
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}