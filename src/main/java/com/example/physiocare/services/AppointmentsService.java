package com.example.physiocare.services;

import com.example.physiocare.models.BaseResponse;
import com.example.physiocare.models.appointment.AppoinmentListResponse;
import com.example.physiocare.models.appointment.Appointment;
import com.example.physiocare.models.appointment.AppointmentResponse;
import com.example.physiocare.models.record.Record;
import com.example.physiocare.utils.ServiceUtils;
import com.google.gson.Gson;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AppointmentsService {
    private static final String URL_RECORDS = ServiceUtils.SERVER + "/records/";
    private static final Gson gson = new Gson();


    public static CompletableFuture<AppointmentResponse> saveAppointment(Record record, Appointment appointment) {

        if (record == null || appointment == null) {
            CompletableFuture<AppointmentResponse> failed = new CompletableFuture<>();
            System.out.println("Record or appointment cannot be null");
            failed.completeExceptionally(new IllegalArgumentException("Record or appointment cannot be null"));
            return failed;
        }

        String url = URL_RECORDS + record.getId() + "/appointments";
        String requestAppoinment = gson.toJson(appointment);

        return ServiceUtils.getResponseAsync(url, requestAppoinment, "POST").thenApply(json -> {
            System.out.println("DEBUG - JSON POST APPOINTMENT : " + json);
            return gson.fromJson(json, AppointmentResponse.class);
        });
    }

    public static CompletableFuture<BaseResponse> deleteAppointment(Record record, Appointment appointment) {
        if (record == null || appointment == null) {
            CompletableFuture<BaseResponse> failed = new CompletableFuture<>();
            System.out.println("Record or appointment cannot be null");
            failed.completeExceptionally(new IllegalArgumentException("Record or appointment cannot be null"));
            return failed;
        }
        String url = URL_RECORDS + record.getId() + "/appointments/" + appointment.getId();

        return ServiceUtils.getResponseAsync(url, null, "DELETE").thenApply(json -> {
            System.out.println("Debug DELETE APPOINTMENT : " + json);
            return gson.fromJson(json, BaseResponse.class);
        });
    }

    public static CompletableFuture<BaseResponse> putAppointment(Record record, Appointment appointment) {
        if (record == null || appointment == null) {
            CompletableFuture<BaseResponse> failed = new CompletableFuture<>();
            System.out.println("Record or appointment cannot be null");
            failed.completeExceptionally(new IllegalArgumentException("Record or appointment cannot be null"));
            return failed;
        }

        String url = URL_RECORDS + record.getId() + "/appointments/" + appointment.getId();
        String requestAppoinment = gson.toJson(appointment);

        return ServiceUtils.getResponseAsync(url, requestAppoinment, "PUT").thenApply(json -> {
            System.out.println("Debug PUT APPOINTMENT : " + json);
            return gson.fromJson(json, BaseResponse.class);
        });
    }

    public static CompletableFuture<BaseResponse> ConfirmAppointments(Appointment appointment) {
        String id = Optional.ofNullable(appointment).map(Appointment::getId).orElse(null);
        String idRecord = Optional.ofNullable(appointment).map(Appointment::getRecordId).orElse(null);

        if(id == null || idRecord == null){
            CompletableFuture<BaseResponse> failed = new CompletableFuture<>();
            System.out.println("No appointment selected");
            failed.completeExceptionally(new IllegalArgumentException("No appointment selected"));
            return failed;
        }

        String url = URL_RECORDS + idRecord + "/appointments/" + id + "/confirm";

        return ServiceUtils.getResponseAsync(url, null, "PUT").thenApply(json -> {
            System.out.println("DEBUG PUT CONFIRM APPOINTMENT : " + json);
            return gson.fromJson(json, BaseResponse.class);
        });
    }

    public static CompletableFuture<AppoinmentListResponse> getUnconfirmedAppointments(String searchQuery) {
        String url = URL_RECORDS + "appointments/unconfirmed";

        if (searchQuery != null && !searchQuery.isEmpty()) {
            url += "?search=" + searchQuery;
        }

        return ServiceUtils.getResponseAsync(url, null, "GET").thenApply(json -> {
            System.out.println("Debug GET UNCONFIRMED APPOINTMENTS : " + json);
            return gson.fromJson(json, AppoinmentListResponse.class);
        });
    }
}
