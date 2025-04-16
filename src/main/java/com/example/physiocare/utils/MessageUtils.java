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
}
