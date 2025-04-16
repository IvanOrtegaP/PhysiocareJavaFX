package com.example.physiocare;

import com.example.physiocare.models.physio.Physio;
import com.example.physiocare.models.physio.PhysioListResponse;
import com.example.physiocare.models.physio.PhysioResponse;
import com.example.physiocare.utils.MessageUtils;
import com.example.physiocare.utils.ServiceUtils;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhysioViewController {


    public enum Specialty {
        SPORTS("Sports"),
        NEUROLOGICAL("Neurological"),
        PEDIATRIC("Pediatric"),
        GERIATRIC("Geriatric"),
        ONCOLOGICAL("Oncological");

        private final String displayName;

        Specialty(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public static Specialty fromString(String text) {
            for (Specialty s : Specialty.values()) {
                if (s.displayName.equalsIgnoreCase(text)) {
                    return s;
                }
            }
            return null;
        }
    }

    @FXML private TextField txtName;
    @FXML private TextField txtSurname;
    @FXML private ComboBox<String> cbSpecialty;
    @FXML private TextField txtLicenseNumber;
    @FXML private TextField txtEmail;
    @FXML private TableView<Physio> tblPhysios;
    @FXML private TableColumn<Physio, String> colName;
    @FXML private TableColumn<Physio, String> colSurname;
    @FXML private TableColumn<Physio, String> colSpecialty;
    @FXML private TableColumn<Physio, String> colLicense;
    @FXML private Button btnNew;
    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Label lblMessage;
    @FXML private Button btnRefresh;
    @FXML private TextField txtSearch;

    private ObservableList<Physio> physios = FXCollections.observableArrayList();
    private Physio currentPhysio = null;
    private final Gson gson = new Gson();

    @FXML
    private void initialize() {
        cbSpecialty.getItems().setAll(
                "Sports",
                "Neurological",
                "Pediatric",
                "Geriatric",
                "Oncological"
        );

        setupTable();
        disableForm(true);
        loadPhysios();
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> loadPhysios(newValue.trim()));

    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colSpecialty.setCellValueFactory(new PropertyValueFactory<>("specialty"));
        colLicense.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));

        tblPhysios.setItems(physios);
        tblPhysios.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        currentPhysio = newSelection;
                        populateForm(currentPhysio);
                        btnDelete.setDisable(false);
                    }
                });
    }

    private void loadPhysios() {
        loadPhysios(null);
    }

    private void loadPhysios(String searchQuery) {
        CompletableFuture.supplyAsync(() -> {
            String url;
            if (searchQuery != null && !searchQuery.isEmpty()) {
                url = ServiceUtils.SERVER + "/physios/find?specialty=" + searchQuery;
            } else {
                url = ServiceUtils.SERVER + "/physios";
            }

            String response = ServiceUtils.getResponse(url, null, "GET");
            System.out.println("DEBUG - JSON recibido de " + url + ":\n" + response);

            return gson.fromJson(response, Physio[].class);
        }).thenAccept(resultArray -> {
            Platform.runLater(() -> {
                if (resultArray != null) {
                    physios.setAll(resultArray);
                    clearSelection();
                } else {
                    MessageUtils.showError("Error", "The physios list could not be loaded or no results were found.");
                }
            });
        }).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() ->
                    MessageUtils.showError("Error", "Failed to load physios: " + ex.getMessage()));
            return null;
        });
    }

    private void findOnePhysio(String surname) {
        CompletableFuture.runAsync(() -> {
            String url = ServiceUtils.SERVER + "/physios/find?surname=" + surname;
            String response = ServiceUtils.getResponse(url, null, "GET");

            System.out.println("DEBUG - JSON One physio '" + surname + "':\n" + response);
        }).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() ->
                    MessageUtils.showError("Error", "Failed to fetch: " + ex.getMessage()));
            return null;
        });
    }

    private void findOnePatientRecords(String surname) {
        CompletableFuture.runAsync(() -> {
            String url = ServiceUtils.SERVER + "/records/find?surname=" + surname;
            String response = ServiceUtils.getResponse(url, null, "GET");

            System.out.println("DEBUG - JSON Record from '" + surname + "':\n" + response);
        }).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() ->
                    MessageUtils.showError("Error", "Failed to fetch: " + ex.getMessage()));
            return null;
        });
    }

    @FXML
    private void handleNew() {
        currentPhysio = null;
        clearForm();
        disableForm(false);
        btnDelete.setDisable(true);
    }

    @FXML
    private void handleRefresh() {
        loadPhysios();
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) return;

        Physio physio = new Physio(
                currentPhysio != null ? currentPhysio.getId() : null,
                txtName.getText().trim(),
                txtSurname.getText().trim(),
                cbSpecialty.getValue(),
                txtLicenseNumber.getText().trim(),
                txtEmail.getText().trim()
        );

        final String requestUrl = ServiceUtils.SERVER + "/physios" +
                (currentPhysio != null ? "/" + currentPhysio.getId() : "");
        final String method = currentPhysio == null ? "POST" : "PUT";
        final String jsonRequest = gson.toJson(physio);

        CompletableFuture.supplyAsync(() -> {
            String jsonResponse = ServiceUtils.getResponse(requestUrl, jsonRequest, method);
            System.out.println("Response from server: " + jsonResponse);
            return gson.fromJson(jsonResponse, PhysioResponse.class);
        }).thenAccept(responseData -> {
            Platform.runLater(() -> {
                if (responseData != null && responseData.getId() != null) {
                    MessageUtils.showMessage("Success",
                            "Physio " + (currentPhysio == null ? "created" : "updated") + " successfully");
                    loadPhysios();
                    disableForm(true);
                } else {
                    loadPhysios();
                    disableForm(true);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() ->
                    MessageUtils.showError("Error", "Operation failed: " + ex.getMessage()));
            return null;
        });
    }

    @FXML
    private void handleDelete() {
        if (currentPhysio == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Physio");
        alert.setContentText("Are you sure you want to delete " + currentPhysio.getName() + "?");

        alert.showAndWait().ifPresent(userResponse -> {
            if (userResponse == ButtonType.OK) {
                final String deleteUrl = ServiceUtils.SERVER + "/physios/" + currentPhysio.getId();

                CompletableFuture.supplyAsync(() -> {
                    String responseJson = ServiceUtils.getResponse(deleteUrl, null, "DELETE");
                    return gson.fromJson(responseJson, PhysioResponse.class);
                }).thenAccept(responseData -> {
                    Platform.runLater(() -> {
                        if (responseData != null && responseData.getId() != null) {
                            MessageUtils.showMessage("Success", "Physio deleted successfully");
                            loadPhysios();
                            clearForm();
                            disableForm(true);
                        } else {
                            loadPhysios();
                            clearForm();
                            disableForm(true);
                        }
                    });
                }).exceptionally(ex -> {
                    Platform.runLater(() ->
                            MessageUtils.showError("Error", "Delete failed: " + ex.getMessage()));
                    return null;
                });
            }
        });
    }

    private void populateForm(Physio physio) {
        txtName.setText(physio.getName());
        txtSurname.setText(physio.getSurname());
        cbSpecialty.setValue(physio.getSpecialty());
        txtLicenseNumber.setText(physio.getLicenseNumber());
        txtEmail.setText(physio.getEmail());
    }

    private void clearForm() {
        txtName.clear();
        txtSurname.clear();
        cbSpecialty.getSelectionModel().clearSelection();
        txtLicenseNumber.clear();
        txtEmail.clear();
    }

    private void clearSelection() {
        tblPhysios.getSelectionModel().clearSelection();
        currentPhysio = null;
        btnDelete.setDisable(true);
    }

    private void disableForm(boolean disable) {
        txtName.setDisable(disable);
        txtSurname.setDisable(disable);
        cbSpecialty.setDisable(disable);
        txtLicenseNumber.setDisable(disable);
        txtEmail.setDisable(disable);
        btnSave.setDisable(disable);
    }

    private boolean validateForm() {
        if (txtName.getText().trim().isEmpty() || txtSurname.getText().trim().isEmpty()) {
            MessageUtils.showError("Validation Error", "Name and surname are required");
            return false;
        }

        if (cbSpecialty.getValue() == null || cbSpecialty.getValue().isEmpty()) {
            MessageUtils.showError("Validation Error", "Specialty is required");
            return false;
        }

        String licenseNumber = txtLicenseNumber.getText().trim();
        if (licenseNumber.isEmpty()) {
            MessageUtils.showError("Validation Error", "License number is required");
            return false;
        }
        if (!licenseNumber.matches("^[a-zA-Z0-9]{8}$")) {
            MessageUtils.showError("Validation Error", "License number must be 8 alphanumeric characters");
            return false;
        }

        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            MessageUtils.showError("Validation Error", "Email is required");
            return false;
        }
        Pattern emailPattern = Pattern.compile("^[^\\s@]+@[\\w-]+\\.[a-zA-Z]{2,}$");
        Matcher matcher = emailPattern.matcher(email);
        if (!matcher.matches()) {
            MessageUtils.showError("Validation Error", "Invalid email format");
            return false;
        }

        return true;
    }
}
