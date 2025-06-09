package com.example.physiocare.controller.patients;

import com.example.physiocare.controller.appointments.UpcomingAppointmentsController;
import com.example.physiocare.models.appointment.Appointment;
import com.example.physiocare.models.patient.PatientMoreInfo;
import com.example.physiocare.models.patient.PatientMoreInfoResponse;
import com.example.physiocare.models.patient.PatientResponse;
import com.example.physiocare.models.physio.Physio;
import com.example.physiocare.models.record.Record;
import com.example.physiocare.models.record.RecordResponse;
import com.example.physiocare.models.user.User;
import com.example.physiocare.utils.MessageUtils;
import com.example.physiocare.utils.ScreenUtils;
import com.example.physiocare.utils.ServiceUtils;
import com.example.physiocare.utils.ValidateUtils;
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
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PatientDetailViewController implements Initializable {
    private final Gson gson = new Gson();
    private final SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
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
    public TableColumn<Appointment, Date> colDateHistory;
    public TableColumn<Appointment, Date> colTimeHistory;
    public TableColumn colPhysioNameHistory;
    public TableColumn colDiagnosisHistory;
    public TabPane tabPane;
    @FXML
    public BorderPane borderPane;
    public Button btnSave;
    public Tab tabPatHistoryApp;
    public Tab tabPatUpcomingApp;
    public Tab tabPatInf;
    private Record RecordShowPatient;
    private Boolean addPatient = false;
    private PatientMoreInfo showPatientMoreInfo;
    private Appointment currentAppointmentUpcoming;

    public void setShowPatientMoreInfo(PatientMoreInfo showPatientMoreInfo) {
        this.showPatientMoreInfo = showPatientMoreInfo;
    }

    public void setAddPatient(boolean add) {
        this.addPatient = add;
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

    private void DisableForm(Boolean disable) {
        txtName.setDisable(disable);
        txtSurname.setDisable(disable);
        txtAddress.setDisable(disable);
        txtEmail.setDisable(disable);
        txtInsuranceNumber.setDisable(disable);
        dpBirthDate.setDisable(disable);
        txtUsername.setDisable(disable);
        pfPassword.setDisable(disable);
    }

    /**
     * Funcion importante que se ejecuta despues del inicialize se ejecuata a mano.
     */
    public void postInit() {
        if (!addPatient) {
            if (showPatientMoreInfo != null) {
                populateForm();
                getAppointmentsPatient();
            }
        } else {
            tabPane.getTabs().remove(tabPatUpcomingApp);
            tabPane.getTabs().remove(tabPatHistoryApp);
            DisableForm(false);
            DisableButtons();
        }
    }

    public void populateForm() {
        if (showPatientMoreInfo != null) {
            txtName.setText((showPatientMoreInfo.getName() != null ? showPatientMoreInfo.getName() : "No tiene nombre asignado"));
            txtSurname.setText((showPatientMoreInfo.getSurname() != null ? showPatientMoreInfo.getSurname() : "No tiene apellido asignado"));
            txtAddress.setText((showPatientMoreInfo.getAddress() != null ? showPatientMoreInfo.getAddress() : "No tiene direccion asignada"));
            txtInsuranceNumber.setText(showPatientMoreInfo.getInsuranceNumber() != null ? showPatientMoreInfo.getInsuranceNumber() : "No tiene insurance number asignado");
            dpBirthDate.setValue(showPatientMoreInfo.getBitrthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            txtUsername.setText(showPatientMoreInfo.getUser().getLogin() != null ? showPatientMoreInfo.getUser().getLogin() : "No tiene nombre de usuario asignado");
            txtEmail.setText((showPatientMoreInfo.getUser().getLogin() != null ? showPatientMoreInfo.getUser().getEmail() : "No tiene email asignado"));
        }
    }

    private void setupTableHistory() {

        colDiagnosisHistory.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));

        colDateHistory.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDateHistory.setCellFactory(column -> buildDateCell(formatDate));

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

        tblUpcoming.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if(newSelection != null){
                        currentAppointmentUpcoming = newSelection;
                        btnDelete.setDisable(false);
                    }
        });

        tblUpcoming.setRowFactory(tv -> {
            TableRow<Appointment> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && (!row.isEmpty())){
                    Stage stage = ScreenUtils.createViewModal("/com/example/physiocare/appointment/AppointmentView.fxml",
                            "PhysioCare - Detail Appointment");

                    if(stage != null){
                        UpcomingAppointmentsController controller =
                                (UpcomingAppointmentsController) stage.getScene().getRoot().getUserData();
                        controller.setShowRecord(RecordShowPatient);
                        controller.setShowAppointment(row.getItem());
                        controller.setAddRecord(true);
                        controller.postInit();
                        stage.showAndWait();
                        getAppointmentsPatient();
                    }
                }
            });
            return row;
        });
    }

    public void getAppointmentsPatient() {
        String url = ServiceUtils.SERVER + "/records/patient/" + showPatientMoreInfo.getId();
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
        if (showPatientMoreInfo == null) return;

        String message = "Are you sure you want to delete " + showPatientMoreInfo.getName() + "?";
        MessageUtils.showConfirmation("Delete Patient", message, "Delete patient").showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                DeletePatient();
                //Hace la accion de cerrar la ventana modal
                ((Stage) btnClose.getScene().getWindow()).close();
            }
        });
    }

    private void DeletePatient() {
        String url = ServiceUtils.SERVER + "/patients/" + showPatientMoreInfo.getId();

        ServiceUtils.getResponseAsync(url, null, "DELETE")
                .thenApply(json -> gson.fromJson(json, PatientResponse.class))
                .thenAccept(responseApi -> Platform.runLater(() -> {
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

    private void EditPatient() {
        if (!validateForm()) return;

        String url = ServiceUtils.SERVER + "/patients/" + showPatientMoreInfo.getId();

        Date date = Date.from(dpBirthDate.getValue().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        PatientMoreInfo patient = getPatientMoreInfo(date);

        String PatientJson = gson.toJson(patient);

        ServiceUtils.getResponseAsync(url, PatientJson, "PUT").thenApply(json -> {
            System.out.println("DEBUG- PATIENT PUT JSON : " + json);
            return gson.fromJson(json, PatientMoreInfoResponse.class);
        }).thenAccept(resp -> Platform.runLater(() -> {
            if (resp != null && resp.isOk()) {
                showPatientMoreInfo = resp.getPatient();
                populateForm();
                DisableForm(true);
            } else {
                System.out.println();
                String errro = (resp.getErrorMessage() != null) ? resp.getErrorMessage() : "Unknown error";
                MessageUtils.showError("Error update patient", errro);
            }
        })).exceptionally(ex -> {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            MessageUtils.showError("Error update patient", ex.getMessage());
            return null;
        });
    }

    private void savePatient() {
        if (!validateForm()) return;

        String url = ServiceUtils.SERVER + "/patients";

        Date date = Date.from(dpBirthDate.getValue().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        PatientMoreInfo patient = getPatientMoreInfo(date);

        String PatientJson = gson.toJson(patient);

        ServiceUtils.getResponseAsync(url, PatientJson, "POST").thenApply(json -> {
            System.out.println("DEBUG- PATIENT POST JSON : " + json);
            return gson.fromJson(json, PatientResponse.class);
        }).thenAccept(resp -> Platform.runLater(() -> {
            if (resp != null && resp.isOk()) {
                ((Stage) btnClose.getScene().getWindow()).close();
            } else {
                String errro = (resp.getErrorMessage() != null) ? resp.getErrorMessage() : "Unknown error";
                MessageUtils.showError("Error post patient", errro);
            }
        })).exceptionally(ex -> {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            MessageUtils.showError("Error post patient", ex.getMessage());
            return null;
        });
    }

    private PatientMoreInfo getPatientMoreInfo(Date date) {

        String idUser = Optional.ofNullable(showPatientMoreInfo)
                .map(PatientMoreInfo::getUser)
                .map(User::getId)
                .orElse(null);

        User user = new User(
                idUser,
                txtUsername.getText().trim(),
                pfPassword.getText().trim(),
                txtEmail.getText().trim(),
                "patient"
        );

        String idPatient = Optional.ofNullable(showPatientMoreInfo)
                .map(PatientMoreInfo::getId)
                .orElse(null);

        return new PatientMoreInfo(
                idPatient,
                txtName.getText().trim(),
                txtSurname.getText().trim(),
                date,
                txtAddress.getText().trim(),
                txtInsuranceNumber.getText().trim(),
                user
        );
    }

    private boolean validateForm() {

        if (!ValidateUtils.validateNotEmpty(txtName.getText()) || !ValidateUtils.validateNotEmpty(txtSurname.getText())) {
            MessageUtils.showError("Validation Error", "Name and surname are required");
            return false;
        }

        if (!ValidateUtils.validateLength(txtName.getText(), 2, 50)) {
            MessageUtils.showError("Validation Error", "The name must be greater than 2 characters and less than 50 characters.");
            return false;
        }

        if (!ValidateUtils.validateLength(txtSurname.getText(), 2, 50)) {
            MessageUtils.showError("Validation Error", "The surname must be greater than 2 characters and less than 50 characters.");
            return false;
        }

        if (dpBirthDate.getValue() == null) {
            MessageUtils.showError("Validation Error", "Birth date is required");
            return false;
        }
        if (!ValidateUtils.validateNotEmpty(txtInsuranceNumber.getText())) {
            MessageUtils.showError("Validation Error", "Insurance number is required");
            return false;
        }

        if (ValidateUtils.validateNotEmpty(txtAddress.getText()) && !ValidateUtils.validateLength(txtAddress.getText(), 0, 255)) {
            MessageUtils.showError("Validation Error", "The address must be less than 255 characters.");
            return false;
        }

        if (!ValidateUtils.validateRegex(txtInsuranceNumber.getText(), Pattern.compile("^[a-zA-Z0-9]{9}$"))) {
            MessageUtils.showError("Validation Error", "Insurance number must be 9 alphanumeric characters");
            return false;
        }

        if (!ValidateUtils.validateNotEmpty(txtUsername.getText())) {
            MessageUtils.showError("Validation Error", "User name are required");
            return false;
        }

        if (!ValidateUtils.validateNotEmpty(txtEmail.getText())) {
            MessageUtils.showError("Validation Error", "Email is required");
            return false;
        }

        if (!ValidateUtils.validateRegex(txtEmail.getText(), Pattern.compile("^\\S+@\\S+\\.\\S+$"))) {
            MessageUtils.showError("Validation Error", "The email does not comply with the correct format");
            return false;
        }
        return true;
    }

    private void DisableButtons() {
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

    public void handleEdit(ActionEvent actionEvent) {
        DisableForm(false);
        DisableButtons();
    }

    public void handleClose(ActionEvent actionEvent) {
        ((Stage) btnClose.getScene().getWindow()).close();
    }

    public void handleAddAppointment(ActionEvent actionEvent) {

        Stage stage = ScreenUtils.createViewModal("/com/example/physiocare/appointment/AppointmentView.fxml",
                "PhysioCare - New Appointment");

        if (stage != null) {
            UpcomingAppointmentsController controller = (UpcomingAppointmentsController) stage.getScene().getRoot().getUserData();
            controller.setShowRecord(RecordShowPatient);
            controller.setAddRecord(true);
            controller.postInit();
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
        if (!addPatient) {
            if (showPatientMoreInfo != null) {
                EditPatient();
                DisableForm(false);
                DisableButtons();
            }
        } else {
            savePatient();
        }
    }
}
