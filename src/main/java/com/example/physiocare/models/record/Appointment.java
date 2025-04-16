package com.example.physiocare.models.record;

import com.example.physiocare.models.physio.Physio;

import java.util.Date;

public class Appointment {
    
    private Date date;
    private String physio;
    private String diagnosis;
    private String treatment;
    private String observations;

    public Appointment(Date date, String physio, String diagnosis, String treatment, String observations) {
        this.date = date;
        this.physio = physio;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.observations = observations;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPhysio() {
        return physio;
    }

    public void setPhysio(String physio) {
        this.physio = physio;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }
}
