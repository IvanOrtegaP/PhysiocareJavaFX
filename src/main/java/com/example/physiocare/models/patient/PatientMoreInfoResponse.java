package com.example.physiocare.models.patient;

import com.example.physiocare.models.BaseResponse;

public class PatientMoreInfoResponse extends BaseResponse {
    PatientMoreInfo result;


    public PatientMoreInfo getPatient() {
        return result;
    }

    public String getId() {
        return result != null ? result.getId() : null;
    }
}
