package com.example.physiocare.models.record;

import com.example.physiocare.models.BaseResponse;

public class RecordResponse extends BaseResponse {
    private Record result;

    public Record getResult() {
        return result;
    }

    public void setResult(Record result) {
        this.result = result;
    }
}
