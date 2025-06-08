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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class UpcomingAppointmentsController implements Initializable {
    public DatePicker dpDate;
    public ChoiceBox<String> cbTime;
    public ChoiceBox<Physio> cbPhysio;
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
    public Label lblDiagnosis;
    public Label lblTreatment;
    public Label lblObservations;

    private Record showRecord;
    private Appointment showAppointment;
    private boolean addRecord = false;
    private final List<String> hoursList = List.of("09:00", "10:00", "11:00", "12:00", "13:00", "15:00", "16:00");

    private Gson gson = new Gson();

    public void setAddRecord(boolean add){
        this.addRecord = add;
    }

    public void setShowAppointment(Appointment showAppointment){
        this.showAppointment = showAppointment;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dialogPane.setUserData(this);
        GetPhysios();
        cbTime.setItems(FXCollections.observableList(hoursList));
        cbTime.getSelectionModel().selectFirst();
    }


    public void postInit(){
        System.out.println(addRecord);
        if(addRecord){
            hideFielsds();
        }
    }

    private void hideFielsds(){
        taDiagnosis.setDisable(true);
        taDiagnosis.setVisible(false);
        lblDiagnosis.setVisible(false);

        taObservations.setDisable(true);
        taObservations.setVisible(false);
        lblObservations.setManaged(false);
        lblDiagnosis.setVisible(false);

        taTreatment.setDisable(true);
        taTreatment.setVisible(false);
        lblTreatment.setVisible(false);
    }

    private void PopulateForm(){

    }

    private Date CreateDate() {
        LocalDate localDate = dpDate.getValue();
        String selectedTime = cbTime.getValue();
        String[] timeParts = selectedTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        LocalDateTime dateTime = localDate.atTime(hour, minute);

        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }


    public void setShowRecord(Record showRecord) {
        this.showRecord = showRecord;
    }

    public void handleSave() {

        if(!validateForm()) return;

        String url = ServiceUtils.SERVER + "/records/" + showRecord.getId() + "/appointments";

        Date date = CreateDate();
        Physio selectedPhysio = cbPhysio.getSelectionModel().getSelectedItem();

        Appointment appointment = new Appointment(date, selectedPhysio, "pending");
        String requestAppoinment = gson.toJson(appointment);

        System.out.println(requestAppoinment);

        ServiceUtils.getResponseAsync(url, requestAppoinment, "POST").thenApply(json -> gson.fromJson(json,
                AppointmentResponse.class)).thenAccept(response -> Platform.runLater(() -> {

            if (response == null || !response.isOk()) {
                System.out.println("No se encontraron los physios o hubo un error");
                MessageUtils.showError("Error", "The physios not found");
            } else {
                MessageUtils.showMessage("Insert is sutesful" , "La insercion es correcta");
                ((Stage) buttClose.getScene().getWindow()).close();
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
                cbPhysio.setItems(FXCollections.observableList(response.getPhysios()));
                if(!response.getPhysios().isEmpty())
                    cbPhysio.getSelectionModel().select(response.getPhysios().getFirst());
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
            return null;
        });
    }

    public void handleDelete() {
        if(showAppointment == null && showRecord == null) return;

        String message = "Are you sure want to delete this appointment?";

        MessageUtils.showConfirmation("Delete Appointment", message , "Delete Appointmet").showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                DelteApponintment();
            }
        });
    }

    private void DelteApponintment(){
        String url = ServiceUtils.SERVER + "/records/" + showRecord.getId() + "/appointments/" + showAppointment.getId();

        ServiceUtils.getResponseAsync(url, null, "DELETE")
                .thenApply(json -> {
                    System.out.println("Debug DELETE APPOINTMENT : " + json);
                    return gson.fromJson(json, AppointmentResponse.class);
                }).thenAccept(resp -> Platform.runLater(() -> {
                    if(resp != null && resp.isOk()){
                        MessageUtils.showMessage("Success", "Appointment deleted successfully");
                        ((Stage) buttClose.getScene().getWindow()).close();
                    }
                })).exceptionally(ex -> {
                    Platform.runLater(() -> MessageUtils.showError("Error deleting appointment", ex.getLocalizedMessage()));
                    return null;
                });
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

        if(cbTime.getSelectionModel().getSelectedItem() == null){
            MessageUtils.showError("Validation Error", "Error you must select a time the date");
            return false;
        }

        return true;
    }
}
