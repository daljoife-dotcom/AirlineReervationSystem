package com.example.airlinereervationsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/LoginView.fxml"));
            Parent root = loader.load();

            primaryStage.setTitle("نظام حجز تذاكر الطيران - تسجيل الدخول");
            primaryStage.setScene(new Scene(root));

            primaryStage.setResizable(false);

            primaryStage.show();

        } catch (IOException e) {
            System.err.println("خطأ حرج: تعذر تحميل شاشة تسجيل الدخول المبدئية Login.fxml");
            System.err.println("تأكد من وجود الملف داخل مسار: src/main/resources/View/LoginView.fxml");
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        // إطلاق تطبيق JavaFX
        launch(args);
    }
}
