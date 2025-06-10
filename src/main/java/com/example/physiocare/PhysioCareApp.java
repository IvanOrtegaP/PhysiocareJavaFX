package com.example.physiocare;

import com.example.physiocare.services.EmailSchedulerService;
import com.example.physiocare.services.NominaSchedulerService;
import com.example.physiocare.utils.EmailSender;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class PhysioCareApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(PhysioCareApp.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 720, 540);
        stage.setTitle("Physiocare");
        stage.setScene(scene);
        stage.show();

        EmailSchedulerService.startEmailScheduler();
        LocalDate hoy = LocalDate.now();
        System.out.println("hola");
        NominaSchedulerService.startPayrollScheduler(hoy.getYear(), hoy.getMonthValue());
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        EmailSchedulerService.stopEmailScheduler();
    }
}