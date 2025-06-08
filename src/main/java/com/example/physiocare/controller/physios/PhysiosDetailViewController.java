package com.example.physiocare.controller.physios;

import com.example.physiocare.models.patient.PatientResponse;
import com.example.physiocare.models.physio.PhysioMoreInfo;
import com.example.physiocare.models.physio.PhysioMoreInfoResponse;
import com.example.physiocare.models.physio.PhysioResponse;
import com.example.physiocare.models.user.User;
import com.example.physiocare.utils.MessageUtils;
import com.example.physiocare.utils.ServiceUtils;
import com.example.physiocare.utils.ValidateUtils;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class PhysiosDetailViewController implements Initializable {
    private final Gson gson = new Gson();

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
    public VBox VBoxMain;
    public Label lblPassword;

    private Boolean addPhysio = false;
    private PhysioMoreInfo showPhysioMoreInfo;

    public void setShowPhysioMoreInfo(PhysioMoreInfo showPhysioMoreInfo) {
        this.showPhysioMoreInfo = showPhysioMoreInfo;
    }

    public void setAddPhysio(Boolean add){
        this.addPhysio = (add != null ? add : false) ;
    }

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

    private void DisableForm(Boolean disable) {
        txtName.setDisable(disable);
        txtSurname.setDisable(disable);
        cbSpecialty.setDisable(disable);
        txtEmail.setDisable(disable);
        txtLicenseNumber.setDisable(disable);
        txtUsername.setDisable(disable);
        pfPassword.setDisable(disable);
    }

    public void populateForm() {
        if (showPhysioMoreInfo != null) {
            txtName.setText((showPhysioMoreInfo.getName() != null ? showPhysioMoreInfo.getName() : "No tiene nombre asignado"));
            txtSurname.setText((showPhysioMoreInfo.getSurname() != null ? showPhysioMoreInfo.getSurname() : "No tiene apellido asignado"));
            txtLicenseNumber.setText(showPhysioMoreInfo.getLicenseNumber() != null ? showPhysioMoreInfo.getLicenseNumber() : "No tiene license number asignado");

            Optional.ofNullable(showPhysioMoreInfo.getUser()).ifPresentOrElse(user -> {
                txtUsername.setText(Optional.ofNullable(user.getLogin()).orElse("No tiene nombre de usuario asignado"));
                txtEmail.setText(Optional.ofNullable(user.getEmail()).orElse("No tiene email asignado"));
            }, () -> {
                txtUsername.setText("No tiene nombre de usuario asignado");
                txtEmail.setText("No tiene email asignado");
            });
        }
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

        if (!ValidateUtils.validateNotEmpty(txtLicenseNumber.getText())) {
            MessageUtils.showError("Validation Error", "The license number is required");
            return false;
        }

        if (!ValidateUtils.validateRegex(txtLicenseNumber.getText(), Pattern.compile("^[a-zA-Z0-9]{8}$"))) {
            MessageUtils.showError("Validation Error", "The license number must be 8 alphanumeric characters");
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

        if(cbSpecialty.getValue() == null){
            MessageUtils.showError("Validation Error", "Specialty is required");
            return false;
        }

        if(addPhysio){
            if(!ValidateUtils.validateNotEmpty(pfPassword.getText())){
                MessageUtils.showError("Validation Error", "Password is required");
                return false;
            }

            if(!ValidateUtils.validateLength(pfPassword.getText(), 7 , 255)){
                MessageUtils.showError("Validation Error", "The password cannot be less than 7 characters");
                return false;
            }
        }

        if (!ValidateUtils.validateRegex(txtEmail.getText(), Pattern.compile("^\\S+@\\S+\\.\\S+$"))) {
            MessageUtils.showError("Validation Error", "The email does not comply with the correct format");
            return false;
        }
        return true;
    }

    private void EditPhysio() {
        if(!validateForm()) return;

        String url = ServiceUtils.SERVER + "/physios/" + showPhysioMoreInfo.getId();

        PhysioMoreInfo physioMoreInfo = getShowPhysioMoreInfo();

        String PhysioJson = gson.toJson(physioMoreInfo);

        ServiceUtils.getResponseAsync(url, PhysioJson, "PUT").thenApply(json -> {
            System.out.println("DEBUG- PHYSIO PUT JSON : " + json);
            return gson.fromJson(json, PhysioMoreInfoResponse.class);
        }).thenAccept(resp -> Platform.runLater(() -> {
            if(resp != null && resp.isOk()){
                showPhysioMoreInfo = resp.getPhysio();
                populateForm();
                DisableForm(true);
            }else {
                String error = Optional.ofNullable(resp.getErrorMessage()).orElse("Unknown error");
                MessageUtils.showError("Error update physio", error);
            }
        })).exceptionally(ex -> {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            MessageUtils.showError("Error update physio", ex.getMessage());
            return null;
        });

    }

    private void savePhysio() {
        if(!validateForm()) return;

        String url = ServiceUtils.SERVER + "/physios";

        PhysioMoreInfo physioMoreInfo = getShowPhysioMoreInfo();

        String PhysioJson = gson.toJson(physioMoreInfo);

        ServiceUtils.getResponseAsync(url, PhysioJson, "POST").thenApply(json -> {
            System.out.println("DEBUG - PHYSIO POST JSON : " +json );
            return gson.fromJson(json, PhysioResponse.class);
        }).thenAccept(resp -> Platform.runLater(() -> {
            if(resp != null && resp.isOk()){
                ((Stage) btnClose.getScene().getWindow()).close();
            }else {
                String error = Optional.ofNullable(resp.getErrorMessage()).orElse("Unknown error");
                MessageUtils.showError("Error post physio", error);
            }
        })).exceptionally(ex -> {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            MessageUtils.showError("Error post physio", ex.getMessage());
            return  null;
        });
    }

    private PhysioMoreInfo getShowPhysioMoreInfo() {
        String idUser = Optional.ofNullable(showPhysioMoreInfo)
                .map(PhysioMoreInfo::getUser)
                .map(User::getId)
                .orElse(null);

        User user = new User(
                idUser,
                txtUsername.getText().trim(),
                pfPassword.getText().trim(),
                txtEmail.getText().trim(),
                "physio"
        );

        String idPhysio = Optional.ofNullable(showPhysioMoreInfo)
                .map(PhysioMoreInfo::getId)
                .orElse(null);

        return new PhysioMoreInfo(
                idPhysio,
                txtName.getText().trim(),
                txtSurname.getText().trim(),
                cbSpecialty.getValue().toString().trim(),
                txtLicenseNumber.getText().trim(),
                user
        );
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

    public void postInit(){
        if(addPhysio){
            DisableButtons();
        } else if (showPhysioMoreInfo != null) {
            pfPassword.setDisable(true);
            pfPassword.setManaged(false);
            pfPassword.setVisible(false);
            lblPassword.setManaged(false);
            lblPassword.setVisible(false);
            populateForm();
            DisableForm(true);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        VBoxMain.setUserData(this);
        cbSpecialty.getItems().addAll(Specialties.values());
        cbSpecialty.getSelectionModel().selectFirst();
        btnSave.setDisable(true);
        btnSave.setVisible(false);
        btnSave.setManaged(false);
    }

    public void handleClose(ActionEvent actionEvent) {
        ((Stage) btnClose.getScene().getWindow()).close();
    }

    public void handleEdit(ActionEvent actionEvent) {
        DisableForm(false);
        DisableButtons();

    }

    public void handleDelete(ActionEvent actionEvent) {
        if (showPhysioMoreInfo == null) return;

        String message = "Are you sure you want to delete " + showPhysioMoreInfo.getName() + "?";

        MessageUtils.showConfirmation("Delete Physio", message, "Delete Physio").showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                DeletePhysio();
                //Hace la accion de cerrar la ventana modal
                ((Stage) btnClose.getScene().getWindow()).close();
            }
        });
    }

    private void DeletePhysio() {
        String url = ServiceUtils.SERVER + "/physios/" + showPhysioMoreInfo.getId();

        ServiceUtils.getResponseAsync(url, null, "DELETE")
                .thenApply(json -> gson.fromJson(json, PhysioResponse.class))
                .thenAccept(responseApi -> Platform.runLater(() -> {
                    if (responseApi != null && responseApi.isOk()) {
                        MessageUtils.showMessage("Success", "Physio deleted successfully");
                    } else {
                        MessageUtils.showError("Error deleting physio", "The physio to be eliminated is not found");
                    }
                })).exceptionally(ex -> {
                    Platform.runLater(() -> MessageUtils.showError("Error deleting physio", ex.getLocalizedMessage()));
                    return null;
                });
    }

    public void handleSave(ActionEvent actionEvent) {
        if(addPhysio){
            savePhysio();
        }else if (showPhysioMoreInfo != null) {
            EditPhysio();
            DisableButtons();
        }
    }
}
