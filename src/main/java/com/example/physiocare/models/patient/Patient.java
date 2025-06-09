package com.example.physiocare.models.patient;
import com.example.physiocare.models.user.User;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.util.Date;

public class Patient extends PatientBase {
    private Date bitrthDate;
    private String address;
    private String insuranceNumber;
    private String user;
    private Record record;

    public Patient() {
        super();
    }

    public Patient(String id, String name, String surname, Date bitrthDate, String address, String insuranceNumber, String user) {
        super(id, name, surname);
        this.bitrthDate = bitrthDate;
        this.address = address;
        this.insuranceNumber = insuranceNumber;
        this.user = user;
    }

    public Date getBitrthDate() {
        return bitrthDate;
    }

    public void setBitrthDate(Date bitrthDate) {
        this.bitrthDate = bitrthDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public void setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber = insuranceNumber;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
