package com.example.physiocare.controller;

import com.example.physiocare.models.patient.Patient;
import com.example.physiocare.models.patient.PatientListResponse;
import com.example.physiocare.utils.MessageUtils;
import com.example.physiocare.utils.ServiceUtils;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class PatientsViewController implements Initializable {
    public Label lblLoggedUser;
    public TextField txtSearch;
    public Button btnAdd;
    public Button btnRefresh;
    public Button btnEdit;
    public Button btnDelete;
    public TableView<Patient> tblPatients;
    public TableColumn colName;
    public TableColumn colSurname;
    public TableColumn<Patient, Date> colBirthDate;
    public TableColumn colEmail;
    private Gson gson = new Gson();
    private final ObservableList<Patient> patients = FXCollections.observableArrayList();
    private Patient currentPatient = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadPatients();
        lblLoggedUser.setText("Welcome , " + ServiceUtils.getLogin());
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> loadPatients(newValue.trim()));
    }

    private void loadPatients() {
        loadPatients(null);
    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colBirthDate.setCellValueFactory(new PropertyValueFactory<>("birthDate"));

        tblPatients.setItems(patients);
        tblPatients.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        currentPatient = newSelection;
//                        populateForm(currentPatient);
                        btnDelete.setDisable(false);
                    }
                });
    }

    private void loadPatients(String searchQuery) {

        String url = ServiceUtils.SERVER + (searchQuery != null && !searchQuery.isEmpty()
                ? "/patients/find?surname=" + searchQuery
                : "/patients");

//        System.out.println("DEBUG - Respuesta JSON: " + response);

        ServiceUtils.getResponseAsync(url, null, "GET").thenApply(json -> gson.fromJson(json,
                PatientListResponse.class)).thenAccept(response -> {
            Platform.runLater(() -> {

                if (response == null || !response.isOk()) {
                    System.out.println("No se encontraron pacientes o hubo un error");
                    patients.clear();
                    MessageUtils.showError("Error", "The patient list could not be loaded or no results were found");
                } else {
                    System.out.println(response.getPatients().size());
                    patients.setAll(response.getPatients());
                    clearSelection();
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
            return null;
        });
    }

    private void clearSelection() {
        tblPatients.getSelectionModel().clearSelection();
        currentPatient = null;
        btnDelete.setDisable(true);
    }


    public void handleRefresh(ActionEvent actionEvent) {
    }

    public void handleViewPhysios(ActionEvent actionEvent) {
    }

    public void handleViewPatients(ActionEvent actionEvent) {
    }

    public void handleViewProfile(ActionEvent actionEvent) {
    }

    public void handleLogout(ActionEvent actionEvent) {
    }

    public void handleAddPatient(ActionEvent actionEvent) {
    }

    public void handleEditPatient(ActionEvent actionEvent) {
    }

    public void handleDeletePatient(ActionEvent actionEvent) {
    }


}
