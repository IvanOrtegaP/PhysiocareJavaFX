package com.example.physiocare.services;

import com.example.physiocare.models.BaseResponse;
import com.example.physiocare.models.physio.*;
import com.example.physiocare.utils.ServiceUtils;
import com.google.gson.Gson;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PhysiosService {
    private static final String URL_PHYSIOS = ServiceUtils.SERVER + "/physios";
    private static final Gson gson = new Gson();

    public static CompletableFuture<PhysioListResponse> getPhysios(String searchQuery){
        String url = URL_PHYSIOS + ((searchQuery != null && !searchQuery.isEmpty()) ?
                "/find?search=" + searchQuery
                : "");

        return ServiceUtils.getResponseAsync(url, null, "GET").thenApply(json -> {
            System.out.println("DEBUG - Respuesta JSON: " + json);
            return gson.fromJson(json, PhysioListResponse.class);
        });
    }

    public static CompletableFuture<PhysioMoreInfoResponse> getPhysioId(Physio physio) {
        String id = Optional.ofNullable(physio).map(Physio::getId).orElse(null);
        if(id == null){
            CompletableFuture<PhysioMoreInfoResponse> failed = new CompletableFuture<>();
            System.out.println("The physio cannot be null");
            failed.completeExceptionally(new IllegalArgumentException("The physio cannot be null"));
            return failed;
        }
        String url = URL_PHYSIOS + "/" + id;

        return ServiceUtils.getResponseAsync(url, null, "GET").thenApply(json -> {
            System.out.println("DEBUG - FIND ID Physio Respuesta JSON:" + json);
            return gson.fromJson(json, PhysioMoreInfoResponse.class);
        });
    }

    public static CompletableFuture<PhysioResponse> savePhysio(PhysioMoreInfo physioMoreInfo) {

        if(physioMoreInfo == null){
            CompletableFuture<PhysioResponse> failed = new CompletableFuture<>();
            System.out.println("The physio cannot be null");
            failed.completeExceptionally(new IllegalArgumentException("The physio cannot be null"));
            return failed;
        }
        String requestPhysio = gson.toJson(physioMoreInfo);

        return ServiceUtils.getResponseAsync(URL_PHYSIOS, requestPhysio, "POST").thenApply(json -> {
            System.out.println("DEBUG - PHYSIO POST JSON : " +json );
            return gson.fromJson(json, PhysioResponse.class);
        });
    }

    public static CompletableFuture<PhysioMoreInfoResponse> putPhysio(PhysioMoreInfo physioMoreInfo){
        String id = Optional.ofNullable(physioMoreInfo).map(PhysioMoreInfo::getId).orElse(null);

        if(id == null){
            CompletableFuture<PhysioMoreInfoResponse> failed = new CompletableFuture<>();
            System.out.println("The physio cannot be null");
            failed.completeExceptionally(new IllegalArgumentException("The physio cannot be null"));
            return failed;
        }

        String url = URL_PHYSIOS + "/" + id;
        String requesPhysio = gson.toJson(physioMoreInfo);

        return ServiceUtils.getResponseAsync(url, requesPhysio, "PUT").thenApply(json -> {
            System.out.println("DEBUG- PHYSIO PUT JSON : " + json);
            return gson.fromJson(json, PhysioMoreInfoResponse.class);
        });
    }

    public static CompletableFuture<BaseResponse> DeletePhysio(Physio physio) {
        String id = Optional.ofNullable(physio).map(Physio::getId).orElse(null);
        if(id == null){
            CompletableFuture<BaseResponse> failed = new CompletableFuture<>();
            System.out.println("The physio cannot be null");
            failed.completeExceptionally(new IllegalArgumentException("The physio cannot be null"));
            return failed;
        }

        String url = ServiceUtils.SERVER + "/physios/" + id;

        return ServiceUtils.getResponseAsync(url, null, "DELETE").thenApply(json -> {
            System.out.println("DEBUG DELETE - Respuesta JSON: " + json);
            return gson.fromJson(json, BaseResponse.class);
        });
    }

    public static CompletableFuture<PhysioMoreInfoResponse> PhysioProfileMe(){

        if(ServiceUtils.getRol() == null || !ServiceUtils.getRol().equals("physio")){
            CompletableFuture<PhysioMoreInfoResponse> failed = new CompletableFuture<>();
            System.out.println("You have no role assigned or you do not have permissions");
            failed.completeExceptionally(new IllegalArgumentException("You have no role assigned or you do not have permissions"));
            return failed;
        }

        String url = URL_PHYSIOS + "/profile/me";

        System.out.println(url);

        return  ServiceUtils.getResponseAsync(url, null,"GET").thenApply(json -> {
            System.out.println("DEBUG GET PHYSIO PROFILE ME : " + json);
            return gson.fromJson(json, PhysioMoreInfoResponse.class);
        });
    }
}
