package com.example.physiocare.utils;

import com.example.physiocare.models.patient.Patient;
import com.example.physiocare.utils.ServiceUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class PatientService {
    private static final String ENDPOINT_URL = ServiceUtils.SERVER + "/patients/more-than-8-appointments";
    private static final Gson gson = new Gson();

    public static List<Patient> fetchPatientsWithAppointments() throws Exception {
        String jsonResponse = ServiceUtils.getResponseSync(ENDPOINT_URL, null, "GET");
        Type listType = new TypeToken<List<Patient>>() {}.getType();
        return gson.fromJson(jsonResponse, listType);
    }
}