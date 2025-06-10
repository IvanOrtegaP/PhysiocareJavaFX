package com.example.physiocare.models.physio;

import com.example.physiocare.models.BaseResponse;

import java.util.List;

public class PhysioMoreInfoListResponse extends BaseResponse {
    private List<PhysioMoreInfo> result;

    public List<PhysioMoreInfo> getPhysios() {
        return result;
    }

    public void setPhysios(List<PhysioMoreInfo> physioMoreInfo){
        this.result = physioMoreInfo;
    }
}
