package com.example.physiocare.models.patient;

import com.google.gson.annotations.SerializedName;

public class PatientBase {
    @SerializedName("_id")
    private String id;
    private String name;
    private String surname;

    public PatientBase() {
    }

    public PatientBase(String id, String name, String surname) {
        this.id = id;
        this.name = name;
        this.surname = surname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}
