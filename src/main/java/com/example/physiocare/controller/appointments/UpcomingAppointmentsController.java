package com.example.physiocare.controller.appointments;

import com.example.physiocare.models.appointment.Appointment;
import com.example.physiocare.models.physio.Physio;
import com.example.physiocare.models.record.Record;
import com.example.physiocare.utils.MessageUtils;
import com.example.physiocare.utils.PhysioListDeserializer;
import com.example.physiocare.utils.ServiceUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class UpcomingAppointmentsController {

    public enum OperationType {
        VIEW, CREATE, EDIT
    }

    @FXML
    private DatePicker dpDate;
    @FXML
    private ComboBox<String> cbTime;
    @FXML
    private ComboBox<Physio> cbPhysio;
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
    private Button ButtSave;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(List.class, new PhysioListDeserializer())
            .create();
    private Appointment appointment;
    private OperationType operationType;
    private Record showRecord;


    @FXML
    private void initialize() {
        cbTime.setItems(FXCollections.observableArrayList("09:00", "10:00", "11:00", "14:00", "15:00"));
        loadPhysios();
    }

    public void setShowRecord(Record showRecord) {
        this.showRecord = showRecord;
    }

    public void configureModal(OperationType operationType, Appointment appointment) {
        this.operationType = operationType;
        this.appointment = appointment;

        switch (operationType) {
            case VIEW:
                disableForm(true);
                buttDelete.setVisible(false);
                ButtSave.setVisible(false);
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
                .thenAccept(json -> {
                    try {
                        List<Physio> physios = gson.fromJson(json, List.class);
                        Platform.runLater(() -> {
                            if (physios != null && !physios.isEmpty()) {
                                cbPhysio.setItems(FXCollections.observableArrayList(physios));
                            } else {
                                MessageUtils.showWarning("No Physios", "No physiotherapists available");
                            }
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            MessageUtils.showError("Error", "Failed to parse physios: " + e.getMessage());
                            System.err.println("Original JSON: " + json);
                            e.printStackTrace();
                        });
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> MessageUtils.showError("Error", "Failed to load physios: " + ex.getMessage()));
                    ex.printStackTrace();
                    return null;
                });
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) return;

        Date date = Date.from(dpDate.getValue().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Physio selectedPhysio = cbPhysio.getSelectionModel().getSelectedItem();

        if (operationType == OperationType.CREATE) {
            appointment = new Appointment(date, selectedPhysio);
        } else if (operationType == OperationType.EDIT) {
            appointment.setDate(date);
            appointment.setPhysio(selectedPhysio);
        }


        MessageUtils.showMessage("Success", "Appointment saved successfully");
        closeModal();
    }

    @FXML
    private void handleDelete() {
        if (appointment == null) return;

        MessageUtils.showMessage("Success", "Appointment deleted successfully");
        closeModal();
    }

    @FXML
    private void handleClose() {
        closeModal();
    }

    private void closeModal() {
        ((Stage) dpDate.getScene().getWindow()).close();
    }

    private boolean validateForm() {
        if (dpDate.getValue() == null) {
            MessageUtils.showError("Validation Error", "Date is required");
            return false;
        }
        if (cbPhysio.getSelectionModel().getSelectedItem() == null) {
            MessageUtils.showError("Validation Error", "Physio is required");
            return false;
        }
        return true;
    }
}