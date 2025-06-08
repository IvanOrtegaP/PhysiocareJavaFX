package com.example.physiocare.models.physio;

import com.example.physiocare.models.BaseResponse;

public class PhysioMoreInfoResponse extends BaseResponse {
    private PhysioMoreInfo result;

    public PhysioMoreInfo getPhysio() {
        return result;
    }

    public void setPhysio(PhysioMoreInfo physioMoreInfo){
        this.result = physioMoreInfo;
    }
}
