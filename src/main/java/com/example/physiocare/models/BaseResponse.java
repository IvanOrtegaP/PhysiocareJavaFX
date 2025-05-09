package com.example.physiocare.models;

public class BaseResponse {
    private boolean ok;
    private String error;

    public String getErrorMessage() {
        return error;
    }

    public boolean isOk() {
        return ok;
    }

}
