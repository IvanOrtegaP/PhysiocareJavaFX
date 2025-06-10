package com.example.physiocare.utils;

import com.example.physiocare.models.appointment.Appointment;
import com.example.physiocare.models.patient.PatientMoreInfoRecord;
import com.example.physiocare.models.record.Record;
import com.example.physiocare.models.user.User;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.*;
import java.util.*;

public class EmailSender {
    public static final String APPLICATION_NAME = "PhysioCare PSP";
    public static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH =
            "src/main/resources/client_secret_181008649046-095sucr2sj4u3jfav6t9ut894oovpleq.apps.googleusercontent.com.json";

    public static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws Exception {
        // Load client secrets.
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(new FileInputStream(CREDENTIALS_FILE_PATH)));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,
                Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM))
                .setDataStoreFactory(new FileDataStoreFactory(
                        new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    public static void sendMessage(Gmail service, String userId, MimeMessage emailContent) throws MessagingException, java.io.IOException {
        Message message = createMessageWithEmail(emailContent);
        message = service.users().messages().send(userId, message).execute();
        System.out.println("Message id: " + message.getId());
        System.out.println("Email sent successfully.");
    }

    public static Message createMessageWithEmail(MimeMessage email) throws MessagingException, java.io.IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = java.util.Base64.getUrlEncoder()
                .encodeToString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    public static MimeMessage createEmailWithAttachment(String to, String from, String subject, String bodyText, String fileDir)
            throws MessagingException {

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(bodyText, "text/plain");
        if (fileDir != null) {
            try {
                mimeBodyPart.attachFile(new java.io.File(fileDir));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);
        email.setContent(multipart);
        return email;
    }


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


    public static void sendReminders(PatientMoreInfoRecord patientMoreInfoRecord, Gmail service) {
        String email = Optional.ofNullable(patientMoreInfoRecord.getUser()).map(User::getEmail).orElse(null);
        List<Appointment> appointments = Optional.ofNullable(patientMoreInfoRecord.getRecord()).map(Record::getAppointments).orElse(new ArrayList<>());
        if (email != null) {
            String pdfPath = null;
            try {
                pdfPath = generateAppointmentsPdf(patientMoreInfoRecord.getName(), appointments);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            try {
                String emailBody = "Dear " + patientMoreInfoRecord.getName() + ",\n\n" +
                        "You have more than 8 appointments. Please note that you only have 2 more appointments left.\n\n" +
                        "Best regards,\nPhysioCare Team";
                //En el from debe de ir el correo de la empresa no el mio.
                MimeMessage emailContent = createEmailWithAttachment(email,
                        "Ivbegon04@gmail.com",
                        "Your 10-date ticket is running out.",
                        emailBody,
                        pdfPath
                );
                sendMessage(service, "me", emailContent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendNomina(){

    }
}