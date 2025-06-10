package com.example.physiocare.services;

import com.example.physiocare.models.physio.Physio;
import com.example.physiocare.utils.EmailSender;
import com.example.physiocare.utils.NominaUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NominaSchedulerService {

    public static void startPayrollScheduler(int year, int month) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> sendPayrollEmails(year, month), 0, 30, TimeUnit.DAYS);
    }

    private static void sendPayrollEmails(int year, int month) {
        try {
            List<Physio> physios = (List<Physio>) PhysiosService.getPhysios(null);

            for (Physio physio : physios) {
                if (physio.getUser() != null) {
                    String outputFilePath = "output/" + physio.getName() + "_Payroll_" + month + "_" + year + ".pdf";
                    NominaUtils.generatePayroll(physio.getId(), year, month, outputFilePath);

                    String emailBody = "Dear " + physio.getName() + ",\n\n" +
                            "Please find attached your payroll for " + month + "/" + year + ".\n\n" +
                            "Best regards,\nPhysioCare Team";

                    EmailSender.sendEmail(physio.getEmail(), "Your Payroll for " + month + "/" + year, emailBody, String.valueOf(new File(outputFilePath)));
                } else {
                    System.err.println("No email found for physio: " + physio.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error while sending payroll emails: " + e.getMessage());
        }
    }
}