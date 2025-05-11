package com.example.physiocare.controller.appointments;

import com.example.physiocare.models.appointment.AppointmentResponse;
import com.example.physiocare.models.physio.Physio;
import com.example.physiocare.models.physio.PhysioListResponse;
import com.example.physiocare.models.appointment.Appointment;
import com.example.physiocare.models.record.Record;
import com.example.physiocare.utils.MessageUtils;
import com.example.physiocare.utils.ServiceUtils;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class UpcomingAppointmentsController implements Initializable {
    public DatePicker dpDate;
    public ComboBox cbTime;
    public ComboBox<Physio> cbPhysio;
    public TextArea taDiagnosis;
    public TextArea taTreatment;
    public TextArea taObservations;
    public ButtonType btnClose;
    public ButtonType btnDelete;
    public ButtonType btnSave;
    public Button buttClose;
    public Button buttDelete;
    public Button ButtSave;
    public DialogPane dialogPane;
    private Record showRecord;

    private Gson gson = new Gson();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dialogPane.setUserData(this);
        GetPhysios();
        cbTime.setDisable(true);
        taDiagnosis.setDisable(true);
        taObservations.setDisable(true);
        taTreatment.setDisable(true);
    }

    public void setShowRecord(Record showRecord) {
        this.showRecord = showRecord;
    }

    public void handleSave() {

        if(!validateForm()) return;

        String url = ServiceUtils.SERVER + "/records/" + showRecord.getId() + "/appointments";

        Date date = Date.from(dpDate.getValue().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Physio selectedPhysio = cbPhysio.getSelectionModel().getSelectedItem();

        Appointment appointment = new Appointment(date, selectedPhysio);
        String requestAppoinment = gson.toJson(appointment);

        System.out.println(requestAppoinment);

        ServiceUtils.getResponseAsync(url, requestAppoinment, "POST").thenApply(json -> gson.fromJson(json,
                AppointmentResponse.class)).thenAccept(response -> Platform.runLater(() -> {

            if (response == null || !response.isOk()) {
                System.out.println("No se encontraron los physios o hubo un error");
                MessageUtils.showError("Error", "The physios not found");
            } else {
                MessageUtils.showMessage("Insert is sutesful" , "La insercion es correcta");
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
            return null;
        });
    }

    private void GetPhysios() {

        String url = ServiceUtils.SERVER + "/physios";

        ServiceUtils.getResponseAsync(url, null, "GET").thenApply(json -> gson.fromJson(json,
                PhysioListResponse.class)).thenAccept(response -> Platform.runLater(() -> {
            if (response == null || !response.isOk()) {
                System.out.println("No se encontraron los physios o hubo un error");
                MessageUtils.showError("Error", "The physios not found");
            } else {
                cbPhysio.setItems(FXCollections.observableList(response.getResult()));
                if(!response.getResult().isEmpty())
                    cbPhysio.getSelectionModel().select(response.getResult().getFirst());
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
            return null;
        });
    }

    public void handleDelete() {

    }

    public void handleClose() {
        ((Stage) dialogPane.getScene().getWindow()).close();
    }

    private boolean validateForm() {
        if (dpDate.getValue() == null) {
            MessageUtils.showError("Validation Error", "The Date is required");
            return false;
        }

        if(dpDate.getValue().isBefore(LocalDate.now())){
            MessageUtils.showError("Validation Error", "The date must be greater than today's date.");
            return false;
        }

        if(cbPhysio.getSelectionModel().getSelectedItem() == null){
            MessageUtils.showError("Validation Error", "Error you must select a physio");
            return false;
        }

        return true;
    }


}
