package com.example.physiocare.models.patient;

import com.example.physiocare.models.BaseResponse;

public class PatientResponse extends BaseResponse {
    Patient result;

    public Patient getResult() {
        return result;
    }

    public Patient getPatient() {
        return result;
    }

    public String getId() {
        return result != null ? result.getId() : null;
    }
}
