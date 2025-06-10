package com.example.physiocare.services;

import com.example.physiocare.models.physio.PhysioMoreInfoResponse;
import com.example.physiocare.models.user.User;
import com.example.physiocare.models.user.UserResponse;
import com.example.physiocare.utils.ServiceUtils;
import com.google.gson.Gson;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UserService {
    private static final Gson gson = new Gson();

    public static CompletableFuture<UserResponse> getProfile() {
        if(ServiceUtils.getRol() == null){
            CompletableFuture<UserResponse> failed = new CompletableFuture<>();
            System.out.println("You have no role assigned");
            failed.completeExceptionally(new IllegalArgumentException("You have no role assigned"));
            return failed;
        }

        String url = ServiceUtils.SERVER + "/auth/profile";

        return ServiceUtils.getResponseAsync(url, null, "GET").thenApply(json -> {
            System.out.println("DEBUG GET PROFILE USER : " + json);
            return gson.fromJson(json, UserResponse.class);
        });
    }

    public static CompletableFuture<UserResponse> putProfile(User user) {
        String id = Optional.ofNullable(user).map(User::getId).orElse(null);

        if(id == null){
            CompletableFuture<UserResponse> failed = new CompletableFuture<>();
            System.out.println("The user cannot be null");
            failed.completeExceptionally(new IllegalArgumentException("The user cannot be null"));
            return failed;
        }

        String url = ServiceUtils.SERVER + "/auth/profile";
        String requestUser = gson.toJson(user);

        return ServiceUtils.getResponseAsync(url, requestUser, "PUT").thenApply(json -> {
            System.out.println("DEBUG PUT PROFILE USER : " + json);
            return gson.fromJson(json, UserResponse.class);
        });
    }
}
