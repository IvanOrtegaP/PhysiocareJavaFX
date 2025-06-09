package com.example.physiocare;

import com.example.physiocare.services.EmailSchedulerService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PhysioCareApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        EmailSchedulerService.startEmailScheduler();
        FXMLLoader fxmlLoader = new FXMLLoader(PhysioCareApp.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 720, 540);
        stage.setTitle("Physiocare");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}