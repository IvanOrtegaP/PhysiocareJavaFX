package com.example.physiocare;

import com.example.physiocare.utils.MessageUtils;
import com.example.physiocare.utils.ScreenUtils;
import com.example.physiocare.utils.ServiceUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginViewController {
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblMessage;

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Username and password are required");
            return;
        }

        ServiceUtils.loginAsync(username, password).thenAccept(success ->
                Platform.runLater(() -> {
                    if (success) {
                        try {
                            Stage stage = (Stage) txtUsername.getScene().getWindow();
                            String role = ServiceUtils.getRol();
                            if (role != null) {
                                if (role.equals("admin")) {
                                    ScreenUtils.loadView(stage, "/com/example/physiocare/patients/PatientsView.fxml",
                                            "PhysioCare - Main Menu");
                                } else if (role.equals("physio")) {
                                    ScreenUtils.loadViewWithRole(stage, "/com/example/physiocare/patients/PatientsView.fxml",
                                            "PhysioCare - Physio Dashboard", role);
                                } else {
                                    lblMessage.setText("Unknown role: " + role);
                                }
                            }
                        } catch (IOException e) {
                            MessageUtils.showError("Error", "Failed to load application");
                            e.printStackTrace();
                        }
                    } else {
                        Platform.runLater(() -> lblMessage.setText("Invalid credentials"));
                    }
                })).exceptionally(ex -> {
                    Platform.runLater(() ->
                            lblMessage.setText("Login failed: " + ex.getMessage()));
                    return null;
                });
    }
}