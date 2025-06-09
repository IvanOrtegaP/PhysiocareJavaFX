package com.example.physiocare.controller.appointments;

import com.example.physiocare.models.BaseResponse;
import com.example.physiocare.models.appointment.Appointment;
import com.example.physiocare.models.physio.Physio;
import com.example.physiocare.models.record.Record;
import com.example.physiocare.services.AppointmentsService;
import com.example.physiocare.services.PhysiosService;
import com.example.physiocare.utils.MessageUtils;
import com.example.physiocare.utils.ValidateUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AppointmentDetailViewController implements Initializable {
    private final List<String> hoursList = List.of("09:00", "10:00", "11:00", "12:00", "13:00", "15:00", "16:00");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    public DatePicker dpDate;
    public ChoiceBox<String> cbTime;
    public ChoiceBox<Physio> cbPhysio;
    public TextArea taDiagnosis;
    public TextArea taTreatment;
    public TextArea taObservations;
    public ButtonType btnDelete;
    public Button buttClose;
    public Button buttDelete;
    public Button buttSave;
    public DialogPane dialogPane;
    public Label lblDiagnosis;
    public Label lblTreatment;
    public Label lblObservations;
    public Button buttEdit;
    private com.example.physiocare.models.record.Record showRecord;
    private Appointment showAppointment;
    private boolean addRecord = false;
    private boolean isFutureAppointment = false;

    public void setAddRecord(boolean add) {
        this.addRecord = add;
    }

    public void setShowAppointment(Appointment showAppointment) {
        this.showAppointment = showAppointment;
    }

    public void setShowRecord(Record showRecord) {
        this.showRecord = showRecord;
    }

    public void setIsFutureAppointment(boolean isFutureAppointment) {
        this.isFutureAppointment = isFutureAppointment;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dialogPane.setUserData(this);
        GetPhysios();
        cbTime.setItems(FXCollections.observableList(hoursList));
        cbTime.getSelectionModel().selectFirst();
        buttSave.setDisable(true);
        buttSave.setVisible(false);
        buttSave.setManaged(false);
    }


    public void postInit() {
        if (isFutureAppointment) {
            hideFielsds();
            if (addRecord) {
                DisableButtons();
            } else {
                DisableForm(true);
                PopulateForm();
            }
        } else {
            if (addRecord) {
                DisableButtons();
            } else if (showAppointment != null) {
                DisableForm(true);
                PopulateForm();
            }
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
        if (showAppointment != null) {
            taDiagnosis.setText((showAppointment.getDiagnosis() != null ? showAppointment.getDiagnosis() : "Has no assigned diagnosis"));
            taTreatment.setText((showAppointment.getTreatment() != null ? showAppointment.getTreatment() : "Has no assigned treatment"));
            taObservations.setText((showAppointment.getTreatment() != null ? showAppointment.getTreatment() : "Has no assigned observations"));
            try {
                LocalDateTime localDateTime = showAppointment.getDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
                dpDate.setValue(localDateTime.toLocalDate());

                String time = localDateTime.format(formatter);

                if (cbTime.getItems().contains(time))
                    cbTime.setValue(localDateTime.format(formatter));
                else {
                    cbTime.setValue(hoursList.getFirst());
                }
            } catch (IllegalArgumentException | NullPointerException e) {
                cbTime.setValue(hoursList.getFirst());
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


    private void saveAppointment() {
        if (!validateForm()) return;

        Appointment appointment;

        if(isFutureAppointment){
            Date date = CreateDate();
            Physio selectedPhysio = cbPhysio.getSelectionModel().getSelectedItem();

            appointment = new Appointment(date, selectedPhysio, "pending");
        }else {
            appointment = getAppointment();
            appointment.setConfirm("pending");
        }

        AppointmentsService.saveAppointment(showRecord, appointment ).thenAccept(resp ->
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

    private void EditAppointment() {
        if (!validateForm()) return;

        Appointment appointment = getAppointment();

        AppointmentsService.putAppointment(showRecord, appointment).thenAccept(resp -> Platform.runLater(() -> {
            if (resp != null && resp.isOk() && resp.getResult() != null) {
                showAppointment = resp.getResult();
                System.out.println("Show appointment : " + showAppointment.getDiagnosis());
                PopulateForm();
                DisableForm(true);
                DisableButtons();
            } else {
                String error = Optional.ofNullable(resp).map(BaseResponse::getErrorMessage).orElse("Unknow error");
                MessageUtils.showError("Error update appointment", error);
            }
        })).exceptionally(ex -> {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            MessageUtils.showError("Error edit appointment", ex.getMessage());
            return null;
        });
    }

    private Appointment getAppointment() {
        String idAppointment = Optional.ofNullable(showAppointment).map(Appointment::getId).orElse(null);
        String confirmAppointment = Optional.ofNullable(showAppointment).map(Appointment::getConfirm).orElse("pending");

        return new Appointment(
                idAppointment,
                CreateDate(),
                cbPhysio.getSelectionModel().getSelectedItem(),
                taDiagnosis.getText(),
                taTreatment.getText(),
                taObservations.getText(),
                confirmAppointment
        );
    }

    public void handleSave() {
        if (addRecord) {
            saveAppointment();
        } else if (showAppointment != null) {
            EditAppointment();
        }
    }

    private void DisableForm(Boolean disable) {
        cbTime.setDisable(disable);
        dpDate.setDisable(disable);
        taObservations.setDisable(disable);
        taTreatment.setDisable(disable);
        taDiagnosis.setDisable(disable);
        cbPhysio.setDisable(disable);
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

    private void DisableButtons() {
        buttSave.setDisable(!buttSave.isDisable());
        buttSave.setVisible(!buttSave.isVisible());
        buttSave.setManaged(!buttSave.isManaged());

        buttEdit.setDisable(!buttEdit.isDisable());
        buttEdit.setManaged(!buttEdit.isManaged());
        buttEdit.setVisible(!buttEdit.isVisible());

        buttDelete.setDisable(!buttDelete.isDisable());
        buttDelete.setVisible(!buttDelete.isVisible());
        buttDelete.setManaged(!buttDelete.isManaged());
    }

    public void handleClose() {
        if (buttSave.isVisible() && !addRecord) {
            PopulateForm();
            DisableButtons();
            DisableForm(true);
        } else {
            ((Stage) dialogPane.getScene().getWindow()).close();
        }
    }

    private boolean validateForm() {
        if (dpDate.getValue() == null) {
            MessageUtils.showError("Validation Error", "The Date is required");
            return false;
        }

        if(isFutureAppointment){
            if (dpDate.getValue().isBefore(LocalDate.now())) {
                MessageUtils.showError("Validation Error", "The date must be greater than today's date.");
                return false;
            }
        }else {
            if (dpDate.getValue().isAfter(LocalDate.now())) {
                MessageUtils.showError("Validation Error", "The date must be less than the current date.");
                return false;
            }
        }

        if (cbPhysio.getSelectionModel().getSelectedItem() == null) {
            MessageUtils.showError("Validation Error", "Error you must select a physio");
            return false;
        }

        if (cbTime.getSelectionModel().getSelectedItem() == null) {
            MessageUtils.showError("Validation Error", "Error you must select a time the date");
            return false;
        }

        if (addRecord && !isFutureAppointment) {
            if (!ValidateUtils.validateLength(taDiagnosis.getText(), 10, 500)) {
                MessageUtils.showError("Validation Error",
                        "The diagnosis must be greater than 10 characters and less than 500 characters.");
                return false;
            }
        }

        return true;
    }

    public void handleEdit(ActionEvent actionEvent) {
        DisableForm(false);
        DisableButtons();
    }
}
