package com.example.physiocare.controller.appointments;

import com.example.physiocare.models.appointment.Appointment;
import com.example.physiocare.services.AppointmentsService;
import com.example.physiocare.utils.MessageUtils;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ConfirmAppointmentView {
    private final ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private final Gson gson = new Gson();
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

    @FXML
    private void initialize() {
        setupTable();
        loadAppointments();

        // Listener para búsqueda
        txtSearch.textProperty().addListener(
                (observable, oldValue, newValue) -> loadAppointments(newValue.trim()));

        // Deshabilitar botón de confirmar si no hay selección
        tableAppointments.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) ->
                        btnConfirm.setDisable(newSelection == null));
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
        AppointmentsService.getUnconfirmedAppointments(searchQuery)
                .thenAccept(resp -> Platform.runLater(() -> {
                    if (resp == null || !resp.isOk() || resp.getResult() == null) {
                        appointments.clear();
                        MessageUtils.showError("Error", "No unconfirmed appointments found");
                    } else {
                        appointments.setAll(resp.getResult());
                    }
                })).exceptionally(ex -> {
                    Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void handleConfirm() {
        Appointment selectedAppointment = tableAppointments.getSelectionModel().getSelectedItem();

        AppointmentsService.ConfirmAppointments(selectedAppointment).thenAccept(resp -> Platform.runLater(() -> {
            if (resp == null || !resp.isOk()) {
                MessageUtils.showError("Error", "Failed to confirm appointment");
            } else {
                MessageUtils.showMessage("Success", "Appointment confirmed successfully");
                loadAppointments();
            }
        })).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
            return null;
        });
    }

    @FXML
    private void handleCancel() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }
}