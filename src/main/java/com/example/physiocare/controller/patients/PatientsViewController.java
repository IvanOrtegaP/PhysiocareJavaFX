package com.example.physiocare.controller.patients;

import com.example.physiocare.models.appointment.AppoinmentListResponse;
import com.example.physiocare.models.appointment.Appointment;
import com.example.physiocare.models.patient.*;
import com.example.physiocare.utils.MessageUtils;
import com.example.physiocare.utils.RoleAwareController;
import com.example.physiocare.utils.ScreenUtils;
import com.example.physiocare.utils.ServiceUtils;
import com.google.gson.Gson;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class PatientsViewController implements Initializable, RoleAwareController {
    private final ObservableList<Patient> patients = FXCollections.observableArrayList();
    private final Gson gson = new Gson();
    public Label lblLoggedUser;
    public TextField txtSearch;
    public Button btnAdd;
    public Button btnRefresh;
    public Button btnEdit;

    public Button btnDelete;
    public TableView<Patient> tblPatients;
    public TableColumn<Patient,String> colName;
    public TableColumn<Patient,String> colSurname;
    public TableColumn<Patient,String> colInsuranceNumber;
    public TableColumn<Patient, Date> colBirthDate;
    public Button btnViewPhysios;

    private Patient currentPatient = null;
    private final SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");

    //Mete dilay a la busqueda por texto
    PauseTransition pause = new PauseTransition(Duration.millis(600));


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadPatients();
        lblLoggedUser.setText("Welcome , " + ServiceUtils.getLogin());
        //No se inciara la busqueda asta que pase 600 ms despues de que el usuario halla dejado de escribir
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            pause.setOnFinished(event ->loadPatients(newValue.trim()));
            pause.playFromStart();
        });
    }

    private void loadPatients() {
        loadPatients(null);
    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colInsuranceNumber.setCellValueFactory(new PropertyValueFactory<>("insuranceNumber"));
        colBirthDate.setCellValueFactory(new PropertyValueFactory<>("bitrthDate"));
        colBirthDate.setCellFactory(column -> buildDateCell(formatDate));

        tblPatients.setItems(patients);
        tblPatients.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        currentPatient = newSelection;
                        btnDelete.setDisable(false);
                    }
                });

        tblPatients.setRowFactory(tv -> {
            TableRow<Patient> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    getPatientId().thenAccept(patient -> Platform.runLater(() -> {
                        if(patient != null){
                            Stage stage = ScreenUtils.createViewModal(
                                    "/com/example/physiocare/patients/PatientDetailView.fxml",
                                    "PhysioCare - Patient Details");

                            if (stage != null) {
                                PatientDetailViewController controller =
                                        (PatientDetailViewController) stage.getScene().getRoot().getUserData();
                                controller.setShowPatientMoreInfo(patient);
                                controller.postInit();

                                stage.setOnHidden(Windows -> loadPatients());
                                stage.showAndWait();
                            }
                        }else {
                            MessageUtils.showError("Error to found patient" ,
                                    "The patient selected not found or error occurred");
                        }
                    }));
                }
            });
            return row;
        });
    }

    private CompletableFuture<PatientMoreInfo> getPatientId(){
        String url = ServiceUtils.SERVER + "/patients/" + currentPatient.getId();

        return ServiceUtils.getResponseAsync(url, null,"GET").thenApply(json -> {
            System.out.println("DEBUR - FIND ID PATIENT Respuesta JSON:" + json);
            PatientMoreInfoResponse resp = gson.fromJson(json, PatientMoreInfoResponse.class);
            if(resp != null && resp.isOk() && resp.getPatient() != null){
                return resp.getPatient();
            }else {
                return null;
            }
        }).exceptionally(ex -> {
            System.out.println(ex.getMessage());
            Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
            ex.printStackTrace();
            return  null;
        });
    }

    private void loadPatients(String searchQuery) {

        String url = ServiceUtils.SERVER + (searchQuery != null && !searchQuery.isEmpty()
                ? "/patients/find?search=" + searchQuery
                : "/patients");

        ServiceUtils.getResponseAsync(url, null, "GET").thenApply(json -> {
            System.out.println("DEBUG - Respuesta JSON: " + json);
            return gson.fromJson(json, PatientListResponse.class);
        }).thenAccept(response -> Platform.runLater(() -> {

            if (response == null || !response.isOk()) {
                System.out.println("No se encontraron pacientes o hubo un error");
                patients.clear();
                MessageUtils.showError("Error", "The patient list could not be loaded or " +
                        "no results were found");
            } else {
                System.out.println(response.getPatients().size());
                patients.setAll(response.getPatients());
                clearSelection();
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
            ex.printStackTrace();
            return null;
        });
    }

    private void clearSelection() {
        tblPatients.getSelectionModel().clearSelection();
        currentPatient = null;
        btnDelete.setDisable(true);
    }

    private TableCell<Patient, Date> buildDateCell(SimpleDateFormat formatter) {
        return new TableCell<>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : formatter.format(item));
            }
        };
    }

    public void handleViewPhysios(ActionEvent actionEvent) throws IOException {
//        ChangeButtons();
        Stage stage = (Stage) txtSearch.getScene().getWindow();
        ScreenUtils.loadView(stage, "/com/example/physiocare/physios/PhysiosView.fxml",
                "PhysioCare - Physios");

    }

    private void ChangeButtons(){
        btnViewPhysios.setVisible(!btnViewPhysios.isVisible());
        btnViewPhysios.setManaged(!btnViewPhysios.isManaged());
        btnViewPhysios.setDisable(!btnViewPhysios.isDisable());
    }
    public void handleViewProfile(ActionEvent actionEvent) {
    }

    public void handleLogout(ActionEvent actionEvent) throws IOException {
        ServiceUtils.logout();
        Stage stage = (Stage) txtSearch.getScene().getWindow();
        ScreenUtils.loadView(stage, "/com/example/physiocare/login-view.fxml", "PhysioCare - Login");
    }

    public void handleAddPatient(ActionEvent actionEvent) {
        Stage stage = ScreenUtils.createViewModal(
                "/com/example/physiocare/patients/PatientDetailView.fxml",
                "PhysioCare - Patient Details");

        if(stage != null){
            PatientDetailViewController controller =
                    (PatientDetailViewController) stage.getScene().getRoot().getUserData();
            controller.setAddPatient(true);
            controller.postInit();
            stage.setOnHidden(Windows -> loadPatients());
            stage.showAndWait();
        }
    }

    public void handleEditPatient(ActionEvent actionEvent) {
    }

    public void handleDeletePatient(ActionEvent actionEvent) {
        if (currentPatient == null) return;
        String message = "Are you sure you want to delete " + currentPatient.getName() + "?";
        MessageUtils.showConfirmation("Delete Patient", message, "Delete patient").showAndWait()
                .ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        DeletePatient();
                    }
                });
    }

    private void DeletePatient() {
        String url = ServiceUtils.SERVER + "/patients/" + currentPatient.getId();

        ServiceUtils.getResponseAsync(url, null, "DELETE").thenApply(json -> gson.fromJson(json,
                PatientResponse.class)).thenAccept(responseApi -> Platform.runLater(() -> {
            if (responseApi != null && responseApi.isOk()) {
                MessageUtils.showMessage("Success", "Patient deleted successfully");
                loadPatients();
            } else {
                MessageUtils.showError("Error deleting patient",
                        "The patient to be eliminated is not found");
            }
        })).exceptionally(ex -> {
            Platform.runLater(() ->
                    MessageUtils.showError("Error deleting patient", ex.getLocalizedMessage()));
            return null;
        });
    }


    public void handleConfirmAppointments(ActionEvent actionEvent) {
        String url = ServiceUtils.SERVER + "/records/appointments/unconfirmed";

        ServiceUtils.getResponseAsync(url, null, "GET").thenApply(json -> {
            System.out.println("DEBUG - JSON Response: " + json);
            return gson.fromJson(json, AppoinmentListResponse.class);
        }).thenAccept(response -> Platform.runLater(() -> {
            if (response == null || !response.isOk() || response.getResult() == null || response.getResult().isEmpty()) {
                System.out.println("No unconfirmed appointments found or an error occurred");
                MessageUtils.showError("Error", "No unconfirmed appointments found");
            } else {
                ScreenUtils.loadViewModal("/com/example/physiocare/appointment/ConfirmAppointmentView.fxml",
                        "PhysioCare - Appointments Confirm");
                System.out.println("Appointments loaded: " + response.getResult().size());
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
            ex.printStackTrace();
            return null;
        });
    }

    @Override
    public void setRole(String role) {
        if ("physio".equals(role)) {
            btnViewPhysios.setVisible(false);
            btnViewPhysios.setManaged(false);
        }
    }
}
