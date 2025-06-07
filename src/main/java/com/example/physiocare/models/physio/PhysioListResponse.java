package com.example.physiocare.models.physio;

import com.example.physiocare.models.BaseResponse;
import com.example.physiocare.models.patient.Patient;

import java.util.List;

public class PhysioListResponse extends BaseResponse {
    private List<Physio> result;

    public List<Physio> getPhysios() {
        return result;
    }

    public void setPhysios(List<Physio> result) {
        this.result = result;
    }
}
