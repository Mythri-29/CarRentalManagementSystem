package com.carrental.model;

public class Car {

    private String id;
    private String name;
    private String brand;
    private String model;
    private CarCategory category;
    private FuelType fuelType;
    private Transmission transmission;
    private int seats;
    private double pricePerDay;
    private boolean available;

    public Car() {
    }

    public Car(String id,
               String name,
               String brand,
               String model,
               CarCategory category,
               FuelType fuelType,
               Transmission transmission,
               int seats,
               double pricePerDay,
               boolean available) {

        this.id = id;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.category = category;
        this.fuelType = fuelType;
        this.transmission = transmission;
        this.seats = seats;
        this.pricePerDay = pricePerDay;
        this.available = available;
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

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public CarCategory getCategory() {
        return category;
    }

    public void setCategory(CarCategory category) {
        this.category = category;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public Transmission getTransmission() {
        return transmission;
    }

    public void setTransmission(Transmission transmission) {
        this.transmission = transmission;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Car{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", category=" + category +
                ", fuelType=" + fuelType +
                ", transmission=" + transmission +
                ", seats=" + seats +
                ", pricePerDay=" + pricePerDay +
                ", available=" + available +
                '}';
    }
}