package com.example.physiocare.models.physio;

public class PhysioResponse {
    private Physio result;

    public Physio getResult() {
        return result;
    }

    public void setResult(Physio result) {
        this.result = result;
    }

    public String getId() {
        return result != null ? result.getId() : null;
    }
}
