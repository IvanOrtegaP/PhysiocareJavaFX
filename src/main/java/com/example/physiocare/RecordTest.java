package com.example.physiocare;

import com.example.physiocare.models.record.Appointment;
import com.example.physiocare.models.record.Record;
import com.example.physiocare.models.patient.Patient;
import com.example.physiocare.utils.ServiceUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class RecordTest {

    public static void main(String[] args) {
        Patient patient = new Patient("6602f4c2f2f43f6f5db4e936", "Carlos", "García", null, "Calle Falsa 123", "A1B2C3D4E", "carlos@correo.com");

        String physioId = "661bc0b2c79a2a90ab345678";

        // Crear cita médica
        Appointment appointment = new Appointment(
                new Date(),
                physioId,
                "Dolor lumbar crónico, necesita seguimiento semanal",
                "Ejercicios de estiramiento y terapia manual",
                "Paciente responde bien a terapia, pero necesita más sesiones"
        );

        List<Appointment> appointments = Arrays.asList(appointment);

        // Crear el record
        Record record = new Record(patient, "Historial médico breve del paciente", appointments);

        // Convertir a JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(record);

        System.out.println("JSON a enviar:\n" + json);

        // Enviar al servidor (POST /records)
        String url = ServiceUtils.SERVER + "/records";
        String response = ServiceUtils.getResponse(url, json, "POST");

        System.out.println("Respuesta del servidor:\n" + response);
    }
}
