package com.example.physiocare.services;

import com.example.physiocare.models.BaseResponse;
import com.example.physiocare.models.appointment.Appointment;
import com.example.physiocare.models.record.Record;
import com.example.physiocare.models.user.User;
import com.example.physiocare.utils.EmailSender;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import jakarta.mail.internet.MimeMessage;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.physiocare.utils.EmailSender.*;

public class EmailSchedulerService {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void startEmailScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Starting scheduled email reminders...");

                final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                Credential credential = EmailSender.getCredentials(HTTP_TRANSPORT);


                Gmail service = new Gmail.Builder(HTTP_TRANSPORT, EmailSender.JSON_FACTORY, credential)
                        .setApplicationName(EmailSender.APPLICATION_NAME)
                        .build();

                PatientsService.getPatientsMoreInfoRecords().thenAccept(resp -> Platform.runLater(() -> {
                    if(resp != null && resp.isOk() && resp.getPatients() != null){
                        resp.getPatients().forEach(patient -> {
                            sendReminders(patient, service);
                        });
                    }else {
                        String error = Optional.ofNullable(resp).map(BaseResponse::getErrorMessage).orElse("Unknow error");
                        System.out.println(error);
                    }
                })).exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });

//                System.out.println("Email reminders sent successfully.");
            } catch (Exception e) {
                System.err.println("Error while sending email reminders: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 24, TimeUnit.HOURS);
    }

    public static void stopEmailScheduler() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}