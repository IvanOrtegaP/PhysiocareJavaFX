package com.example.physiocare.controller.patients;

import com.example.physiocare.controller.appointments.UpcomingAppointmentsController;
import com.example.physiocare.models.appointment.Appointment;
import com.example.physiocare.models.patient.Patient;
import com.example.physiocare.models.patient.PatientResponse;
import com.example.physiocare.models.physio.Physio;
import com.example.physiocare.models.record.Record;
import com.example.physiocare.models.record.RecordResponse;
import com.example.physiocare.utils.MessageUtils;
import com.example.physiocare.utils.ScreenUtils;
import com.example.physiocare.utils.ServiceUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PatientDetailViewController implements Initializable {
    private final Gson gson = new Gson();
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
    public TableColumn<Appointment, Date> colDateUpcoming;
    public TableColumn<Appointment, Date> colTimeUpcoming;
    public TableColumn<Appointment, Physio> colPhysioNameUpcoming;
    public TableColumn colReasonUpcoming;
    public TextField txtSearchHistory;
    public Button btnAddHistory;
    public Button btnEditHistory;
    public Button btnDeleteHistory;
    public TableView<Appointment> tblHistory;
    public TableColumn<Appointment , Date> colDateHistory;
    public TableColumn<Appointment,Date> colTimeHistory;
    public TableColumn colPhysioNameHistory;
    public TableColumn colDiagnosisHistory;
    public TabPane tabPane;
    @FXML
    public BorderPane borderPane;
    public Button btnSave;
    private Patient showPatient;
    private Record RecordShowPatient;

    private final SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");


    public void setShowPatient(Patient showPatient) {
        this.showPatient = showPatient;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        borderPane.setUserData(this);
        setupTableHistory();
        setupTableUpcoming();
        DisableForm(true);
        btnSave.setDisable(true);
        btnSave.setVisible(false);
        btnSave.setManaged(false);
    }

    private void DisableForm(Boolean disable){
        txtName.setDisable(disable);
        txtSurname.setDisable(disable);
        txtAddress.setDisable(disable);
        txtEmail.setDisable(disable);
        txtInsuranceNumber.setDisable(disable);
        dpBirthDate.setDisable(disable);
    }

    public void postInit() {
        if (showPatient != null) {
            txtName.setText((showPatient.getName() != null ? showPatient.getName() : "No tiene nombre asignado"));
            txtSurname.setText((showPatient.getSurname() != null ? showPatient.getSurname() : "No tiene apellido asignado"));
            txtAddress.setText((showPatient.getAddress() != null ? showPatient.getAddress() : "No tiene direccion asignada"));
            txtEmail.setText((showPatient.getEmail() != null ? showPatient.getEmail() : "No tiene email asignado"));
            txtInsuranceNumber.setText(showPatient.getInsuranceNumber() != null ? showPatient.getInsuranceNumber() : "No tiene insurance number asignado");
            dpBirthDate.setValue(showPatient.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

            getAppointmentsPatient();
        }
    }

    private void setupTableHistory() {

        colDiagnosisHistory.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));

        colDateHistory.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDateHistory.setCellFactory(column ->buildDateCell(formatDate));

        colTimeHistory.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTimeHistory.setCellFactory(column -> buildDateCell(formatTime));

        colPhysioNameHistory.setCellValueFactory(new PropertyValueFactory<>("physio"));

    }

    private TableCell<Appointment, Date> buildDateCell(SimpleDateFormat formatter) {
        return new TableCell<>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : formatter.format(item));
            }
        };
    }


    private void setupTableUpcoming() {
        colDateUpcoming.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTimeUpcoming.setCellValueFactory(new PropertyValueFactory<>("date"));

        colDateUpcoming.setCellFactory(col -> buildDateCell(formatDate));
        colTimeUpcoming.setCellFactory(col -> buildDateCell(formatTime));
        colPhysioNameUpcoming.setCellValueFactory(new PropertyValueFactory<>("physio"));
    }

    public void getAppointmentsPatient() {
        String url = ServiceUtils.SERVER + "/records/patient/" + showPatient.getId();
        System.out.println("Fetching appointments from: " + url);

        ServiceUtils.getResponseAsync(url, null, "GET")
                .thenApply(json -> {
                    System.out.println("Raw JSON response: " + json);
                    try {
                        // First parse as JsonObject to handle the structure properly
                        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

                        // Then convert to RecordResponse
                        RecordResponse response = gson.fromJson(json, RecordResponse.class);

                        // If appointments is null in the response, initialize empty list
                        if (response.getResult() != null && response.getResult().getAppointments() == null) {
                            response.getResult().setAppointments(new ArrayList<>());
                        }

                        return response;
                    } catch (Exception e) {
                        System.err.println("Error parsing JSON: " + e.getMessage());
                        throw new RuntimeException("Failed to parse patient records", e);
                    }
                })
                .thenAccept(response -> Platform.runLater(() -> {
                    if (response == null || !response.isOk()) {
                        System.out.println("No patient records found or error in response");
                        MessageUtils.showError("Error", "No se encontró el historial del paciente");
                    } else {
                        System.out.println("Successfully retrieved patient records");
                        RecordShowPatient = response.getResult();

                        // Initialize appointments if null
                        if (RecordShowPatient.getAppointments() == null) {
                            RecordShowPatient.setAppointments(new ArrayList<>());
                        }

                        tblHistory.setItems(FXCollections.observableList(RecordShowPatient.getAppointments()));
                        PopulateTables();
                    }
                }))
                .exceptionally(ex -> {
                    System.err.println("Exception in getAppointmentsPatient: " + ex.getMessage());
                    Platform.runLater(() -> {
                        if (ex.getCause() instanceof JsonSyntaxException) {
                            MessageUtils.showError("Error de formato", "Los datos del paciente tienen un formato incorrecto");
                        } else {
                            MessageUtils.showError("Error", "No se pudo cargar el historial del paciente: " +
                                    (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()));
                        }
                    });
                    ex.printStackTrace();
                    return null;
                });
    }

    private void PopulateTables() {
        if (RecordShowPatient == null || RecordShowPatient.getAppointments() == null) {
            tblUpcoming.setItems(FXCollections.observableArrayList());
            tblHistory.setItems(FXCollections.observableArrayList());
            return;
        }

        Date now = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        List<Appointment> upcomingAppointments = RecordShowPatient.getAppointments().stream()
                .filter(appointment -> appointment != null && appointment.getDate() != null && appointment.getDate().after(now))
                .collect(Collectors.toList());

        List<Appointment> pastAppointments = RecordShowPatient.getAppointments().stream()
                .filter(appointment -> appointment != null && appointment.getDate() != null && appointment.getDate().before(now))
                .collect(Collectors.toList());

        tblUpcoming.setItems(FXCollections.observableList(upcomingAppointments));
        tblHistory.setItems(FXCollections.observableList(pastAppointments));
    }



    public void handleDelete(ActionEvent actionEvent) {
        if (showPatient == null) return;

        String message = "Are you sure you want to delete " + showPatient.getName() + "?";
        MessageUtils.showConfirmation("Delete Patient", message, "Delete patient").showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                DeletePatient();
                //Hace la accion de cerrar la ventana modal
                ((Stage) btnClose.getScene().getWindow()).close();
            }
        });
    }

    private void DeletePatient() {
        String url = ServiceUtils.SERVER + "/patients/" + showPatient.getId();

        ServiceUtils.getResponseAsync(url, null, "DELETE").thenApply(json -> gson.fromJson(json, PatientResponse.class)).thenAccept(responseApi -> Platform.runLater(() -> {
            if (responseApi != null && responseApi.isOk()) {
                MessageUtils.showMessage("Success", "Patient deleted successfully");
            } else {
                MessageUtils.showError("Error deleting patient", "The patient to be eliminated is not found");
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> MessageUtils.showError("Error deleting patient", ex.getLocalizedMessage()));
            return null;
        });
    }

    public void handleEdit(ActionEvent actionEvent) {
        DisableForm(false);
        DisableButtons();
    }

    private void DisableButtons(){
        btnDelete.setDisable(!btnDelete.isDisable());
        btnDelete.setVisible(!btnDelete.isVisible());
        btnDelete.setManaged(!btnDelete.isManaged());
        btnSave.setDisable(!btnSave.isDisable());
        btnSave.setVisible(!btnSave.isVisible());
        btnSave.setManaged(!btnSave.isManaged());
        btnEdit.setVisible(!btnEdit.isVisible());
        btnEdit.setDisable(!btnEdit.isDisable());
        btnEdit.setManaged(!btnEdit.isManaged());
    }

    public void handleClose(ActionEvent actionEvent) {
        ((Stage) btnClose.getScene().getWindow()).close();
    }

    public void handleAddAppointment(ActionEvent actionEvent) {

        Stage stage = ScreenUtils.createViewModal("/com/example/physiocare/appointment/AppointmentView.fxml", "PhysioCare - New Appointment");

        if (stage != null) {
            UpcomingAppointmentsController controller = (UpcomingAppointmentsController) stage.getScene().getRoot().getUserData();
            controller.setShowRecord(RecordShowPatient);
            stage.showAndWait();
            getAppointmentsPatient();
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

    public void handleSave(ActionEvent actionEvent) {
        DisableForm(false);
        DisableButtons();
    }
}
