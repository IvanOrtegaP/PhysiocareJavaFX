package com.example.physiocare.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

public class ScreenUtils {
    public static void loadView(Stage stage, String view, String titleView) throws IOException {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(ScreenUtils.class.getResource(view)));
            stage.setScene(new Scene(root));
            stage.setTitle(titleView);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading view: " + view);
            e.printStackTrace();
            throw e;
        }
    }

    public static void loadViewWithRole(Stage stage, String view, String titleView, String role) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(ScreenUtils.class.getResource(view)));
        Parent root = loader.load();

        Object controller = loader.getController();
        if (controller instanceof RoleAwareController) {
            ((RoleAwareController) controller).setRole(role);
        }

        stage.setScene(new Scene(root));
        stage.setTitle(titleView);
        stage.show();
    }

    public static void loadViewModal(String view, String titleView){
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(ScreenUtils.class.getResource(view)));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.setScene(new Scene(root));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle(titleView);
            modalStage.showAndWait();
        }catch (Exception e){
            throw new RuntimeException("Error loading view: " + view, e);
        }
    }

    public static Stage createViewModal(String view, String titleView) {
        Stage modalStage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(ScreenUtils.class.getResource(view)));
            Parent root = loader.load();

            Object controller = loader.getController();
            root.setUserData(controller);

            modalStage.setScene(new Scene(root));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle(titleView);
            return modalStage;

        } catch (Exception e) {
            e.printStackTrace();
            MessageUtils.showError("Error loading view", e.getLocalizedMessage());
        }

        return null;
    }
}
