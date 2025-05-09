package com.example.physiocare.models.physio;

import com.example.physiocare.models.BaseResponse;

public class PhysioResponse extends BaseResponse {
    private Physio result;

    public Physio getResult() {
        return result;
    }

    public void setResult(Physio result) {
        this.result = result;
    }

    public String getId() {
        return result != null ? result.getId() : null;
    }
}
