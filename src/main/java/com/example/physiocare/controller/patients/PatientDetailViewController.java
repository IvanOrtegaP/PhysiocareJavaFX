package com.example.physiocare.controller.patients;

import com.example.physiocare.controller.appointments.UpcomingAppointmentsController;
import com.example.physiocare.models.appointment.Appointment;
import com.example.physiocare.models.patient.Patient;
import com.example.physiocare.models.record.Record;
import com.example.physiocare.models.record.RecordResponse;
import com.example.physiocare.utils.MessageUtils;
import com.example.physiocare.utils.ScreenUtils;
import com.example.physiocare.utils.ServiceUtils;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class PatientDetailViewController implements Initializable {
    public TextField txtName;
    public TextField txtSurname;
    public DatePicker dpBirthDate;
    public TextField txtEmail;
    public TextField txtAddress;
    public TextField txtInsuranceNumber;
    public TextField txtUsername;
    public PasswordField pfPassword;
    public Button btnClose;
    public Button btnEdit;
    public Button btnDelete;
    public TextField txtSearchUpcoming;
    public Button btnAddAppointment;
    public Button btnEditAppointment;
    public Button btnDeleteAppointment;
    public TableView<Appointment> tblUpcoming;
    public TableColumn<Appointment,Date> colDateUpcoming;
    public TableColumn colTimeUpcoming;
    public TableColumn colPhysioNameUpcoming;
    public TableColumn colReasonUpcoming;
    public TextField txtSearchHistory;
    public Button btnAddHistory;
    public Button btnEditHistory;
    public Button btnDeleteHistory;
    public TableView tblHistory;
    public TableColumn colDateHistory;
    public TableColumn colTimeHistory;
    public TableColumn colPhysioNameHistory;
    public TableColumn colDiagnosisHistory;
    public TabPane tabPane;
    @FXML
    public BorderPane borderPane;
    private Gson gson = new Gson();
    private Patient showPatient;
    private Record RecordShowPatient;


    public void setShowPatient(Patient showPatient) {
        this.showPatient = showPatient;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        borderPane.setUserData(this);
        setupTableHistory();
        setupTableUpcoming();
    }

    public  void postInit(){
        if(showPatient != null){
            txtName.setText((showPatient.getName() != null ? showPatient.getName() : "No tiene nombre asignado"));
            getAppoinmentsPatient();
        }
    }

    private void setupTableHistory() {

        colDiagnosisHistory.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        colDateHistory.setCellValueFactory(new PropertyValueFactory<>("date"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        colDateHistory.setCellFactory(column -> new TableCell<Appointment, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(sdf.format(item));
                }
            }
        });
    }

    private void  setupTableUpcoming(){
        colDateUpcoming.setCellValueFactory(new PropertyValueFactory<>("date"));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        colDateUpcoming.setCellFactory(column -> new TableCell<Appointment, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(sdf.format(item));
                }
            }
        });
    }

    public void getAppoinmentsPatient() {
        String url = ServiceUtils.SERVER + "/records/patient/" + showPatient.getId();

        ServiceUtils.getResponseAsync(url, null, "GET").thenApply(json -> gson.fromJson(json,
                RecordResponse.class)).thenAccept(response -> Platform.runLater(() -> {

            if (response == null || !response.isOk()) {
                System.out.println("No se encontraron pacientes o hubo un error");
                MessageUtils.showError("Error", "The patient record not found");
            } else {
                Date date = new Date();
                RecordShowPatient = response.getResult();
                tblHistory.setItems(FXCollections.observableList(response.getResult().getAppointments()));
                PopulateTables();

            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
            ex.printStackTrace();
            return null;
        });
    }

    private void PopulateTables(){
        Date now = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        List<Appointment> proxAppoinments = RecordShowPatient.getAppointments().stream()
                .filter(appointment -> appointment.getDate().after(now)).toList();

        tblUpcoming.setItems(FXCollections.observableList(proxAppoinments));

        List<Appointment> historyAppoinments = RecordShowPatient.getAppointments().stream()
                .filter(appointment -> appointment.getDate().before(now)).toList();

        tblHistory.setItems(FXCollections.observableList(historyAppoinments));

    }

    public void handleDelete(ActionEvent actionEvent) {
    }

    public void handleEdit(ActionEvent actionEvent) {
    }

    public void handleClose(ActionEvent actionEvent) {
        ((Stage) btnClose.getScene().getWindow()).close();
    }

    public void handleAddAppointment(ActionEvent actionEvent) {

        Stage stage = ScreenUtils.createViewModal("/com/example/physiocare/appointment/AppointmentView.fxml"  ,
                "PhysioCare - New Appointment");

        if(stage != null){
            UpcomingAppointmentsController controller = (UpcomingAppointmentsController) stage.getScene().getRoot().getUserData();
            controller.setShowRecord(RecordShowPatient);
            stage.showAndWait();
            getAppoinmentsPatient();
        }
    }

    public void handleEditAppointment(ActionEvent actionEvent) {
    }

    public void handleDeleteAppointment(ActionEvent actionEvent) {
    }

    public void handleAddHistory(ActionEvent actionEvent) {
    }

    public void handleEditHistory(ActionEvent actionEvent) {
    }

    public void handleDeleteHistory(ActionEvent actionEvent) {
    }


}
