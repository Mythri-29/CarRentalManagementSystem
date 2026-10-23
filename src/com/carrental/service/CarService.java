package com.carrental.service;

import com.carrental.exception.CarNotFoundException;
import com.carrental.exception.ValidationException;
import com.carrental.model.Car;
import com.carrental.model.CarCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CarService {

    private final FileStorageService storageService;
    private final List<Car> cars;

    public CarService(FileStorageService storageService) {
        this.storageService = storageService;
        this.cars = new ArrayList<>(storageService.loadCars());
    }

    // =========================
    // GET ALL CARS
    // =========================

    public synchronized List<Car> getAllCars() {
        return new ArrayList<>(cars);
    }

    // =========================
    // GET CAR BY ID
    // =========================

    public synchronized Car getCarById(String id) {

        return cars.stream()
                .filter(car -> car.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElseThrow(() ->
                        new CarNotFoundException(
                                "Car not found with ID: " + id
                        )
                );
    }

    // =========================
    // ADD CAR
    // =========================

    public synchronized Car addCar(Car car) {

        validateCar(car);

        if (cars.stream().anyMatch(
                existing -> existing.getId().equalsIgnoreCase(car.getId()))) {

            throw new ValidationException(
                    "A car with ID " + car.getId() + " already exists."
            );
        }

        cars.add(car);
        storageService.saveCars(cars);

        return car;
    }

    // =========================
    // UPDATE CAR
    // =========================

    public synchronized Car updateCar(String id, Car updatedCar) {

        validateCar(updatedCar);

        Car existingCar = getCarById(id);

        existingCar.setName(updatedCar.getName());
        existingCar.setBrand(updatedCar.getBrand());
        existingCar.setModel(updatedCar.getModel());
        existingCar.setCategory(updatedCar.getCategory());
        existingCar.setFuelType(updatedCar.getFuelType());
        existingCar.setTransmission(updatedCar.getTransmission());
        existingCar.setSeats(updatedCar.getSeats());
        existingCar.setPricePerDay(updatedCar.getPricePerDay());
        existingCar.setAvailable(updatedCar.isAvailable());

        storageService.saveCars(cars);

        return existingCar;
    }

    // =========================
    // DELETE CAR
    // =========================

    public synchronized void deleteCar(String id) {

        Car car = getCarById(id);

        cars.remove(car);

        storageService.saveCars(cars);
    }

    // =========================
    // SEARCH
    // =========================

    public synchronized List<Car> searchCars(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return getAllCars();
        }

        String search = keyword
                .trim()
                .toLowerCase(Locale.ROOT);

        return cars.stream()
                .filter(car ->
                        contains(car.getName(), search)
                                || contains(car.getBrand(), search)
                                || contains(car.getModel(), search)
                                || car.getCategory().name()
                                .toLowerCase(Locale.ROOT)
                                .contains(search)
                )
                .collect(Collectors.toList());
    }

    // =========================
    // FILTER BY CATEGORY
    // =========================

    public synchronized List<Car> filterByCategory(
            CarCategory category) {

        if (category == null) {
            return getAllCars();
        }

        return cars.stream()
                .filter(car -> car.getCategory() == category)
                .collect(Collectors.toList());
    }

    // =========================
    // AVAILABLE CARS
    // =========================

    public synchronized List<Car> getAvailableCars() {

        return cars.stream()
                .filter(Car::isAvailable)
                .collect(Collectors.toList());
    }

    // =========================
    // UPDATE AVAILABILITY
    // =========================

    public synchronized void updateAvailability(
            String id,
            boolean available) {

        Car car = getCarById(id);

        car.setAvailable(available);

        storageService.saveCars(cars);
    }

    // =========================
    // STATISTICS
    // =========================

    public synchronized long getTotalCars() {
        return cars.size();
    }

    public synchronized long getAvailableCarsCount() {

        return cars.stream()
                .filter(Car::isAvailable)
                .count();
    }

    public synchronized long getRentedCarsCount() {

        return cars.stream()
                .filter(car -> !car.isAvailable())
                .count();
    }

    // =========================
    // VALIDATION
    // =========================

    private void validateCar(Car car) {

        if (car == null) {
            throw new ValidationException("Car cannot be null.");
        }

        if (isBlank(car.getId())) {
            throw new ValidationException("Car ID cannot be empty.");
        }

        if (isBlank(car.getName())) {
            throw new ValidationException("Car name cannot be empty.");
        }

        if (isBlank(car.getBrand())) {
            throw new ValidationException("Car brand cannot be empty.");
        }

        if (isBlank(car.getModel())) {
            throw new ValidationException("Car model cannot be empty.");
        }

        if (car.getCategory() == null) {
            throw new ValidationException(
                    "Car category must be selected."
            );
        }

        if (car.getFuelType() == null) {
            throw new ValidationException(
                    "Fuel type must be selected."
            );
        }

        if (car.getTransmission() == null) {
            throw new ValidationException(
                    "Transmission must be selected."
            );
        }

        if (car.getSeats() <= 0) {
            throw new ValidationException(
                    "Number of seats must be greater than zero."
            );
        }

        if (car.getPricePerDay() <= 0) {
            throw new ValidationException(
                    "Price per day must be greater than zero."
            );
        }
    }

    private boolean contains(String value, String keyword) {

        return value != null
                && value.toLowerCase(Locale.ROOT)
                .contains(keyword);
    }

    private boolean isBlank(String value) {

        return value == null || value.isBlank();
    }
}