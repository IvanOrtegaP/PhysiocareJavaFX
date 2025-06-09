package com.example.physiocare.controller.patients;

import com.example.physiocare.models.patient.Patient;
import com.example.physiocare.services.AppointmentsService;
import com.example.physiocare.services.PatientsService;
import com.example.physiocare.utils.MessageUtils;
import com.example.physiocare.utils.RoleAwareController;
import com.example.physiocare.utils.ScreenUtils;
import com.example.physiocare.utils.ServiceUtils;
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

public class PatientsViewController implements Initializable, RoleAwareController {
    private final ObservableList<Patient> patients = FXCollections.observableArrayList();
    private final SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");
    public Label lblLoggedUser;
    public TextField txtSearch;
    public Button btnAdd;
    public Button btnDelete;
    public TableView<Patient> tblPatients;
    public TableColumn<Patient, String> colName;
    public TableColumn<Patient, String> colSurname;
    public TableColumn<Patient, String> colInsuranceNumber;
    public TableColumn<Patient, Date> colBirthDate;
    public Button btnViewPhysios;
    //Mete dilay a la busqueda por texto
    PauseTransition pause = new PauseTransition(Duration.millis(600));
    private Patient currentPatient = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadPatients();
        lblLoggedUser.setText("Welcome , " + ServiceUtils.getLogin());
        //No se inciara la busqueda asta que pase 600 ms despues de que el usuario halla dejado de escribir
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            pause.setOnFinished(event -> loadPatients(newValue.trim()));
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
                    PatientsService.getPatientId(currentPatient).thenAccept(resp -> Platform.runLater(() -> {
                        if (resp != null && resp.isOk() && resp.getPatient() != null) {
                            Stage stage = ScreenUtils.createViewModal(
                                    "/com/example/physiocare/patients/PatientDetailView.fxml",
                                    "PhysioCare - Patient Details");

                            if (stage != null) {
                                PatientDetailViewController controller =
                                        (PatientDetailViewController) stage.getScene().getRoot().getUserData();
                                controller.setShowPatientMoreInfo(resp.getPatient());
                                controller.postInit();

                                stage.setOnHidden(Windows -> loadPatients());
                                stage.showAndWait();
                            }
                        } else {
                            MessageUtils.showError("Error to found patient",
                                    "The patient selected not found or error occurred");
                        }
                    })).exceptionally(ex -> {
                        System.out.println(ex.getMessage());
                        Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
                        ex.printStackTrace();
                        return null;
                    });
                }
            });
            return row;
        });
    }

    private void loadPatients(String searchQuery) {
        PatientsService.getPatients(searchQuery).thenAccept(resp -> Platform.runLater(() -> {
            if (resp == null || !resp.isOk()) {
                System.out.println("No se encontraron pacientes o hubo un error");
                patients.clear();
                MessageUtils.showError("Error", "The patient list could not be loaded or " +
                        "no results were found");
            } else {
                System.out.println(resp.getPatients().size());
                patients.setAll(resp.getPatients());
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
        Stage stage = (Stage) txtSearch.getScene().getWindow();
        ScreenUtils.loadView(stage, "/com/example/physiocare/physios/PhysiosView.fxml",
                "PhysioCare - Physios");

    }

    private void ChangeButtons() {
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

        if (stage != null) {
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
        PatientsService.deletePatient(currentPatient).thenAccept(resp -> Platform.runLater(() -> {
            if (resp != null && resp.isOk()) {
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
        AppointmentsService.getUnconfirmedAppointments(null).thenAccept(resp -> Platform.runLater(() -> {
            if (resp == null || !resp.isOk()) {
                System.out.println("No se encontraron pacientes o hubo un error");
                MessageUtils.showError("Error", "The physio list could not be loaded or no results were found");
            } else {
                ScreenUtils.loadViewModal("/com/example/physiocare/appointment/ConfirmAppointmentView.fxml",
                        "PhysioCare - Appointments Confirm");
                System.out.println(resp.getResult().size());
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
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

    public void handleViewOptions(ActionEvent actionEvent) {
        ScreenUtils.loadViewModal("/com/example/physiocare/options-view.fxml", "PhysioCare - Options");

    }
}
