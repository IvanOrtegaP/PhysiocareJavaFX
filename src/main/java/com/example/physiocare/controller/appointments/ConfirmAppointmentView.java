package com.example.physiocare.controller.appointments;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ConfirmAppointmentView {
    @FXML
    public TextField txtSearch;
    @FXML
    public Button btnConfirm;
    @FXML
    public Button btnCancel;
    @FXML
    public TableView tableAppointments;
    @FXML
    public TableColumn colDate;
    @FXML
    public TableColumn colTime;
    @FXML
    public TableColumn colPatient;
    @FXML
    public TableColumn colConfirmed;
}
