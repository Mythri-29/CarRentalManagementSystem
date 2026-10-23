package com.carrental.model;

import java.time.LocalDate;

public class Booking {

    private String id;
    private String customerId;
    private String carId;
    private LocalDate startDate;
    private LocalDate endDate;
    private long numberOfDays;
    private double totalAmount;
    private BookingStatus status;

    public Booking() {
    }

    public Booking(String id,
                   String customerId,
                   String carId,
                   LocalDate startDate,
                   LocalDate endDate,
                   long numberOfDays,
                   double totalAmount,
                   BookingStatus status) {

        this.id = id;
        this.customerId = customerId;
        this.carId = carId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numberOfDays = numberOfDays;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public long getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(long numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", carId='" + carId + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", numberOfDays=" + numberOfDays +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                '}';
    }
}
