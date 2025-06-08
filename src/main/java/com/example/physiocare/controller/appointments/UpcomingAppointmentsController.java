package com.example.physiocare.controller.appointments;

import com.example.physiocare.models.appointment.Appointment;
import com.example.physiocare.models.appointment.AppointmentResponse;
import com.example.physiocare.models.physio.Physio;
import com.example.physiocare.models.physio.PhysioListResponse;
import com.example.physiocare.models.record.Record;
import com.example.physiocare.utils.MessageUtils;
import com.example.physiocare.utils.PhysioListDeserializer;
import com.example.physiocare.utils.ServiceUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class UpcomingAppointmentsController implements Initializable {

    public enum OperationType {
        VIEW, CREATE, EDIT
    }

    @FXML
    private DatePicker dpDate;
    @FXML
    private ChoiceBox<String> cbTime;
    @FXML
    private ChoiceBox<Physio> cbPhysio;
    @FXML
    private TextArea taDiagnosis;
    @FXML
    private TextArea taTreatment;
    @FXML
    private TextArea taObservations;
    @FXML
    private Button buttClose;
    @FXML
    private Button buttDelete;
    @FXML
    private Button buttSave;
    @FXML
    private Label lblDiagnosis;
    @FXML
    private Label lblTreatment;
    @FXML
    private Label lblObservations;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(List.class, new PhysioListDeserializer())
            .create();
    private Appointment appointment;
    private OperationType operationType;
    private Record showRecord;
    private boolean addRecord = false;
    private final List<String> hoursList = List.of("09:00", "10:00", "11:00", "12:00", "13:00", "15:00", "16:00");

    public void setAddRecord(boolean add) {
        this.addRecord = add;
    }

    public void setShowRecord(Record showRecord) {
        this.showRecord = showRecord;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cbTime.setItems(FXCollections.observableList(hoursList));
        cbTime.getSelectionModel().selectFirst();
        loadPhysios();

        if (addRecord) {
            hideFields();
        }
    }

    public void configureModal(OperationType operationType, Appointment appointment) {
        this.operationType = operationType;
        this.appointment = appointment;

        switch (operationType) {
            case VIEW:
                disableForm(true);
                buttDelete.setVisible(false);
                buttSave.setVisible(false);
                populateForm(appointment);
                break;
            case CREATE:
                disableForm(false);
                buttDelete.setVisible(false);
                break;
            case EDIT:
                disableForm(false);
                populateForm(appointment);
                break;
        }
    }

    private void hideFields() {
        taDiagnosis.setDisable(true);
        taDiagnosis.setVisible(false);
        lblDiagnosis.setVisible(false);

        taObservations.setDisable(true);
        taObservations.setVisible(false);
        lblObservations.setVisible(false);

        taTreatment.setDisable(true);
        taTreatment.setVisible(false);
        lblTreatment.setVisible(false);
    }

    private void disableForm(boolean disable) {
        dpDate.setDisable(disable);
        cbTime.setDisable(disable);
        cbPhysio.setDisable(disable);
        taDiagnosis.setDisable(disable);
        taTreatment.setDisable(disable);
        taObservations.setDisable(disable);
    }

    private void populateForm(Appointment appointment) {
        if (appointment != null) {
            dpDate.setValue(appointment.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            cbPhysio.setValue(appointment.getPhysio());
            taDiagnosis.setText(appointment.getDiagnosis());
            taTreatment.setText(appointment.getTreatment());
            taObservations.setText(appointment.getObservations());
        }
    }

    private void loadPhysios() {
        String url = ServiceUtils.SERVER + "/physios";
        ServiceUtils.getResponseAsync(url, null, "GET")
                .thenApply(json -> gson.fromJson(json, PhysioListResponse.class))
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response == null || !response.isOk()) {
                        MessageUtils.showError("Error", "The physios not found");
                    } else {
                        cbPhysio.setItems(FXCollections.observableList(response.getPhysios()));
                        if (!response.getPhysios().isEmpty()) {
                            cbPhysio.getSelectionModel().select(response.getPhysios().getFirst());
                        }
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> MessageUtils.showError("Error", "Failed to load physios: " + ex.getMessage()));
                    ex.printStackTrace();
                    return null;
                });
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) return;

        Date date = createDate();
        Physio selectedPhysio = cbPhysio.getSelectionModel().getSelectedItem();

        String url = ServiceUtils.SERVER;
        if (operationType == OperationType.CREATE) {
            url += "/records/" + showRecord.getId() + "/appointments";
            appointment = new Appointment(date, selectedPhysio, "pending");
        } else if (operationType == OperationType.EDIT) {
            url += "/records/" + showRecord.getId() + "/appointments/" + appointment.getId();
            appointment.setDate(date);
            appointment.setPhysio(selectedPhysio);
        }

        String requestAppointment = gson.toJson(appointment);
        String method = operationType == OperationType.CREATE ? "POST" : "PUT";

        ServiceUtils.getResponseAsync(url, requestAppointment, method)
                .thenApply(json -> gson.fromJson(json, AppointmentResponse.class))
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response == null || !response.isOk()) {
                        MessageUtils.showError("Error", "Failed to save appointment");
                    } else {
                        MessageUtils.showMessage("Success", "Appointment saved successfully");
                        closeModal();
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void handleDelete() {
        if (appointment == null || showRecord == null) return;

        String message = "Are you sure you want to delete this appointment?";
        MessageUtils.showConfirmation("Delete Appointment", message, "Delete Appointment")
                .showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        deleteAppointment();
                    }
                });
    }

    private void deleteAppointment() {
        String url = ServiceUtils.SERVER + "/records/" + showRecord.getId() + "/appointments/" + appointment.getId();

        ServiceUtils.getResponseAsync(url, null, "DELETE")
                .thenApply(json -> gson.fromJson(json, AppointmentResponse.class))
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response != null && response.isOk()) {
                        MessageUtils.showMessage("Success", "Appointment deleted successfully");
                        closeModal();
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> MessageUtils.showError("Error deleting appointment", ex.getLocalizedMessage()));
                    return null;
                });
    }

    @FXML
    private void handleClose() {
        closeModal();
    }

    private void closeModal() {
        ((Stage) dpDate.getScene().getWindow()).close();
    }

    private Date createDate() {
        LocalDate localDate = dpDate.getValue();
        String selectedTime = cbTime.getValue();
        String[] timeParts = selectedTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        LocalDateTime dateTime = localDate.atTime(hour, minute);

        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private boolean validateForm() {
        if (dpDate.getValue() == null) {
            MessageUtils.showError("Validation Error", "Date is required");
            return false;
        }

        if (dpDate.getValue().isBefore(LocalDate.now())) {
            MessageUtils.showError("Validation Error", "The date must be greater than today's date");
            return false;
        }

        if (cbPhysio.getSelectionModel().getSelectedItem() == null) {
            MessageUtils.showError("Validation Error", "Physio is required");
            return false;
        }

        if (cbTime.getSelectionModel().getSelectedItem() == null) {
            MessageUtils.showError("Validation Error", "Time is required");
            return false;
        }

        return true;
    }
}