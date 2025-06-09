package com.example.physiocare.controller.appointments;

import com.example.physiocare.models.appointment.Appointment;
import com.example.physiocare.models.physio.Physio;
import com.example.physiocare.models.record.Record;
import com.example.physiocare.services.AppointmentsService;
import com.example.physiocare.services.PhysiosService;
import com.example.physiocare.utils.MessageUtils;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class UpcomingAppointmentsController implements Initializable {
    private final List<String> hoursList = List.of("09:00", "10:00", "11:00", "12:00", "13:00", "15:00", "16:00");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    private final Gson gson = new Gson();
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

    public void setAddRecord(boolean add) {
        this.addRecord = add;
    }

    public void setShowAppointment(Appointment showAppointment) {
        this.showAppointment = showAppointment;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dialogPane.setUserData(this);
        GetPhysios();
        cbTime.setItems(FXCollections.observableList(hoursList));
        cbTime.getSelectionModel().selectFirst();
    }


    public void postInit() {
        if (addRecord) {
            hideFielsds();
        } else if (showAppointment != null) {
            PopulateForm();
        }
    }

    private void hideFielsds() {
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

    private void PopulateForm() {
        if(showAppointment != null){
            taDiagnosis.setText((showAppointment.getDiagnosis() != null ? showAppointment.getDiagnosis() : "Has no assigned diagnosis"));
            taTreatment.setText((showAppointment.getTreatment() != null ? showAppointment.getTreatment() : "Has no assigned treatment"));
            taObservations.setText((showAppointment.getTreatment() != null ? showAppointment.getTreatment() : "Has no assigned observations"));
            try{
                LocalDateTime localDateTime = showAppointment.getDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
                dpDate.setValue(localDateTime.toLocalDate());
                cbTime.setValue(localDateTime.format(formatter));

            }catch (IllegalArgumentException | NullPointerException e){
                cbTime.setValue(null);
            }
        }
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

        if (!validateForm()) return;

        Date date = CreateDate();
        Physio selectedPhysio = cbPhysio.getSelectionModel().getSelectedItem();

        Appointment appointment = new Appointment(date, selectedPhysio, "pending");

        AppointmentsService.saveAppointment(showRecord, appointment).thenAccept(resp ->
                Platform.runLater(() -> {
                    if (resp == null || !resp.isOk()) {
                        System.out.println("No se encontraron los physios o hubo un error");
                        MessageUtils.showError("Error", "The physios not found");
                    } else {
                        MessageUtils.showMessage("Insert is sutesful", "La insercion es correcta");
                        ((Stage) buttClose.getScene().getWindow()).close();
                    }
                })).exceptionally(ex -> {
            Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
            return null;
        });
    }

    private void GetPhysios() {
        PhysiosService.getPhysios(null).thenAccept(resp -> Platform.runLater(() -> {
            if (resp == null || !resp.isOk()) {
                System.out.println("No se encontraron los physios o hubo un error");
                MessageUtils.showError("Error", "The physios not found");
            } else {
                cbPhysio.setItems(FXCollections.observableList(resp.getPhysios()));
                if (!resp.getPhysios().isEmpty())
                    cbPhysio.getSelectionModel().select(resp.getPhysios().getFirst());
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
            return null;
        });
    }

    public void handleDelete() {
        if (showAppointment == null && showRecord == null) return;

        String message = "Are you sure want to delete this appointment?";

        MessageUtils.showConfirmation("Delete Appointment", message, "Delete Appointmet").showAndWait()
                .ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        DeleteAppointment();
                        ((Stage) buttClose.getScene().getWindow()).close();
                    }
                });
    }

    private void DeleteAppointment() {
        AppointmentsService.deleteAppointment(showRecord, showAppointment).thenAccept(resp ->
                Platform.runLater(() -> {
                    if (resp != null && resp.isOk()) {
                        MessageUtils.showMessage("Success", "Appointment deleted successfully");
                        ((Stage) buttClose.getScene().getWindow()).close();
                    }
                })).exceptionally(ex -> {
            ex.printStackTrace();
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

        if (dpDate.getValue().isBefore(LocalDate.now())) {
            MessageUtils.showError("Validation Error", "The date must be greater than today's date.");
            return false;
        }

        if (cbPhysio.getSelectionModel().getSelectedItem() == null) {
            MessageUtils.showError("Validation Error", "Error you must select a physio");
            return false;
        }

        if (cbTime.getSelectionModel().getSelectedItem() == null) {
            MessageUtils.showError("Validation Error", "Error you must select a time the date");
            return false;
        }

        return true;
    }
}
