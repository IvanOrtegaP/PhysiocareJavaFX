module com.example.physiocare {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.physiocare.models.auth to com.google.gson;
    opens com.example.physiocare.models.physio to javafx.base, com.google.gson;
    //Controladores con las vistas donde estan
    opens com.example.physiocare to javafx.fxml;
    opens com.example.physiocare.controller.patients to javafx.fxml;
    opens com.example.physiocare.controller.physios to javafx.fxml;
    opens com.example.physiocare.controller.appointments to javafx.fxml;


    opens com.example.physiocare.models;
    opens com.example.physiocare.models.patient;
    opens com.example.physiocare.models.record;
    opens com.example.physiocare.models.appointment;
    opens com.example.physiocare.models.user;
    opens com.example.physiocare.utils;
    exports com.example.physiocare;
}