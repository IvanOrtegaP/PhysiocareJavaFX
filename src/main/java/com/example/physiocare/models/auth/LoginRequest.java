package com.example.physiocare.models.auth;

public class LoginRequest {
    String login;
    String password;

    public LoginRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }
}