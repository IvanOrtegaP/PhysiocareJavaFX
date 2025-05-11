package com.example.physiocare.models.appointment;

import com.example.physiocare.models.BaseResponse;

public class AppointmentResponse extends BaseResponse {
    private Appointment result;

    public Appointment getResult() {
        return result;
    }

    public void setResult(Appointment result) {
        this.result = result;
    }
}
