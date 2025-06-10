package com.example.physiocare.services;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EmailSchedulerServiceTest {

    public static void main(String[] args) {
        ScheduledExecutorService testScheduler = Executors.newScheduledThreadPool(1);

        testScheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Testing scheduled email reminders...");

                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "TOKEN");
                headers.put("Content-Type", "application/json");

                PatientsService.fetchPatientsWithUserWithHeaders(headers);
                PatientsService.sendReminders();
                System.out.println("Test email reminders sent successfully.");
            } catch (Exception e) {
                System.err.println("Error during test email reminders: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS);

        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            testScheduler.shutdown();
            System.out.println("Test completed. Scheduler stopped.");
        }, 30, TimeUnit.SECONDS);
    }
}