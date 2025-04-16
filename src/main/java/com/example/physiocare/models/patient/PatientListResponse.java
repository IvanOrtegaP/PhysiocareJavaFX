package com.example.physiocare.models.patient;

import com.example.physiocare.models.BaseResponse;

public class PatientListResponse extends BaseResponse {
    private Patient[] result;

    public Patient[] getPatients() {
        return result;
    }

    public void setResult(Patient[] result) {
        this.result = result;
    }
}

