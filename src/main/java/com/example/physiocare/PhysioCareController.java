package com.example.physiocare;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import com.example.physiocare.models.patient.Patient;
import com.example.physiocare.models.patient.PatientListResponse;
import com.example.physiocare.models.patient.PatientResponse;
import com.example.physiocare.utils.MessageUtils;
import com.example.physiocare.utils.ServiceUtils;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.ResourceBundle;

public class PhysioCareController implements Initializable {
    @FXML private TextField txtLogin;
    @FXML private TextField txtPassword;
    @FXML private TextField txtName;
    @FXML private TextField txtSurname;
    @FXML private TextField txtBirthDate;
    @FXML private TextField txtAddress;
    @FXML private TextField txtInsuranceNumber;
    @FXML private TextField txtEmail;
    @FXML private Button btDelete;
    @FXML private Button btPut;
    @FXML private Button btPost;
    @FXML private ListView<Patient> lsPatients;

    private final Gson gson = new Gson();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing PhysioCareController...");
        setupPatientSelectionListener();
        handleLogin();
    }

    private void setupPatientSelectionListener() {
        System.out.println("Setting up patient selection listener...");
        lsPatients.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends Patient> observable, Patient oldValue, Patient newValue) -> {
                    System.out.println("Patient selection changed");
                    if (newValue != null) {
                        System.out.println("Selected patient: " + newValue.getName());
                        populatePatientFields(newValue);
                        enableEditAndDeleteButtons(true);
                    } else {
                        System.out.println("Patient selection cleared");
                        clearPatientFields();
                        enableEditAndDeleteButtons(false);
                    }
                });
    }

    private void populatePatientFields(Patient patient) {
        System.out.println("Populating fields for patient: " + patient.getId());
        txtName.setText(patient.getName());
        txtSurname.setText(patient.getSurname());
        txtBirthDate.setText(patient.getBirthDate().toString());
        txtAddress.setText(patient.getAddress());
        txtInsuranceNumber.setText(patient.getInsuranceNumber());
    }

    private void clearPatientFields() {
        System.out.println("Clearing patient fields");
        txtName.clear();
        txtSurname.clear();
        txtBirthDate.clear();
        txtAddress.clear();
        txtInsuranceNumber.clear();
    }

    private void enableEditAndDeleteButtons(boolean enable) {
        System.out.println("Setting edit/delete buttons to: " + enable);
        btPut.setDisable(!enable);
        btDelete.setDisable(!enable);
    }

    private void handleLogin() {
        String username = txtLogin.getText().trim();
        String password = txtPassword.getText().trim();

        System.out.println("Attempting login for user: " + username);

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Login failed - empty credentials");
            MessageUtils.showError("Login Error", "Username and password are required");
            return;
        }

        ServiceUtils.loginAsync(username, password)
                .thenAccept(success -> {
                    if (success) {
                        System.out.println("Login successful for user: " + username);
                        Platform.runLater(this::loadPatients);
                    } else {
                        System.out.println("Login failed - invalid credentials");
                        Platform.runLater(() ->
                                MessageUtils.showError("Login Failed", "Invalid credentials"));
                    }
                })
                .exceptionally(ex -> {
                    System.out.println("Login error: " + ex.getMessage());
                    Platform.runLater(() ->
                            MessageUtils.showError("Login Error", "Failed to login: " + ex.getMessage()));
                    return null;
                });
    }

    private void loadPatients() {
        System.out.println("Loading patients list...");
        String url = ServiceUtils.SERVER + "/patients";

        ServiceUtils.getResponseAsync(url, null, "GET")
                .thenApply(json -> {
                    System.out.println("Received patients data: " + json);
                    return gson.fromJson(json, PatientListResponse.class);
                })
                .thenAccept(response -> {
                    if (response != null && !response.isError()) {
                        int patientCount = response.getPatients() != null ? response.getPatients().length : 0;
                        System.out.println("Successfully loaded " + patientCount + " patients");

                        Platform.runLater(() -> {
                            if (response.getPatients() != null) {
                                lsPatients.getItems().setAll(Arrays.asList(response.getPatients()));
                            } else {
                                lsPatients.getItems().clear();
                            }
                            lsPatients.getSelectionModel().clearSelection();
                        });
                    } else {
                        String errorMessage = response == null ? "Null response received" : response.getErrorMessage();
                        System.out.println("Error loading patients: " + errorMessage);
                        Platform.runLater(() ->
                                MessageUtils.showError("Load Error", errorMessage));
                    }
                })
                .exceptionally(ex -> {
                    System.out.println("Exception loading patients: " + ex.getMessage());
                    Platform.runLater(() ->
                            MessageUtils.showError("Error", "Failed to load patients: " + ex.getMessage()));
                    return null;
                });
    }


    @FXML
    private void handleAddPatient(ActionEvent event) {
        System.out.println("Add patient button clicked");
        try {
            Patient newPatient = createPatientFromFields();
            System.out.println("Creating new patient: " + newPatient.getName());
            if (validatePatient(newPatient)) {
                createPatient(newPatient);
            }
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format entered");
            MessageUtils.showError("Invalid Date", "Please enter a valid date (YYYY-MM-DD)");
        } catch (Exception e) {
            System.out.println("Error creating patient: " + e.getMessage());
            MessageUtils.showError("Error", "Invalid patient data: " + e.getMessage());
        }
    }

    private Patient createPatientFromFields() throws DateTimeParseException {
        return new Patient(
                null,
                txtName.getText().trim(),
                txtSurname.getText().trim(),
                LocalDate.parse(txtBirthDate.getText().trim()),
                txtAddress.getText().trim(),
                txtInsuranceNumber.getText().trim(),
                txtEmail.getText().trim()
        );
    }

    private boolean validatePatient(Patient patient) {
        if (patient.getName().isEmpty() || patient.getSurname().isEmpty()) {
            System.out.println("Validation failed - name/surname empty");
            MessageUtils.showError("Validation Error", "Name and surname are required");
            return false;
        }
        if (patient.getInsuranceNumber().isEmpty()) {
            System.out.println("Validation failed - insurance number empty");
            MessageUtils.showError("Validation Error", "Insurance number is required");
            return false;
        }
        return true;
    }

    private void createPatient(Patient patient) {
        System.out.println("Sending create request for patient: " + patient.getName());
        btPost.setDisable(true);
        String url = ServiceUtils.SERVER + "/patients";
        String jsonRequest = gson.toJson(patient);
        System.out.println("Request JSON: " + jsonRequest);

        ServiceUtils.getResponseAsync(url, jsonRequest, "POST")
                .thenApply(json -> {
                    System.out.println("Create response: " + json);
                    return gson.fromJson(json, PatientResponse.class);
                })
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        btPost.setDisable(false);
                        if (!response.isError()) {
                            System.out.println("Patient created successfully: " + response.getPatient().getId());
                            MessageUtils.showMessage("Success",
                                    "Patient created: " + response.getPatient().getName());
                            loadPatients();
                            clearPatientFields();
                        } else {
                            System.out.println("Error creating patient: " + response.getErrorMessage());
                            MessageUtils.showError("Create Error", response.getErrorMessage());
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btPost.setDisable(false);
                        System.out.println("Exception creating patient: " + ex.getMessage());
                        MessageUtils.showError("Error", "Failed to create patient: " + ex.getMessage());
                    });
                    return null;
                });
    }

    @FXML
    private void handleUpdatePatient(ActionEvent event) {
        System.out.println("Update patient button clicked");
        Patient selected = lsPatients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            System.out.println("No patient selected for update");
            MessageUtils.showError("Error", "No patient selected");
            return;
        }

        try {
            System.out.println("Updating patient ID: " + selected.getId());
            updatePatientFromFields(selected);
            if (validatePatient(selected)) {
                updatePatient(selected);
            }
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format entered for update");
            MessageUtils.showError("Invalid Date", "Please enter a valid date (YYYY-MM-DD)");
        } catch (Exception e) {
            System.out.println("Error updating patient: " + e.getMessage());
            MessageUtils.showError("Error", "Invalid patient data: " + e.getMessage());
        }
    }

    private void updatePatientFromFields(Patient patient) throws DateTimeParseException {
        patient.setName(txtName.getText().trim());
        patient.setSurname(txtSurname.getText().trim());
        patient.setBirthDate(LocalDate.parse(txtBirthDate.getText().trim()));
        patient.setAddress(txtAddress.getText().trim());
        patient.setInsuranceNumber(txtInsuranceNumber.getText().trim());
        patient.setEmail(txtEmail.getText().trim());
    }

    private void updatePatient(Patient patient) {
        System.out.println("Sending update for patient ID: " + patient.getId());
        btPut.setDisable(true);
        String url = ServiceUtils.SERVER + "/patients/" + patient.getId();
        String jsonRequest = gson.toJson(patient);
        System.out.println("Update request JSON: " + jsonRequest);

        ServiceUtils.getResponseAsync(url, jsonRequest, "PUT")
                .thenApply(json -> {
                    System.out.println("Update response: " + json);
                    return gson.fromJson(json, PatientResponse.class);
                })
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        btPut.setDisable(false);
                        if (!response.isError()) {
                            System.out.println("Patient updated successfully");
                            MessageUtils.showMessage("Success",
                                    "Patient updated: " + response.getPatient().getName());
                            loadPatients();
                        } else {
                            System.out.println("Error updating patient: " + response.getErrorMessage());
                            MessageUtils.showError("Update Error", response.getErrorMessage());
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btPut.setDisable(false);
                        System.out.println("Exception updating patient: " + ex.getMessage());
                        MessageUtils.showError("Error", "Failed to update patient: " + ex.getMessage());
                    });
                    return null;
                });
    }

    @FXML
    private void handleDeletePatient(ActionEvent event) {
        System.out.println("Delete patient button clicked");
        Patient selected = lsPatients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            System.out.println("No patient selected for deletion");
            MessageUtils.showError("Error", "No patient selected");
            return;
        }

        System.out.println("Attempting to delete patient ID: " + selected.getId());
        deletePatient(selected);
    }

    private void deletePatient(Patient patient) {
        System.out.println("Processing delete for patient: " + patient.getId());
        btDelete.setDisable(true);
        String url = ServiceUtils.SERVER + "/patients/" + patient.getId();

        ServiceUtils.getResponseAsync(url, null, "DELETE")
                .thenApply(json -> {
                    System.out.println("Delete response: " + json);
                    return gson.fromJson(json, PatientResponse.class);
                })
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        btDelete.setDisable(false);
                        if (!response.isError()) {
                            System.out.println("Patient deleted successfully");
                            MessageUtils.showMessage("Success",
                                    "Patient deleted: " + response.getPatient().getName());
                            loadPatients();
                            clearPatientFields();
                        } else {
                            System.out.println("Error deleting patient: " + response.getErrorMessage());
                            MessageUtils.showError("Delete Error", response.getErrorMessage());
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btDelete.setDisable(false);
                        System.out.println("Exception deleting patient: " + ex.getMessage());
                        MessageUtils.showError("Error", "Failed to delete patient: " + ex.getMessage());
                    });
                    return null;
                });
    }
}