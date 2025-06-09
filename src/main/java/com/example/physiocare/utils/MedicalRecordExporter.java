package com.example.physiocare.utils;

import com.example.physiocare.models.appointment.Appointment;
import com.example.physiocare.models.patient.Patient;
import com.example.physiocare.models.record.Record;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MedicalRecordExporter {

    private static final String API_URL = "http://bellon.shop:8080/records";
    private static final String SFTP_HOST = "bellon.shop";
    private static final int SFTP_PORT = 22;
    private static final String SFTP_USER = "ivan";
    private static final String SFTP_PASS = "Bellon.Ivan1";
    private static final String SFTP_DIR = "/home/ivan/output/records";

    public static void exportMedicalRecords(String outputDirectory) {
        try {

            File outputDir = new File(outputDirectory);
            if (!outputDir.exists()) {
                if (!outputDir.mkdirs()) {
                    throw new IOException("Failed to create output directory: " + outputDirectory);
                }
            }


            List<Record> records = fetchRecordsFromApi();


            for (Record record : records) {
                if (record.getPatient() != null) {
                    File outputFile = new File(outputDir,
                            record.getPatient().getName() + "_" +
                                    record.getPatient().getSurname() + ".pdf");
                    generatePdf(record, outputFile);
                    uploadFileViaSftp(outputFile);
                }
            }

            System.out.println("Medical records exported and uploaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error exporting or uploading medical records: " + e.getMessage());
        }
    }

    private static List<Record> fetchRecordsFromApi() throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");

        String token = ServiceUtils.getToken();
        if (token != null) {
            connection.setRequestProperty("Authorization", "Bearer " + token);
        }

        if (connection.getResponseCode() != 200) {
            throw new RuntimeException("Failed to fetch records: HTTP " + connection.getResponseCode());
        }


        String response;
        try (Scanner scanner = new Scanner(connection.getInputStream())) {
            response = scanner.useDelimiter("\\A").next();
        }


        Gson gson = new Gson();
        JsonElement jsonElement = JsonParser.parseString(response);
        JsonObject jsonResponse = jsonElement.getAsJsonObject();

        List<Record> validRecords = new ArrayList<>();

        if (jsonResponse.get("ok").getAsBoolean()) {
            JsonArray recordsArray = jsonResponse.getAsJsonArray("result");

            for (JsonElement recordElement : recordsArray) {
                JsonObject recordObject = recordElement.getAsJsonObject();
                JsonElement patientElement = recordObject.get("patient");


                if (patientElement != null && patientElement.isJsonObject()) {
                    try {
                        Record record = gson.fromJson(recordObject, Record.class);
                        validRecords.add(record);
                    } catch (JsonSyntaxException e) {
                        System.err.println("Skipping malformed record: " + recordObject);
                    }
                }
            }
        }

        return validRecords;
    }

    private static void generatePdf(Record record, File file) throws Exception {
        Patient patient = record.getPatient();

        try (PdfWriter writer = new PdfWriter(new FileOutputStream(file));
             Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer))) {

            document.add(new Paragraph("MEDICAL RECORD").setBold().setFontSize(16));
            document.add(new Paragraph("Record ID: " + record.getId()));
            document.add(new Paragraph("Patient: " + patient.getName() + " " + patient.getSurname()));

            document.add(new Paragraph("\nMEDICAL HISTORY:").setBold());
            document.add(new Paragraph(record.getMedicalRecord() != null ? record.getMedicalRecord() : "No medical history available"));

            document.add(new Paragraph("\nAPPOINTMENTS:").setBold());

            if (record.getAppointments() != null && !record.getAppointments().isEmpty()) {
                for (Appointment appointment : record.getAppointments()) {
                    document.add(new Paragraph("------------------------------------"));
                    document.add(new Paragraph("Date: " + appointment.getDate()));
                    document.add(new Paragraph("Diagnosis: " + appointment.getDiagnosis()));
                    document.add(new Paragraph("Treatment: " + appointment.getTreatment()));
                    document.add(new Paragraph("Observations: " + appointment.getObservations()));
                    document.add(new Paragraph("Status: " + appointment.getConfirm()));

                }
            } else {
                document.add(new Paragraph("No appointments found"));
            }
        }
    }

    private static void uploadFileViaSftp(File file) {
        Session session = null;
        ChannelSftp sftpChannel = null;

        try {

            if (SFTP_DIR == null || SFTP_DIR.trim().isEmpty()) {
                throw new IllegalArgumentException("SFTP directory path is not configured");
            }

            JSch jsch = new JSch();
            session = jsch.getSession(SFTP_USER, SFTP_HOST, SFTP_PORT);
            session.setPassword(SFTP_PASS);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;


            String[] folders = SFTP_DIR.split("/");
            StringBuilder pathBuilder = new StringBuilder();

            for (String folder : folders) {
                if (folder.isEmpty()) continue;

                pathBuilder.append("/").append(folder);
                String currentPath = pathBuilder.toString();

                try {
                    sftpChannel.cd(currentPath);
                } catch (Exception e) {

                    sftpChannel.mkdir(currentPath);
                    sftpChannel.cd(currentPath);
                    System.out.println("Created remote directory: " + currentPath);
                }
            }

            sftpChannel.put(new FileInputStream(file), file.getName());
            System.out.println("Successfully uploaded: " + file.getName());

        } catch (Exception e) {
            System.err.println("SFTP upload failed for " + file.getName() + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (sftpChannel != null) sftpChannel.exit();
            if (session != null) session.disconnect();
        }
    }

    public static void main(String[] args) {
        exportMedicalRecords("output/records");
    }

    private static class ApiResponse {
        private boolean ok;
        private List<Record> result;

        public boolean isOk() {
            return ok;
        }

        public List<Record> getResult() {
            return result;
        }
    }
}