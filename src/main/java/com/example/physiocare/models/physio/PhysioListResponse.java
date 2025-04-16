package com.example.physiocare.models.physio;

import com.example.physiocare.models.patient.Patient;

public class PhysioListResponse {
    private Physio[] result;

    public PhysioListResponse() {

    }

    public Physio[] getResult() {
        return result;
    }

    public void setResult(Physio[] result) {
        this.result = result;
    }
}
