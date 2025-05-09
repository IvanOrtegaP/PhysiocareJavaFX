module com.example.physiocare {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.physiocare.models.auth to com.google.gson;
    opens com.example.physiocare.models.physio to javafx.base, com.google.gson;
    opens com.example.physiocare to javafx.fxml;
    opens com.example.physiocare.controller to javafx.fxml;
    opens com.example.physiocare.models;
    opens com.example.physiocare.utils;
    exports com.example.physiocare;
    opens com.example.physiocare.models.patient;
}