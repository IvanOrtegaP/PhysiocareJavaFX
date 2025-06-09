package com.example.physiocare.services;

import com.example.physiocare.models.BaseResponse;
import com.example.physiocare.models.patient.*;
import com.example.physiocare.models.physio.Physio;
import com.example.physiocare.models.physio.PhysioMoreInfoResponse;
import com.example.physiocare.utils.ServiceUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PatientsService {
    private static final String URL_PATIENTS = ServiceUtils.SERVER + "/patients";
    private static final String ENDPOINT_URL = ServiceUtils.SERVER + "/patients/more-than-8-appointments";
    private static final Gson gson = new Gson();

    public static List<Patient> fetchPatientsWithAppointments() throws Exception {
        String jsonResponse = ServiceUtils.getResponseSync(ENDPOINT_URL, null, "GET");
        Type listType = new TypeToken<List<Patient>>() {}.getType();
        return gson.fromJson(jsonResponse, listType);
    }

    public static CompletableFuture<PatientListResponse> getPatients(String searchQuery) {
        String url = URL_PATIENTS + ((searchQuery != null && !searchQuery.isEmpty()) ?
                "/find?search=" + searchQuery
                : "");

        return ServiceUtils.getResponseAsync(url, null, "GET").thenApply(json -> {
            System.out.println("DEBUG - Respuesta JSON: " + json);
            return gson.fromJson(json, PatientListResponse.class);
        });
    }

    public static CompletableFuture<PatientMoreInfoResponse> getPatientId(Patient patient){
        String id = Optional.ofNullable(patient).map(Patient::getId).orElse(null);

        if(id == null){
            CompletableFuture<PatientMoreInfoResponse> failed = new CompletableFuture<>();
            System.out.println("The patient cannot be null");
            failed.completeExceptionally(new IllegalArgumentException("The patient cannot be null"));
            return failed;
        }

        String url = URL_PATIENTS + "/" + id;

        return ServiceUtils.getResponseAsync(url, null, "GET").thenApply(json ->{
            System.out.println("DEBUR - FIND ID PATIENT Respuesta JSON:" + json);
            return gson.fromJson(json, PatientMoreInfoResponse.class);
        });
    }

    public static CompletableFuture<PatientResponse> savePatient(PatientMoreInfo patientMoreInfo){
        if(patientMoreInfo == null){
            CompletableFuture<PatientResponse> failed = new CompletableFuture<>();
            System.out.println("The patient cannot be null");
            failed.completeExceptionally(new IllegalArgumentException("The patient cannot be null"));
            return failed;
        }

        String requestPatient = gson.toJson(patientMoreInfo);

        return ServiceUtils.getResponseAsync(URL_PATIENTS, requestPatient, "POST").thenApply(json -> {
            System.out.println("DEBUG- PATIENT POST JSON : " + json);
            return gson.fromJson(json, PatientResponse.class);
        });
    }

    public static CompletableFuture<PatientMoreInfoResponse> putPatient(PatientMoreInfo patientMoreInfo){
        String id = Optional.ofNullable(patientMoreInfo).map(PatientMoreInfo::getId).orElse(null);

        if(id == null){
            CompletableFuture<PatientMoreInfoResponse> failed = new CompletableFuture<>();
            System.out.println("The patient cannot be null");
            failed.completeExceptionally(new IllegalArgumentException("The patient cannot be null"));
            return failed;
        }

        String url = URL_PATIENTS + "/" + id;
        String requestPatient = gson.toJson(patientMoreInfo);

        return ServiceUtils.getResponseAsync(url, requestPatient, "PUT").thenApply(json -> {
            System.out.println("DEBUG- PATIENT PUT JSON : " + json);
            return gson.fromJson(json, PatientMoreInfoResponse.class);
        });
    }

    public static CompletableFuture<BaseResponse> deletePatient(Patient patient) {
        String id = Optional.ofNullable(patient).map(Patient::getId).orElse(null);
        if(id == null){
            CompletableFuture<BaseResponse> failed = new CompletableFuture<>();
            System.out.println("The patient cannot be null");
            failed.completeExceptionally(new IllegalArgumentException("The patient cannot be null"));
            return failed;
        }
        String url = URL_PATIENTS + "/" + id;

        return ServiceUtils.getResponseAsync(url, null, "DELETE").thenApply(json -> {
            System.out.println("DEBUG DELETE PATIENT - Respuesta JSON: " + json);
            return gson.fromJson(json, BaseResponse.class);
        });
    }
}