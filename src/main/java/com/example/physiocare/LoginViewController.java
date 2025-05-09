package com.example.physiocare;

import com.example.physiocare.utils.MessageUtils;
import com.example.physiocare.utils.ServiceUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

        ServiceUtils.loginAsync(username, password)
                .thenAccept(success -> {
                    if (success) {
                        Platform.runLater(() -> {
                            try {
                                loadMainView();
                            } catch (IOException e) {
                                MessageUtils.showError("Error", "Failed to load application");
                            }
                        });
                    } else {
                        Platform.runLater(() ->
                                lblMessage.setText("Invalid credentials"));
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            lblMessage.setText("Login failed: " + ex.getMessage()));
                    return null;
                });
    }

    private void loadMainView() throws IOException {
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("main-view.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle("PhysioCare - Main Menu");
        stage.show();
    }
}