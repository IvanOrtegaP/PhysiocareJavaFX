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
    public static void loadView(Stage stage , String view, String titleView) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(ScreenUtils.class.getResource(view)));
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

    public static Stage createViewModal(String view , String titleView){
        Stage modalStage = new Stage();
        try{
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(ScreenUtils.class.getResource(view)));
            Parent root = loader.load();

            modalStage.setScene(new Scene(root));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle(titleView);
            return  modalStage;

        }catch (Exception e){
            e.printStackTrace();
            MessageUtils.showError("Error to loading view" , e.getLocalizedMessage());
        }

        return  null;
    }
}
