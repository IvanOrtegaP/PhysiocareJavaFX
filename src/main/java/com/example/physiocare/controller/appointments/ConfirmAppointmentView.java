package com.example.physiocare.controller.appointments;

import com.example.physiocare.models.appointment.Appointment;
import com.example.physiocare.models.appointment.AppoinmentListResponse;
import com.example.physiocare.utils.MessageUtils;
import com.example.physiocare.utils.ServiceUtils;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class ConfirmAppointmentView {
    @FXML
    public TextField txtSearch;
    @FXML
    public Button btnConfirm;
    @FXML
    public Button btnCancel;
    @FXML
    public TableView<Appointment> tableAppointments;
    @FXML
    public TableColumn<Appointment, String> colDate;
    @FXML
    public TableColumn<Appointment, String> colPatient;
    @FXML
    public TableColumn<Appointment, Boolean> colConfirmed;

    private final ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private final Gson gson = new Gson();

    @FXML
    private void initialize() {
        setupTable();
        loadAppointments();

        // Listener para búsqueda
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> loadAppointments(newValue.trim()));

        // Deshabilitar botón de confirmar si no hay selección
        tableAppointments.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnConfirm.setDisable(newSelection == null);
        });
    }

    private void setupTable() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colConfirmed.setCellValueFactory(new PropertyValueFactory<>("confirm"));

        tableAppointments.setItems(appointments);
    }

    private void loadAppointments() {
        loadAppointments(null);
    }

    private void loadAppointments(String searchQuery) {
        String url = ServiceUtils.SERVER + "/records/appointments/unconfirmed";

        if (searchQuery != null && !searchQuery.isEmpty()) {
            url += "?search=" + searchQuery;
        }

        ServiceUtils.getResponseAsync(url, null, "GET").thenApply(json -> gson.fromJson(json, AppoinmentListResponse.class))
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response == null || !response.isOk() || response.getResult() == null) {
                        appointments.clear();
                        MessageUtils.showError("Error", "No unconfirmed appointments found");
                    } else {
                        appointments.setAll(response.getResult());
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void handleConfirm() {
        Appointment selectedAppointment = tableAppointments.getSelectionModel().getSelectedItem();
        if (selectedAppointment == null) {
            MessageUtils.showError("Error", "No appointment selected");
            return;
        }

        String url = ServiceUtils.SERVER + "/records/" + selectedAppointment.getRecordId() +
                "/appointments/" + selectedAppointment.getId() + "/confirm";

        ServiceUtils.getResponseAsync(url, null, "PUT").thenAccept(response -> Platform.runLater(() -> {
            if (response == null || !response.contains("ok") || !response.contains("true")) {
                MessageUtils.showError("Error", "Failed to confirm appointment");
            } else {
                MessageUtils.showMessage("Success", "Appointment confirmed successfully");
                loadAppointments();
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
            return null;
        });
    }

    @FXML
    private void handleCancel() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }
}