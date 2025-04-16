package com.example.physiocare;

import com.example.physiocare.utils.MessageUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import com.example.physiocare.utils.ServiceUtils;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class MainController {




    @FXML
    private Button btnLogout;

    @FXML
    private void handlePatients() throws IOException {
        loadCrudView("patient-view.fxml", "Patient Management");
    }

    @FXML
    private void handlePhysios() throws IOException {
        loadCrudView("physio-view.fxml", "Physiotherapist Management");
    }


//    private void findOnePatient(String surname) {
//        CompletableFuture.runAsync(() -> {
//            String url = ServiceUtils.SERVER + "/patients/find?surname=" + surname;
//            String response = ServiceUtils.getResponse(url, null, "GET");
//
//            System.out.println("DEBUG - JSON One patient '" + surname + "':\n" + response);
//        }).exceptionally(ex -> {
//            ex.printStackTrace();
//            Platform.runLater(() ->
//                    MessageUtils.showError("Error", "Failed to fetch: " + ex.getMessage()));
//            return null;
//        });
//    }
//
//    private void findOnePhysio(String id) {
//        CompletableFuture.runAsync(() -> {
//            String url = ServiceUtils.SERVER + "/physios/" + id;
//            String response = ServiceUtils.getResponse(url, null, "GET");
//
//            System.out.println("DEBUG - JSON One Physio '" + id + "':\n" + response);
//        }).exceptionally(ex -> {
//            ex.printStackTrace();
//            Platform.runLater(() ->
//                    MessageUtils.showError("Error", "Failed to fetch: " + ex.getMessage()));
//            return null;
//        });
//    }
//
//    private void postOneRecord(String id) {
//
//
//        CompletableFuture.runAsync(() -> {
//            String url = ServiceUtils.SERVER + "/physios/" + id;
//            String response = ServiceUtils.getResponse(url, null, "GET");
//
//            System.out.println("DEBUG - JSON One Physio '" + id + "':\n" + response);
//        }).exceptionally(ex -> {
//            ex.printStackTrace();
//            Platform.runLater(() ->
//                    MessageUtils.showError("Error", "Failed to fetch: " + ex.getMessage()));
//            return null;
//        });
//    }
//
//    private void findRecord(String id) {
//        CompletableFuture.runAsync(() -> {
//            String url = ServiceUtils.SERVER + "/records/" + id;
//            String response = ServiceUtils.getResponse(url, null, "GET");
//
//            System.out.println("DEBUG - JSON Record '" + id + "':\n" + response);
//        }).exceptionally(ex -> {
//            ex.printStackTrace();
//            Platform.runLater(() ->
//                    MessageUtils.showError("Error", "Failed to fetch: " + ex.getMessage()));
//            return null;
//        });
//    }

    @FXML
    private void handleLogout() {
        ServiceUtils.removeToken();
        Stage stage = (Stage) btnLogout.getScene().getWindow();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("login-view.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("PhysioCare Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCrudView(String fxmlFile, String title) throws IOException {
        Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
    }


}