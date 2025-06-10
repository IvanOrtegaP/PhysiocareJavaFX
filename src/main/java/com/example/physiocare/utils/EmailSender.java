package com.example.physiocare.utils;

import com.example.physiocare.models.appointment.Appointment;
import com.example.physiocare.models.patient.Patient;
import com.example.physiocare.services.AppointmentsService;
import com.example.physiocare.services.PatientsService;
import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;

public class EmailSender {

    public static String generateAppointmentsPdf(String patientName, List<Appointment> appointments) throws Exception {
        String filePath = "output/" + patientName.replace(" ", "_") + "_appointments.pdf";
        PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
        Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer));

        document.add(new Paragraph("Appointments for " + patientName));
        Table table = new Table(4);
        table.addCell("Date");
        table.addCell("Diagnosis");
        table.addCell("Treatment");
        table.addCell("Observations");

        for (Appointment appointment : appointments) {
            table.addCell(appointment.getDate().toString());
            table.addCell(appointment.getDiagnosis());
            table.addCell(appointment.getTreatment());
            table.addCell(appointment.getObservations());
        }

        document.add(table);
        document.close();
        return filePath;
    }

    public static void sendEmail(String recipient, String subject, String body, String attachmentPath) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.example.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("your-email@example.com", "your-password");
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("your-email@example.com"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(body);

        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setDataHandler(new DataHandler(new FileDataSource(attachmentPath)));
        attachmentPart.setFileName("Appointments.pdf");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(attachmentPart);

        message.setContent(multipart);
        Transport.send(message);
    }

    public static void sendReminders() {
        try {
            // Fetch all patients
            PatientsService.getPatients(null).thenAccept(patientResponse -> {
                if (patientResponse != null && patientResponse.isOk()) {
                    List<Patient> patients = patientResponse.getPatients();

                    for (Patient patient : patients) {
                        // Fetch appointments for each patient
                        AppointmentsService.getAppointmentsByPatientID(patient.getId()).thenAccept(appointmentResponse -> {
                            if (appointmentResponse != null && appointmentResponse.isOk()) {
                                List<Appointment> appointments = (List<Appointment>) appointmentResponse.getResult();

                                if (appointments.size() > 8) {
                                    try {
                                        // Generate PDF for appointments
                                        String pdfPath = generateAppointmentsPdf(patient.getName(), appointments);

                                        // Prepare email body
                                        String emailBody = "Dear " + patient.getName() + ",\n\n" +
                                                "You have more than 8 appointments. Please note that you only have 2 more appointments left.\n\n" +
                                                "Attached is a summary of your appointments.\n\n" +
                                                "Best regards,\nPhysioCare Team";

                                        // Send email
                                        sendEmail(patient.getUser(), "Appointment Reminder", emailBody, pdfPath);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).exceptionally((Throwable ex) -> {
                            ex.printStackTrace();
                            return null;
                        });
                    }
                }
            }).exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}