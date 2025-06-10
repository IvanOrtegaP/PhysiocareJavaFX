package com.example.physiocare.controller.users;

import com.example.physiocare.controller.physios.PhysiosDetailViewController.Specialties;
import com.example.physiocare.models.BaseResponse;
import com.example.physiocare.models.physio.PhysioMoreInfo;
import com.example.physiocare.models.user.User;
import com.example.physiocare.services.PhysiosService;
import com.example.physiocare.services.UserService;
import com.example.physiocare.utils.ImageUtils;
import com.example.physiocare.utils.MessageUtils;
import com.example.physiocare.utils.ServiceUtils;
import com.example.physiocare.utils.ValidateUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class UserProfileController implements Initializable {
    public Button btnClose;
    public Button btnEdit;
    public Button btnSave;
    public TextField txtUsername;
    public Label lblPassword;
    public PasswordField pfPassword;
    public TextField txtEmail;
    public TextField txtName;
    public TextField txtSurname;
    public ChoiceBox<Specialties> cbSpecialty;
    public TextField txtLicenseNumber;
    public VBox VBoxPhysio;
    public VBox VBoxMain;
    public ImageView imgAvatar;
    public VBox VBoxUser;
    private boolean isAdmin = false;
    private PhysioMoreInfo showPhysio;
    private User profileAdmin;

    public void setShowPhysio(PhysioMoreInfo showPhysio) {
        this.showPhysio = showPhysio;
    }


    public void setProfileAdmin(User profileAdmin) {
        this.profileAdmin = profileAdmin;
    }

    public void handleClose(ActionEvent actionEvent) {
        if (btnSave.isVisible()) {
            populateForm();
            DisableForm(true);
            DisableButtons();
        }else {
            ((Stage) btnClose.getScene().getWindow()).close();
        }
    }

    public void handleEdit(ActionEvent actionEvent) {
        DisableForm(false);
        DisableButtons();
    }

    public void handleSave(ActionEvent actionEvent) {
        if(!validateForm()) return;

        if(showPhysio != null){
            EditPhysio();
        } else if (profileAdmin != null) {
            EditUser();
        }
    }

    private void  EditUser() {
        if (!validateForm()) return;

        User userAdmin = getUser();

        UserService.putProfile(userAdmin).thenAccept(resp -> Platform.runLater(() -> {
            if(resp != null && resp.isOk() && resp.getUser() != null){
                profileAdmin = resp.getUser();
                populateForm();
                DisableForm(true);
                DisableButtons();
            }else {
                String error = Optional.ofNullable(resp).map(BaseResponse::getErrorMessage).orElse("Unknown error");
                MessageUtils.showError("Error update physio", error);
            }
        })).exceptionally(ex-> {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            MessageUtils.showError("Error update profile admin", ex.getMessage());
            return null;
        });
    }

    private void EditPhysio() {
        if(!validateForm()) return;
        PhysioMoreInfo physioMoreInfo = getShowPhysioMoreInfo();

        PhysiosService.putPhysio(physioMoreInfo).thenAccept(resp -> Platform.runLater(() -> {
            if(resp != null && resp.isOk() && resp.getPhysio() != null){
                showPhysio = resp.getPhysio();
                populateForm();
                DisableForm(true);
                DisableButtons();
            }else {
                String error = Optional.ofNullable(resp).map(BaseResponse::getErrorMessage).orElse("Unknown error");
                MessageUtils.showError("Error update profile physio", error);
            }
        })).exceptionally(ex -> {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            MessageUtils.showError("Error update profile physio", ex.getMessage());
            return null;
        });
    }

    public void postInit() {
        if(profileAdmin != null){
            isAdmin = true;
            VBoxPhysio.setDisable(true);
            VBoxPhysio.setVisible(false);
            VBoxPhysio.setManaged(false);
            VBoxMain.setMaxSize(VBoxMain.getMaxHeight(), ((VBoxMain.getHeight() - VBoxPhysio.getHeight()) + 250));
            populateForm();
        } else if (showPhysio != null) {
            populateForm();
            isAdmin = false;
        }
    }

    public void populateForm() {
        User show = null;
        if (profileAdmin != null) {
            show = profileAdmin;
        } else if (showPhysio != null) {

            txtName.setText((showPhysio.getName() != null
                    ? showPhysio.getName()
                    : "No tiene nombre asignado"));

            txtSurname.setText((showPhysio.getSurname() != null
                    ? showPhysio.getSurname()
                    : "No tiene apellido asignado"));

            txtLicenseNumber.setText(showPhysio.getLicenseNumber() != null
                    ? showPhysio.getLicenseNumber()
                    : "No tiene license number asignado");

            try {
                cbSpecialty.setValue(Specialties.valueOf(showPhysio.getSpecialty().toUpperCase()));
            } catch (IllegalArgumentException | NullPointerException e) {
                cbSpecialty.setValue(null);
            }
            show = showPhysio.getUser();
        }

        Optional.ofNullable(show).ifPresentOrElse(user -> {
            Image avatar = ImageUtils.createImageViewFromBase64(user.getAvatar());
            imgAvatar.setImage(avatar);
            txtUsername.setText(Optional.ofNullable(user.getLogin()).orElse("No tiene nombre de usuario asignado"));
            txtEmail.setText(Optional.ofNullable(user.getEmail()).orElse("No tiene email asignado"));
        }, () -> {
            txtUsername.setText("No tiene nombre de usuario asignado");
            txtEmail.setText("No tiene email asignado");
        });
    }

    public void DisableButtons() {
        btnSave.setDisable(!btnSave.isDisable());
        btnSave.setVisible(!btnSave.isVisible());
        btnSave.setManaged(!btnSave.isManaged());
        btnEdit.setVisible(!btnEdit.isVisible());
        btnEdit.setDisable(!btnEdit.isDisable());
        btnEdit.setManaged(!btnEdit.isManaged());
    }

    public void DisableForm(boolean disable) {
        VBoxPhysio.setDisable(disable);
        VBoxUser.setDisable(disable);
    }

    private User getUser() {
        String idUser = null;
        String rol = ServiceUtils.getRol();

        if(profileAdmin != null){
            idUser = Optional.of(profileAdmin).map(User::getId).orElse(null);
        }else if(showPhysio != null) {
             idUser = Optional.of(showPhysio)
                    .map(PhysioMoreInfo::getUser)
                    .map(User::getId)
                    .orElse(null);

        }

        if(idUser != null && rol != null){
            return new User(
                    idUser,
                    txtUsername.getText().trim(),
                    txtEmail.getText().trim(),
                    rol
            );
        }else {
            return null;
        }
    }

    private PhysioMoreInfo getShowPhysioMoreInfo() {
        User user = getUser();

        String idPhysio = Optional.ofNullable(showPhysio)
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

    private boolean validateForm() {

        if(showPhysio != null){
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

            if(cbSpecialty.getValue() == null){
                MessageUtils.showError("Validation Error", "Specialty is required");
                return false;
            }
        }

        if (!ValidateUtils.validateNotEmpty(txtUsername.getText())) {
            MessageUtils.showError("Validation Error", "Username are required");
            return false;
        }

        if (!ValidateUtils.validateNotEmpty(txtEmail.getText())) {
            MessageUtils.showError("Validation Error", "Email is required");
            return false;
        }

        if(!ValidateUtils.validateLength(txtUsername.getText() ,4 , 500)){
            MessageUtils.showError("Validation Error", "The username must be longer than 4 characters.");
            return false;
        }

        if (!ValidateUtils.validateRegex(txtEmail.getText(), Pattern.compile("^\\S+@\\S+\\.\\S+$"))) {
            MessageUtils.showError("Validation Error", "The email does not comply with the correct format");
            return false;
        }

        return true;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        VBoxMain.setUserData(this);
        cbSpecialty.getItems().addAll(Specialties.values());
        cbSpecialty.getSelectionModel().selectFirst();
        btnSave.setDisable(true);
        btnSave.setVisible(false);
        btnSave.setManaged(false);
        DisableForm(true);
    }
}
