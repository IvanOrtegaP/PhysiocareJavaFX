package com.example.physiocare.models.physio;

public class PhysioStats {
    private int totalAppointments;
    private int futureAppointments;
    private int pastAppointments;
    private int thisMonthAppointments;
    private double averageRating;

    public PhysioStats(int totalAppointments, int futureAppointments, int pastAppointments, int thisMonthAppointments, double averageRating) {
        this.totalAppointments = totalAppointments;
        this.futureAppointments = futureAppointments;
        this.pastAppointments = pastAppointments;
        this.thisMonthAppointments = thisMonthAppointments;
        this.averageRating = averageRating;
    }

    public int getTotalAppointments() {
        return totalAppointments;
    }

    public int getFutureAppointments() {
        return futureAppointments;
    }

    public int getPastAppointments() {
        return pastAppointments;
    }

    public int getThisMonthAppointments() {
        return thisMonthAppointments;
    }

    public double getAverageRating() {
        return averageRating;
    }
}