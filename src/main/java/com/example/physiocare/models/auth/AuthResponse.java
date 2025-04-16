package com.example.physiocare.models.auth;

public class AuthResponse {
    private String result;

    public String getToken() {
        return result;
    }

    public boolean isOk() {
        return result != null;
    }
}

