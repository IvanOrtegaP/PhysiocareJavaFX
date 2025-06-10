package com.example.physiocare.models.patient;

import com.example.physiocare.models.BaseResponse;

import java.util.List;

public class PatientMoreInfoListResponse extends BaseResponse {
    private List<PatientMoreInfo> result;

    public List<PatientMoreInfo> getPatients(){
        return result;
    }

    public void setPatients(List<PatientMoreInfo> result){
        this.result = result;
    }
}
