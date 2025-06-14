package com.example.physiocare.models.record;

import com.example.physiocare.models.appointment.Appointment;
import com.example.physiocare.models.patient.Patient;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Record {

    @SerializedName("_id")
    private String id;
    private Patient patient;
    private String medicalRecord;
    private List<Appointment> appointments;

    public Record(){

    }

    public Record(List<Appointment> appointments, String id) {
        this.appointments = appointments;
        this.id = id;
    }

    public Record(Patient patient, String medicalRecord, List<Appointment> appointments) {
        this.patient = patient;
        this.medicalRecord = medicalRecord;
        this.appointments = appointments;
    }

    public Record(String id, Patient patient, String medicalRecord, List<Appointment> appointments) {
        this.id = id;
        this.patient = patient;
        this.medicalRecord = medicalRecord;
        this.appointments = appointments;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getMedicalRecord() {
        return medicalRecord;
    }

    public void setMedicalRecord(String medicalRecord) {
        this.medicalRecord = medicalRecord;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    @Override
    public String toString() {
        return "Record{" +
                "id='" + id + '\'' +
                ", patient='" + patient + '\'' +
                ", medicalRecord='" + medicalRecord + '\'' +
                ", appointments=" + appointments +
                '}';
    }
}

