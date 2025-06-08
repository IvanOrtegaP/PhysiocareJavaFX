package com.example.physiocare.controller.options;

import com.example.physiocare.utils.MedicalRecordExporter;
import com.example.physiocare.utils.MessageUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;

public class OptionsViewController {

    public void handleGenerateReports(ActionEvent actionEvent) {
    }

    public void handleGeneratePayrolls(ActionEvent actionEvent) {
    }

    public void handleSendEmails(ActionEvent actionEvent) {
    }

    public void handleUploadFiles(ActionEvent actionEvent) {
        new Thread(() -> {
            try {
                MedicalRecordExporter.exportMedicalRecords("output/records");
                Platform.runLater(() -> MessageUtils.showMessage("Success", "Medical records exported successfully!"));
            } catch (Exception e) {
                Platform.runLater(() -> MessageUtils.showError("Error", "Failed to export medical records: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }
}
