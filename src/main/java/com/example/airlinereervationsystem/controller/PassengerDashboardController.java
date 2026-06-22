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
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * Controller class in charge of orchestrating the Passenger Dashboard interface.
 * Implements a reactive searching mechanism allowing passengers to filter available flights
 * based on contextual destination keywords and targeted schedules in real-time.
 * <p>
 * <b>Key Software Engineering Concepts Demonstrated:</b>
 * <ul>
 * <li><b>Reactive UI Architecture:</b> Hooks continuous listeners onto text properties
 * to trigger automatic database re-queries as the client types.</li>
 * <li><b>Secure Dynamic SQL Compilation:</b> Leverages {@link StringBuilder} alongside structured
 * {@link PreparedStatement} parameter index offsets to prevent relational SQL Injection vulnerabilities.</li>
 * <li><b>Session-Driven State Propagation:</b> Saves transient workflow identifiers into the global
 * {@link UserSession} context to manage linear navigation streams.</li>
 * </ul>
 *
 * @author Donia Ahmed
 * @version 2.0 (Sprint 2)
 */
public class PassengerDashboardController {

    // ربط عناصر التحكم العلوية وعناصر البحث والتصفية
    @FXML private Label lblWelcomePassenger;
    @FXML private TextField txtSearchDestination;
    @FXML private DatePicker dpSearchDate;

    // ربط جدول عرض الرحلات المتاحة وأعمدته المخصصة لكائن الـ Flight
    @FXML private TableView<Flight> passengerFlightTable;
    @FXML private TableColumn<Flight, String> colFlightNum;
    @FXML private TableColumn<Flight, String> colDestination;
    @FXML private TableColumn<Flight, LocalDateTime> colDate;
    @FXML private TableColumn<Flight, Double> colPrice;
    @FXML private TableColumn<Flight, Integer> colSeats;

    // قائمة مراقبة (ObservableList) لتحديث جدول الرحلات المعروض للمسافر تلقائياً
    private ObservableList<Flight> flightsList = FXCollections.observableArrayList();

    /**
     * Initializes the passenger controller context. Builds columns data factories,
     * pulls default session data, and sets up reactive keyboard input listeners.
     */
    @FXML
    public void initialize() {
        // 1. إدارة الحالة (State Management): تخصيص رسالة الترحيب باسم المسافر الفعلي المسترجع من الجلسة العامة الحالية
        if (UserSession.getLoggedInUser() != null) {
            lblWelcomePassenger.setText("مرحباً بك، " + UserSession.getLoggedInUser().getFullName());
        }

        // 2. ربط أعمدة الجدول بخصائص كائن الـ Flight (Data Binding) عبر التعبيرات اللمداوية
        colFlightNum.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getFlightNumber()));
        colDestination.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDestination()));
        colDate.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getFlightDate()));
        colPrice.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrice()));
        colSeats.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAvailableSeats()));

        // 3. جلب البيانات من السيرفر لأول مرة بدون أي فلاتر (عرض كافة الرحلات المتاحة)
        loadFlightsData(null, null);

        // 4. [برمجة تفاعلية متقدمة - Reactive Listening]:
        // مراقبة خاصية النص لحقل البحث؛ حيث يتم استدعاء دالة التحديث وإعادة الاستعلام من قاعدة البيانات تلقائياً
        // فور قيام المسافر بكتابة أو حذف أي حرف، لتوفر تصفية فورية وسلسة للواجهة.
        txtSearchDestination.textProperty().addListener((observable, oldValue, newValue) -> {
            loadFlightsData(newValue, dpSearchDate.getValue() != null ? Date.valueOf(dpSearchDate.getValue()) : null);
        });
    }

    /**
     * Dynamically compiles a parameterized SQL select block to filter out available flights.
     * Enforces that only flights with vacant capacity are exposed to potential bookings.
     * * @param destinationKeyword the string literal pattern used in the SQL LIKE constraint
     * @param dateFilter         the temporal boundary used to isolate specific travel dates
     */
    private void loadFlightsData(String destinationKeyword, Date dateFilter) {
        flightsList.clear(); // مسح القائمة لمنع تراكم البيانات السابقة

        // البدء بالاستعلام الأساسي الذي يضمن منطق العمل: عدم عرض الرحلات الممتلئة (AvailableSeats > 0)
        StringBuilder query = new StringBuilder("SELECT * FROM Flights WHERE AvailableSeats > 0");

        // بناء الاستعلام ديناميكياً بحسب المدخلات النشطة من المستخدم في الواجهة
        if (destinationKeyword != null && !destinationKeyword.isEmpty()) {
            query.append(" AND Destination LIKE ?");
        }
        if (dateFilter != null) {
            query.append(" AND CAST(FlightDate AS DATE) = ?");
        }

        // استخدام try-with-resources لضمان معالجة استثناءات SQL وغلق قنوات الاتصال تلقائياً
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            // إدارة مؤشرات المعاملات (Parameter Index Orchestration) بشكل ديناميكي آمن لمنع تداخل الحقول
            int paramIndex = 1;
            if (destinationKeyword != null && !destinationKeyword.isEmpty()) {
                stmt.setString(paramIndex++, "%" + destinationKeyword + "%"); // استخدام الرموز البديلة لتنفيذ البحث الجزئي (LIKE %keyword%)
            }
            if (dateFilter != null) {
                stmt.setDate(paramIndex, dateFilter);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // بناء كائنات Flight وإضافتها إلى قائمة المراقبة المعروضة للمستخدم
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
            passengerFlightTable.setItems(flightsList); // ربط القائمة بالجدول التفاعلي في الواجهة
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Triggered event handler mapped to the DatePicker UI component.
     * Re-filters active flight rows based on the chosen chronological date.
     * * @param event the structural action event triggered by shifting the date element
     */
    @FXML
    void handleDateSearch(ActionEvent event) {
        // تحديث البيانات بناءً على التاريخ الجديد المحدد مع الحفاظ على الكلمة المفتاحية للوجهة إن وجدت
        loadFlightsData(txtSearchDestination.getText(), dpSearchDate.getValue() != null ? Date.valueOf(dpSearchDate.getValue()) : null);
    }

    /**
     * Captures the selected table row entity and passes its contextual references
     * onto the central global session scope before pushing scene navigation.
     * * @param event the structural action event triggered by clicking the confirmation button
     */
    @FXML
    void handleGoToBooking(ActionEvent event) {
        // جلب السطر المحدد من قبل المسافر في الجدول
        Flight selectedFlight = passengerFlightTable.getSelectionModel().getSelectedItem();

        // البرمجة الدفاعية: التحقق من قيام المستخدم بالاختيار الفعلي قبل بدء الانتقال، منعاً لحدوث خطأ NullPointerException
        if (selectedFlight == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("تنبيه");
            alert.setHeaderText(null);
            alert.setContentText("يرجى اختيار الرحلة المطلوبة من الجدول أولاً للمتابعة!");
            alert.showAndWait();
            return;
        }

        // تفعيل الرابط المشترك (State Management): حفظ معرّف الرحلة المختار في الجلسة العامة لتستلمه الشاشة المالية اللاحقة بدقة
        UserSession.setSelectedFlightId(selectedFlight.getFlightId());

        // التنقل الآمن لشاشة الحجز وإصدار الفاتورة والدفع الإلكتروني
        navigate(event, "/View/BookingPaymentView.fxml", "تأكيد حجز التذكرة وإتمام الدفع المالي");
    }

    /**
     * Resets active user metrics inside the global context and routes the view back to the authentication screen.
     * * @param event the structural action event triggered by clicking the Logout button
     */
    @FXML
    void handleLogout(ActionEvent event) {
        UserSession.clearSession(); // تفريغ بيانات الجلسة الساكنة لضمان سرية وأمان البيانات عند المغادرة
        navigate(event, "/View/LoginView.fxml", "بوابة الدخول - نظام حجز الطيران");
    }

    /**
     * Routine handling structural JavaFX scene-swapping by loading targeted FXML files.
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
}