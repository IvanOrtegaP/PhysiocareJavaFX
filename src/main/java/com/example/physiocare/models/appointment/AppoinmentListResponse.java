package com.example.physiocare.models.appointment;

import com.example.physiocare.models.BaseResponse;

import java.util.List;

public class AppoinmentListResponse extends BaseResponse {
    private List<Appointment> result;

    public List<Appointment> getResult() {
        return result;
    }

    public void setResult(List<Appointment> result) {
        this.result = result;
    }
}
