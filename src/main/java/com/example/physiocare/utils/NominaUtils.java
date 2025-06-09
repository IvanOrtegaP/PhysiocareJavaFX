package com.example.physiocare.utils;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.google.gson.Gson;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class NominaUtils {

    private static final String ENDPOINT_URL = "http://localhost:3000/:id/appointments-per-month";
    private static final int APPOINTMENT_FEE = 40;

    public static void generatePayroll(String physioId, int year, int month, String outputFilePath) {
        try {
            String queryUrl = ENDPOINT_URL.replace(":id", physioId) + "?year=" + year + "&month=" + month;
            HttpURLConnection connection = (HttpURLConnection) new URL(queryUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Error al consumir el endpoint: " + connection.getResponseCode());
            }

            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            Gson gson = new Gson();
            AppointmentResponse appointmentResponse = gson.fromJson(response.toString(), AppointmentResponse.class);

            int totalAppointments = appointmentResponse.getTotalAppointments();
            int totalEarnings = totalAppointments * APPOINTMENT_FEE;

            PdfWriter writer = new PdfWriter(new FileOutputStream(outputFilePath));
            Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer));

            document.add(new Paragraph("Nómina del Fisioterapeuta"));
            document.add(new Paragraph("ID del Fisioterapeuta: " + physioId));
            document.add(new Paragraph("Mes: " + month + " / Año: " + year));
            document.add(new Paragraph("Total de citas: " + totalAppointments));
            document.add(new Paragraph("Ingresos totales: " + totalEarnings + " euros"));

            document.close();
            System.out.println("Nómina generada exitosamente en: " + outputFilePath);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al generar la nómina: " + e.getMessage());
        }
    }

    private static class AppointmentResponse {
        private String physioId;
        private int year;
        private int month;
        private int totalAppointments;

        public int getTotalAppointments() {
            return totalAppointments;
        }
    }

    public static void main(String[] args) {
        generatePayroll("12345", 2023, 10, "nomina_octubre_2023.pdf");
    }
}