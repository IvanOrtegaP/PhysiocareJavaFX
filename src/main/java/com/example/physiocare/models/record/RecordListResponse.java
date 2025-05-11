package com.example.physiocare.models.record;

import com.example.physiocare.models.BaseResponse;

import java.util.List;

public class RecordListResponse extends BaseResponse {
    private List<Record> result;

    public List<Record> getResult() {
        return result;
    }

    public void setResult(List<Record> result) {
        this.result = result;
    }
}
