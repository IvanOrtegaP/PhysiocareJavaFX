package com.example.physiocare.utils;

import javafx.scene.control.Alert;

public class MessageUtils
{
    public static void showError(String header, String message)
    {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setHeaderText(header);
        error.setContentText(message);
        error.showAndWait();
    }

    public static void showMessage(String header, String message)
    {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setHeaderText(header);
        info.setContentText(message);
        info.showAndWait();
    }

    public static Alert showConfirmation(String header, String message, String title){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        return  alert;
    }

    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
