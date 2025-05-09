package com.example.physiocare.models.patient;

import com.example.physiocare.models.BaseResponse;

import java.util.List;

public class PatientListResponse extends BaseResponse {
    private List<Patient> result;

    public List<Patient> getPatients() {
        return result;
    }

    public void setResult(List<Patient> result) {
        this.result = result;
    }
}

