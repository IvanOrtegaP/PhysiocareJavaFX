package com.example.physiocare;

import com.example.physiocare.models.patient.Patient;
import com.example.physiocare.models.patient.PatientListResponse;
import com.example.physiocare.models.patient.PatientResponse;
import com.example.physiocare.utils.MessageUtils;
import com.example.physiocare.utils.ServiceUtils;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.ZoneId;
import java.util.Date;

public class PatientViewController {
    private final Gson gson = new Gson();
    private final ObservableList<Patient> patients = FXCollections.observableArrayList();
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtSurname;
    @FXML
    private DatePicker dpBirthDate;
    @FXML
    private TextField txtAddress;
    @FXML
    private TextField txtInsuranceNumber;
    @FXML
    private TextField txtEmail;
    @FXML
    private TableView<Patient> tblPatients;
    @FXML
    private TableColumn<Patient, String> colName;
    @FXML
    private TableColumn<Patient, String> colSurname;
    @FXML
    private TableColumn<Patient, String> colInsurance;
    @FXML
    private TableColumn<Patient, String> colEmail;
    @FXML
    private Button btnNew;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnDelete;
    @FXML
    private Label lblMessage;
    @FXML
    private Button btnRefresh;
    @FXML
    private TextField txtSearch;
    private Patient currentPatient = null;

    @FXML
    private void initialize() {
        setupTable();
        disableForm(true);
        loadPatients();
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> loadPatients(newValue.trim()));
    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colInsurance.setCellValueFactory(new PropertyValueFactory<>("insuranceNumber"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        tblPatients.setItems(patients);
        tblPatients.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        currentPatient = newSelection;
                        populateForm(currentPatient);
                        btnDelete.setDisable(false);
                    }
                });
    }

    private void loadPatients() {
        loadPatients(null);
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
                    patients.setAll(response.getPatients());
                    clearSelection();
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
            return null;
        });
    }

    @FXML
    private void handleNew() {
        currentPatient = null;
        clearForm();
        disableForm(false);
        btnDelete.setDisable(true);
    }

    @FXML
    private void handleRefresh() {
        txtSearch.clear();
        loadPatients();
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) return;

        Date date = Date.from(dpBirthDate.getValue().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        Patient patient = new Patient(
                currentPatient != null ? currentPatient.getId() : null,
                txtName.getText().trim(),
                txtSurname.getText().trim(),
                date,
                txtAddress.getText().trim(),
                txtInsuranceNumber.getText().trim(),
                txtEmail.getText().trim()
        );

        final String method = currentPatient == null ? "POST" : "PUT";
        final String url = currentPatient == null
                ? ServiceUtils.SERVER + "/patients"
                : ServiceUtils.SERVER + "/patients/" + currentPatient.getId();


        String jsonRequest = gson.toJson(patient);

        ServiceUtils.getResponseAsync(url, jsonRequest, method).thenApply(json -> gson.fromJson(json,
                PatientResponse.class)).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response != null && response.isOk()) {
                    MessageUtils.showMessage("Success",
                            "Patient " + (currentPatient == null ? "created" : "updated") + " successfully");
                    loadPatients();
                    disableForm(true);
                } else {
                    loadPatients();
                    disableForm(true);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
            return null;
        });
    }

    @FXML
    private void handleDelete() {
        if (currentPatient == null) return;
        String message = "Are you sure you want to delete " + currentPatient.getName() + "?";
        MessageUtils.showConfirmation("Delete Patient", message, "Delete patient").showAndWait()
                .ifPresent(response -> {
                    if (response == ButtonType.OK) {

                        String url = ServiceUtils.SERVER + "/patients/" + currentPatient.getId();

                        ServiceUtils.getResponseAsync(url, null, "DELETE").thenApply(json -> gson.fromJson(json,
                                PatientResponse.class)).thenAccept(responseApi -> {
                            Platform.runLater(() -> {
                                if (responseApi != null && responseApi.isOk()) {
                                    MessageUtils.showMessage("Success", "Patient deleted successfully");
                                    loadPatients();
                                    clearForm();
                                    disableForm(true);
                                } else {
                                    loadPatients();
                                    clearForm();
                                    disableForm(true);
                                }
                            });
                        });
                    }
                });
        loadPatients();
    }

    private void populateForm(Patient patient) {
        System.out.println(patient.getBitrthDate());
        txtName.setText(patient.getName());
        txtSurname.setText(patient.getSurname());
        dpBirthDate.setValue(patient.getBitrthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        txtAddress.setText(patient.getAddress());
        txtInsuranceNumber.setText(patient.getInsuranceNumber());
//        txtEmail.setText(patient.getEmail());
    }

    private void clearForm() {
        txtName.clear();
        txtSurname.clear();
        dpBirthDate.setValue(null);
        txtAddress.clear();
        txtInsuranceNumber.clear();
        txtEmail.clear();
    }

    private void clearSelection() {
        tblPatients.getSelectionModel().clearSelection();
        currentPatient = null;
        btnDelete.setDisable(true);
    }

    private void disableForm(boolean disable) {
        txtName.setDisable(disable);
        txtSurname.setDisable(disable);
        dpBirthDate.setDisable(disable);
        txtAddress.setDisable(disable);
        txtInsuranceNumber.setDisable(disable);
        txtEmail.setDisable(disable);
        btnSave.setDisable(disable);
    }

    private boolean validateForm() {
        if (txtName.getText().trim().isEmpty() || txtSurname.getText().trim().isEmpty()) {
            MessageUtils.showError("Validation Error", "Name and surname are required");
            return false;
        }
        if (dpBirthDate.getValue() == null) {
            MessageUtils.showError("Validation Error", "Birth date is required");
            return false;
        }
        if (txtInsuranceNumber.getText().trim().isEmpty()) {
            MessageUtils.showError("Validation Error", "Insurance number is required");
            return false;
        }
        if (!txtInsuranceNumber.getText().trim().matches("^[a-zA-Z0-9]{9}$")) {
            MessageUtils.showError("Validation Error", "Insurance number must be 9 alphanumeric characters");
            return false;
        }
        if (txtEmail.getText().trim().isEmpty()) {
            MessageUtils.showError("Validation Error", "Email is required");
            return false;
        }
        return true;
    }
}