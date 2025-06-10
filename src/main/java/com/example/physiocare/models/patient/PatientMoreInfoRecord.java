package com.example.physiocare.models.patient;

import com.example.physiocare.models.record.Record;
import com.example.physiocare.models.user.User;

import java.util.Date;

public class PatientMoreInfoRecord  extends PatientMoreInfo {

    private Record record;

    public PatientMoreInfoRecord(String id, String name, String surname, Date bitrthDate, String address, String insuranceNumber, User user, Record record) {
        super(id, name, surname, bitrthDate, address, insuranceNumber, user);
        this.record = record;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }
}
