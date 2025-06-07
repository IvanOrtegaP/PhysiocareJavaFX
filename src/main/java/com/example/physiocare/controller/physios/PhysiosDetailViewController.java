package com.example.physiocare.controller.physios;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PhysiosDetailViewController implements Initializable {
    public TextField txtName;
    public TextField txtSurname;
    public ChoiceBox<Specialties> cbSpecialty;
    public TextField txtLicenseNumber;
    public TextField txtUsername;
    public PasswordField pfPassword;
    public TextField txtEmail;
    public Button btnClose;
    public Button btnEdit;
    public Button btnDelete;
    public Button btnSave;

    public enum Specialties {
        SPORTS, NEUROLOGICAL, PEDIATIC, GERIATRIC, ONCOLOGICAL;

        //Se sobre escribe el toString() para que sea mas legible
        @Override
        public String toString() {
            return switch (this) {
                case SPORTS -> "Sports";
                case NEUROLOGICAL -> "Neurological";
                case PEDIATIC -> "Pediatic";
                case GERIATRIC -> "Geriatric";
                case ONCOLOGICAL -> "Oncological";
            };
        }
    }

    public void postInit(){

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cbSpecialty.getItems().addAll(Specialties.values());
        cbSpecialty.getSelectionModel().selectFirst();
    }

    public void handleClose(ActionEvent actionEvent) {
        ((Stage) btnClose.getScene().getWindow()).close();
    }

    public void handleEdit(ActionEvent actionEvent) {
    }

    public void handleDelete(ActionEvent actionEvent) {
    }

    public void handleSave(ActionEvent actionEvent) {
    }
}
