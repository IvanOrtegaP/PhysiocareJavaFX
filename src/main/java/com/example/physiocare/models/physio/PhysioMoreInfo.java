package com.example.physiocare.models.physio;

import com.example.physiocare.models.user.User;

public class PhysioMoreInfo extends PhysioBase{
    private String specialty;
    private String licenseNumber;
    private User user;

    public PhysioMoreInfo(){
        super();
    }

    public PhysioMoreInfo(String id, String name, String surname, String specialty, String licenseNumber, User user) {
        super(id, name, surname);
        this.specialty = specialty;
        this.licenseNumber = licenseNumber;
        this.user = user;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
