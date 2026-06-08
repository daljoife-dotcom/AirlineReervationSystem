module com.example.airlinereervationsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.airlinereervationsystem to javafx.fxml;
    opens com.example.airlinereervationsystem.controller to javafx.fxml;
    opens com.example.airlinereervationsystem.database to javafx.fxml;
    opens com.example.airlinereervationsystem.models to javafx.fxml;
    exports com.example.airlinereervationsystem;
}