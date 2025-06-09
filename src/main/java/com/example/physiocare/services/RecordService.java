package com.example.physiocare.services;

import com.example.physiocare.models.patient.Patient;
import com.example.physiocare.models.patient.PatientMoreInfo;
import com.example.physiocare.models.physio.PhysioMoreInfoResponse;
import com.example.physiocare.models.record.Record;
import com.example.physiocare.models.record.RecordResponse;
import com.example.physiocare.utils.ServiceUtils;
import com.google.gson.Gson;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RecordService {
    private static final String URL_RECORDS = ServiceUtils.SERVER + "/records";
    private static final Gson gson = new Gson();


    public static CompletableFuture<RecordResponse> getRecordPatient(Patient patient){
        String id = Optional.ofNullable(patient).map(Patient::getId).orElse(null);

        if(id == null){
            CompletableFuture<RecordResponse> failed = new CompletableFuture<>();
            System.out.println("The record cannot be null");
            failed.completeExceptionally(new IllegalArgumentException("The record cannot be null"));
            return failed;
        }

        String url = URL_RECORDS + "/patient/" + id;


        return ServiceUtils.getResponseAsync(url , null, "GET").thenApply(json -> {
            System.out.println("DEBUG GET RECORD PATIENT ID : " + json);
            return gson.fromJson(json, RecordResponse.class);
        });
    }
}
