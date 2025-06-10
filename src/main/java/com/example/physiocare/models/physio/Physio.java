package com.example.physiocare.models.physio;

import com.example.physiocare.models.user.UserResponse;
import com.example.physiocare.services.PhysiosService;
import com.google.gson.annotations.SerializedName;


public class Physio extends PhysioBase {
    private String specialty;
    private String licenseNumber;
    private String user;

    public Physio(){
        super();
    }

    public Physio(String id, String name, String surname, String specialty, String licenseNumber, String user) {
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

    public String getUser() {
        return user;
    }

    public String getEmail() {
        try {
            UserResponse userResponse = PhysiosService.getPhysioUser(this.getId()).join();

            if (userResponse != null && userResponse.getUser() != null) {
                return userResponse.getUser().getEmail();
            }
        } catch (Exception e) {
            System.err.println("Error fetching email for physio: " + this.getName());
            e.printStackTrace();
        }

        return null;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return super.getName();
    }
}