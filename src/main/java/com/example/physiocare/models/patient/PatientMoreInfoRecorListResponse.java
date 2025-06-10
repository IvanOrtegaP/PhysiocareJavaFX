package com.example.physiocare.models.patient;

import com.example.physiocare.models.BaseResponse;

import java.util.List;

public class PatientMoreInfoRecorListResponse extends BaseResponse {
    private List<PatientMoreInfoRecord> result;

    public List<PatientMoreInfoRecord> getPatients(){
        return result;
    }

    public void setPatients(List<PatientMoreInfoRecord> result){
        this.result = result;
    }
}
