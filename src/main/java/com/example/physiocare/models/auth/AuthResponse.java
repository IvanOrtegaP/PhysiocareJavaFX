package com.example.physiocare.models.auth;

public class AuthResponse {
    private boolean ok;
    private String token;
    private String login;
    private String rol;
    private String id;

    public boolean isOk() {
        return ok;
    }

    public String getToken() {
        return token;
    }

    public String getLogin() {
        return login;
    }

    public String getRol() {
        return rol;
    }

    public String getId() {
        return id;
    }
}

