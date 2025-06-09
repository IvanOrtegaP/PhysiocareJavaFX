package com.example.physiocare.models.appointment;

import com.example.physiocare.models.patient.Patient;
import com.example.physiocare.models.physio.Physio;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Appointment {

    @SerializedName("_id")
    private String id;
    private Date date;
    private Physio physio;
    private String diagnosis;
    private String treatment;
    private String observations;
    private String recordId;
    private Patient patient;
    private String confirm;
    private Integer rating;

    public Appointment(Date date, Physio physio) {
        this.date = date;
        this.physio = physio;
    }

    public Appointment(Date date, Physio physio, String confirm) {
        this.date = date;
        this.physio = physio;
        this.confirm = confirm;
    }

    public Appointment(Date date, Physio physio, String diagnosis, String treatment, String observations) {
        this.date = date;
        this.physio = physio;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.observations = observations;
    }

    public Appointment(String id, Date date, Physio physio, String diagnosis, String treatment, String observations) {
        this.id = id;
        this.date = date;
        this.physio = physio;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.observations = observations;
    }

    public Appointment(Date date, Physio physio, Patient patient, String diagnosis, String treatment, String observations) {
        this.date = date;
        this.physio = physio;
        this.patient = patient;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.observations = observations;
    }

    public Appointment(String id, Date date, Physio physio, String diagnosis, String treatment, String observations, String confirm) {
        this.id = id;
        this.date = date;
        this.physio = physio;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.observations = observations;
        this.confirm = confirm;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Physio getPhysio() {
        return physio;
    }

    public void setPhysio(Physio physio) {
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

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getPatientName() {
        if (patient != null) {
            return patient.getName() + " " + patient.getSurname();
        }
        return "Unknown";
    }

    public String getConfirm() {
        return confirm;
    }

    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
