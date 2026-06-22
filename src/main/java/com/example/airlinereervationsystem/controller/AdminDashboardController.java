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
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * Controller class managing the Administrative Dashboard interface.
 * Implements full CRUD functionality (Create, Read, Update, Delete) for flight operations
 * while interacting dynamically with the database layer and enforcing referential integrity.
 * <p>
 * <b>Key Software Engineering Concepts Demonstrated:</b>
 * <ul>
 * <li><b>Full CRUD Implementation:</b> Encapsulates logic for adding, reading, updating, and deleting flight schedules.</li>
 * <li><b>Data Binding & Reactive UI:</b> Maps domain models directly to {@link TableView} components via observable data streams.</li>
 * <li><b>UI Controls Customization:</b> Uses a custom {@link javafx.util.StringConverter} to bind whole complex object entities into a ComboBox dropdown.</li>
 * <li><b>Database Constraint Handling:</b> Intercepts SQL exceptions gracefully to protect business logic (e.g., flight booking locking).</li>
 * </ul>
 *
 * @author Donia Ahmed
 * @version 2.0 (Sprint 2)
 */
public class AdminDashboardController {

    // ربط عناصر جدول عرض الرحلات (JavaFX TableView Components)
    @FXML private TableView<Flight> adminFlightTable;
    @FXML private TableColumn<Flight, String> colFlightNum;
    @FXML private TableColumn<Flight, String> colDestination;
    @FXML private TableColumn<Flight, LocalDateTime> colDate;
    @FXML private TableColumn<Flight, Double> colPrice;
    @FXML private TableColumn<Flight, Integer> colSeats;

    // ربط حقول المدخلات وعناصر التحكم الخاصة بمدير النظام
    @FXML private TextField txtAdminFlightNum;
    @FXML private TextField txtAdminDestination;
    @FXML private TextField txtAdminPrice;
    @FXML private DatePicker dpAdminDate;
    @FXML private ComboBox<Aircraft> cbSelectAircraft; // استقبال كائن الطائرة بالكامل لربطه بالرحلة

    // قائمة مراقبة (ObservableList) لتحديث الجدول فورياً عند أي تعديل في البيانات دون الحاجة لإعادة تشغيل الشاشة
    private ObservableList<Flight> allFlights = FXCollections.observableArrayList();

    /**
     * Initializes the dashboard controller automatically upon view loading.
     * Maps table columns, fetches data from the database, and registers reactive row listeners.
     */
    @FXML
    public void initialize() {
        // 1. إعداد أعمدة الجدول وربطها بخصائص كائن الـ Flight (Data Binding)
        colFlightNum.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getFlightNumber()));
        colDestination.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDestination()));
        colDate.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getFlightDate()));
        colPrice.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrice()));
        colSeats.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAvailableSeats()));

        // 2. تحميل البيانات الأولية للرحلات والطائرات من قاعدة البيانات عند فتح الشاشة
        loadAllFlights();
        loadAircrafts();

        // 3. برمجة مستمع الأحداث (Reactive Selection Listener):
        // عند نقر المسؤول على أي سطر في الجدول، يتم ملء حقول الإدخال تلقائياً ببيانات الرحلة المحددة لتسهيل التعديل أو الحذف (UX Enhancement)
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

    /**
     * Fetches all registered flight records from the database and updates the TableView container.
     * Uses safe transactional statement pooling.
     */
    private void loadAllFlights() {
        allFlights.clear(); // تنظيف القائمة الحالية لمنع التكرار
        String sql = "SELECT * FROM Flights";

        // استخدام try-with-resources لضمان الإغلاق التلقائي للموارد وحماية ذاكرة النظام
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // قراءة السطور وتحويلها لكائنات Flight وإضافتها للقائمة المشاهدة
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
            adminFlightTable.setItems(allFlights); // ربط البيانات بالجدول في الواجهة
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Populates the Aircraft ComboBox dropdown menu and configures a custom StringConverter
     * to smoothly bind complex database objects to UI text representations.
     */
    private void loadAircrafts() {
        ObservableList<Aircraft> aircraftList = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Aircraft";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // إضافة كائن الطائرة بالكامل (المعرف، الموديل، السعة) لخدمة العمليات البرمجية لاحقاً
                aircraftList.add(new Aircraft(
                        rs.getInt("AircraftID"),
                        rs.getString("AircraftModel"),
                        rs.getInt("TotalCapacity")
                ));
            }

            cbSelectAircraft.setItems(aircraftList);

            // الهندسة البرمجية للـ ComboBox: إخبار المكون أن يعرض "اسم موديل الطائرة" فقط للمستخدم
            // مع الاحتفاظ بكائن الطائرة الفعلي ومعرّفها (ID) مخفياً في الخلفية لخدمة العلاقات بين الجداول.
            cbSelectAircraft.setConverter(new javafx.util.StringConverter<Aircraft>() {
                @Override
                public String toString(Aircraft aircraft) {
                    return aircraft == null ? "" : aircraft.getAircraftModel();
                }

                @Override
                public Aircraft fromString(String string) {
                    return null; // قائمة خيارات مغلقة للقراءة فقط، فلا داعي للتحويل العكسي
                }
            });

            if (!aircraftList.isEmpty()) {
                cbSelectAircraft.getSelectionModel().selectFirst();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Validates and executes a SQL INSERT command to add a new flight record.
     * Automatically syncs initial seating capacity to the chosen aircraft configuration.
     * * @param event the triggered ActionEvent from the UI button
     */
    @FXML
    void handleAddFlight(ActionEvent event) {
        String num = txtAdminFlightNum.getText().trim();
        String dest = txtAdminDestination.getText().trim();
        String priceStr = txtAdminPrice.getText().trim();

        // التحقق من تعبئة جميع الحقول لمنع إرسال قيم غير صالحة لقاعدة البيانات
        if (num.isEmpty() || dest.isEmpty() || priceStr.isEmpty() || dpAdminDate.getValue() == null || cbSelectAircraft.getValue() == null) {
            showAlert("تنبيه", "يرجى ملء كافة تفاصيل الرحلة الجديدة!");
            return;
        }

        Aircraft selectedAircraft = cbSelectAircraft.getValue();
        if (selectedAircraft == null) {
            showAlert("تنبيه", "يرجى اختيار طائرة من القائمة!");
            return;
        }

        // جلب السعة الكلية للطائرة المحددة ديناميكياً لتحديد عدد المقاعد المتاحة (Available Seats) للرحلة الجديدة
        int aircraftId = selectedAircraft.getAircraftId();
        int capacity = 150; // قيمة احترازية/افتراضية في حال تعذر القراءة
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
            stmt.setInt(6, capacity); // تعيين المقاعد المتاحة مساوية لسعة الطائرة كاملة عند الإنشاء

            stmt.executeUpdate();
            loadAllFlights(); // تحديث الجدول فورياً
            clearFields();     // مسح الحقول لتسهيل العملية التالية
        } catch (SQLException e) {
            showAlert("خطأ في الإدخال", "فشل إضافة الرحلة: " + e.getMessage());
        }
    }

    /**
     * Executes an SQL UPDATE command to modify an existing flight record selected in the table.
     * * @param event the triggered ActionEvent from the UI button
     */
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
            stmt.setInt(6, selected.getFlightId()); // الاعتماد على المعرّف الفريد لتوجيه التعديل بدقة

            stmt.executeUpdate();
            loadAllFlights();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes an SQL DELETE command to permanently drop a flight schedule.
     * Enforces database referential integrity constraint checks.
     * * @param event the triggered ActionEvent from the UI button
     */
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
            // معالجة ذكية للقيود (Foreign Key Constraint Violation):
            // التقاط الاستثناء عند رفض قاعدة البيانات للحذف لمنع انهيار البرنامج وتوضيح السبب للمسؤول.
            showAlert("قيد الحجز", "لا يمكن حذف الرحلة لأن هناك ركاب قاموا بحجز مقاعد عليها بالفعل!");
        }
    }

    // دوال التنقل وإدارة الشاشات عبر لوحة التحكم الخاصة بالمسؤول
    @FXML void handleNavToAircrafts(ActionEvent event) { navigate(event, "/View/AircraftManagementView.fxml", "إدارة أسطول الطائرات"); }
    @FXML void handleNavToReservations(ActionEvent event) { navigate(event, "/View/ReservationManagementView.fxml", "شاشة مراقبة المدفوعات المالية"); }

    /**
     * Logs the current administrator out of the active global session state and returns to login gate.
     */
    @FXML void handleAdminLogout(ActionEvent event) {
        UserSession.clearSession(); // تفريغ بيانات الجلسة الساكنة لضمان الأمان وحماية البيانات
        navigate(event, "/View/LoginView.fxml", "بوابة الدخول");
    }

    /**
     * Utility function to clear form text-fields after operation execution.
     */
    private void clearFields() {
        txtAdminFlightNum.clear();
        txtAdminDestination.clear();
        txtAdminPrice.clear();
        dpAdminDate.setValue(null);
    }

    /**
     * Shared utility routine handling JavaFX structural scene-switches.
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
     * Displays information dialogues to keep the operational admin well-informed.
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}