package com.example.physiocare.services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EmailSchedulerService {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void startEmailScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Starting scheduled email reminders...");
                PatientsService.sendReminders();
                System.out.println("Email reminders sent successfully.");
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