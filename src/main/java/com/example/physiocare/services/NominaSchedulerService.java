package com.example.physiocare.services;

import com.example.physiocare.models.BaseResponse;
import com.example.physiocare.models.physio.Physio;
import com.example.physiocare.models.user.User;
import com.example.physiocare.utils.EmailSender;
import com.example.physiocare.utils.NominaUtils;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import jakarta.mail.internet.MimeMessage;
import javafx.application.Platform;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.physiocare.utils.EmailSender.*;

public class NominaSchedulerService {

    public static void startPayrollScheduler(int year, int month) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> sendPayrollEmails(year, month), 0, 30, TimeUnit.DAYS);
    }

    private static void sendPayrollEmails(int year, int month) {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = EmailSender.getCredentials(HTTP_TRANSPORT);


            Gmail service = new Gmail.Builder(HTTP_TRANSPORT, EmailSender.JSON_FACTORY, credential)
                    .setApplicationName(EmailSender.APPLICATION_NAME)
                    .build();

            PhysiosService.getPhysiosMore().thenAccept(resp -> Platform.runLater(() -> {
                if(resp != null && resp.isOk() && resp.getPhysios() != null){
                    resp.getPhysios().forEach(physio -> {
                        System.out.println("hola");
                        String email = Optional.ofNullable(physio.getUser()).map(User::getEmail).orElse(null);
                        if(email != null){
                            try {
                                String emailBody = "Dear " + physio.getName() + ",\n\n" +
                                        "Please find attached your payroll for " + month + "/" + year + ".\n\n" +
                                        "Best regards,\nPhysioCare Team";

                                MimeMessage emailContent = createEmailWithAttachment(email,
                                        "Ivbegon04@gmail.com",
                                        "Your Payroll for " + month + "/" + year,
                                        emailBody,
                                        null
                                );
                                sendMessage(service, "me", emailContent);
                            }catch (Exception e ){
                                e.printStackTrace();
                            }
                        }
                    });
                }else {
                    String error = Optional.ofNullable(resp).map(BaseResponse::getErrorMessage).orElse("Unknow error");
                    System.out.println(error);
                }
            })).exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });

//            List<Physio> physios = (List<Physio>) PhysiosService.getPhysios(null);
//
//            for (Physio physio : physios) {
//                if (physio.getUser() != null) {
//                    String outputFilePath = "output/" + physio.getName() + "_Payroll_" + month + "_" + year + ".pdf";
//                    NominaUtils.generatePayroll(physio.getId(), year, month, outputFilePath);
//
//                    String emailBody = "Dear " + physio.getName() + ",\n\n" +
//                            "Please find attached your payroll for " + month + "/" + year + ".\n\n" +
//                            "Best regards,\nPhysioCare Team";
//
//
//
//                    EmailSender.sendEmail(physio.getEmail(), "Your Payroll for " + month + "/" + year, emailBody, String.valueOf(new File(outputFilePath)));
//                } else {
//                    System.err.println("No email found for physio: " + physio.getName());
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error while sending payroll emails: " + e.getMessage());
        }
    }


}