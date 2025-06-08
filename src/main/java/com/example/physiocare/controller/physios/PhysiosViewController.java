package com.example.physiocare.controller.physios;

import com.example.physiocare.models.appointment.AppoinmentListResponse;
import com.example.physiocare.models.patient.PatientMoreInfoResponse;
import com.example.physiocare.models.physio.*;
import com.example.physiocare.utils.MessageUtils;
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
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class PhysiosViewController implements Initializable {
    private final Gson gson = new Gson();
    private final ObservableList<Physio> physios = FXCollections.observableArrayList();
    public Label lblLoggedUser;
    public TextField txtSearch;
    public Button btnAdd;
    public Button btnDelete;
    public TableView<Physio> tblPhysios;
    public TableColumn<Physio, String> colName;
    public TableColumn<Physio, String> colSurname;
    public TableColumn<Physio, String> colSpecialty;
    public TableColumn<Physio, String> colLicenseNumber;
    PauseTransition pause = new PauseTransition(Duration.millis(600));
    private Physio curretPhysio = null;

    private void loadPhysios() {
        loadPhysios(null);
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            pause.setOnFinished(event -> loadPhysios(newValue.trim()));
            pause.playFromStart();
        });
    }

    private void loadPhysios(String searchQuery) {
        String url = ServiceUtils.SERVER + (searchQuery != null && !searchQuery.isEmpty()
                ? "/physios/find?search=" + searchQuery
                : "/physios");
        ServiceUtils.getResponseAsync(url, null, "GET").thenApply(json -> {
            System.out.println("DEBUG - Respuesta JSON: " + json);
            return gson.fromJson(json, PhysioListResponse.class);
        }).thenAccept(response -> Platform.runLater(() -> {
            if (response == null || !response.isOk()) {
                System.out.println("No se encontraron pacientes o hubo un error");
                physios.clear();
                MessageUtils.showError("Error", "The physio list could not be loaded or no results were found");
            } else {
                System.out.println(response.getPhysios().size());
                physios.setAll(response.getPhysios());
                clearSelection();
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
            ex.printStackTrace();
            return null;
        });
    }

    private void clearSelection() {
        tblPhysios.getSelectionModel().clearSelection();
        curretPhysio = null;
        btnDelete.setDisable(true);
    }


    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colLicenseNumber.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));
        colSpecialty.setCellValueFactory(new PropertyValueFactory<>("specialty"));

        tblPhysios.setItems(physios);
        tblPhysios.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        curretPhysio = newSelection;
                        btnDelete.setDisable(false);
                    }
                });

        tblPhysios.setRowFactory(tv -> {
            TableRow<Physio> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    getPhysioId().thenAccept(physio -> Platform.runLater(() -> {
                        if(physio != null){
                            Stage stage = ScreenUtils.createViewModal("/com/example/physiocare/physios/PhysiosDetailView.fxml",
                                    "PhysioCare - Physio Details");

                            if (stage != null) {
                                PhysiosDetailViewController controller =
                                        (PhysiosDetailViewController) stage.getScene().getRoot().getUserData();
                                controller.setShowPhysioMoreInfo(physio);
                                controller.postInit();

                                stage.setOnHidden(windows -> loadPhysios());
                                stage.showAndWait();
                            }
                        }else {
                            MessageUtils.showError("Error to found physio" ,
                                    "The physio selected not found or error occurred");
                        }
                    }));
                }
            });
            return row;
        });

    }

    private CompletableFuture<PhysioMoreInfo> getPhysioId(){
        String url = ServiceUtils.SERVER + "/physios/" + curretPhysio.getId();

        return ServiceUtils.getResponseAsync(url, null, "GET").thenApply(json -> {
            System.out.println("DEBUG - FIND ID Physio Respuesta JSON:" + json);
            PhysioMoreInfoResponse resp = gson.fromJson(json, PhysioMoreInfoResponse.class);
            if(resp != null && resp.isOk() && resp.getPhysio() != null){
                return resp.getPhysio();
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

    private void DeletePhysio() {
        String url = ServiceUtils.SERVER + "/physios/" + curretPhysio.getId();

        ServiceUtils.getResponseAsync(url, null, "DELETE").thenApply(json -> {
            System.out.println("DEBUG DELETE - Respuesta JSON: " + json);
            return gson.fromJson(json, PhysioResponse.class);
        }).thenAccept(response -> Platform.runLater(() -> {
            if (response != null && response.isOk()) {
                MessageUtils.showMessage("Success", "Physio deleted successfully");
                loadPhysios();
            } else {
                MessageUtils.showError("Error deleting physio",
                        "The physio to be eliminated is not found");
            }
        })).exceptionally(ex -> {
            ex.printStackTrace();
            Platform.runLater(() ->
                    MessageUtils.showError("Error deleting patient", ex.getLocalizedMessage()));

            return null;
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lblLoggedUser.setText("Welcome , " + ServiceUtils.getLogin());
        setupTable();
        loadPhysios();
    }

    public void handleViewPatients(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) txtSearch.getScene().getWindow();
        ScreenUtils.loadView(stage, "/com/example/physiocare/patients/PatientsView.fxml",
                "PhysioCare - Patients");
    }

    public void handleViewProfile(ActionEvent actionEvent) {
    }

    public void handleConfirmAppointments(ActionEvent actionEvent) {
        String url = ServiceUtils.SERVER + "/records/appointments/unconfirmed";

        ServiceUtils.getResponseAsync(url, null, "GET").thenApply(json -> gson.fromJson(json,
                AppoinmentListResponse.class)).thenAccept(response -> Platform.runLater(() -> {

            if (response == null || !response.isOk()) {
                System.out.println("No se encontraron pacientes o hubo un error");
                MessageUtils.showError("Error", "The physio list could not be loaded or no results were found");
            } else {
                ScreenUtils.loadViewModal("/com/example/physiocare/appointment/ConfirmAppointmentView.fxml",
                        "PhysioCare - Appointments Confirm");
                System.out.println(response.getResult().size());
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> MessageUtils.showError("Error", ex.getMessage()));
            return null;
        });
    }

    public void handleLogout(ActionEvent actionEvent) throws IOException {
        ServiceUtils.logout();
        Stage stage = (Stage) txtSearch.getScene().getWindow();
        ScreenUtils.loadView(stage, "/com/example/physiocare/login-view.fxml", "PhysioCare - Login");
    }

    public void handleAddPhysio(ActionEvent actionEvent) {
        Stage stage = ScreenUtils.createViewModal("/com/example/physiocare/physios/PhysiosDetailView.fxml",
                "PhysioCare - Physio Add");

        if (stage != null) {
            PhysiosDetailViewController controller = (PhysiosDetailViewController) stage.getScene().getRoot().getUserData();
            controller.setAddPhysio(true);
            controller.postInit();
            stage.setOnHidden(Windows -> loadPhysios());
            stage.showAndWait();
        }
    }

    public void handleDeletePhysio(ActionEvent actionEvent) {
        if (curretPhysio == null) return;

        String message = "Are you sure you want to delete " + curretPhysio.getName() + "?";
        MessageUtils.showConfirmation("Delete Patient", message, "Delete patient").showAndWait()
                .ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        DeletePhysio();
                    }
                });
    }
}
