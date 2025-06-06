package com.example.physiocare.models.patient;

import com.example.physiocare.models.user.User;

import java.util.Date;

public class PatientMoreInfo extends PatientBase {

    private Date bitrthDate;
    private String address;
    private String insuranceNumber;
    private User user;

    public PatientMoreInfo(){
        super();
    }

    public PatientMoreInfo(String id, String name, String surname, Date bitrthDate, String address, String insuranceNumber, User user){
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
