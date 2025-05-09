package com.example.physiocare.models.auth;

public class LoginRequest {
    String usuario;
    String password;

    public LoginRequest(String usuario, String password) {
        this.usuario = usuario;
        this.password = password;
    }
}