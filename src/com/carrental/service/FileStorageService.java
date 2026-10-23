package com.carrental.service;

import com.carrental.model.Booking;
import com.carrental.model.BookingStatus;
import com.carrental.model.Car;
import com.carrental.model.CarCategory;
import com.carrental.model.Customer;
import com.carrental.model.FuelType;
import com.carrental.model.Transmission;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FileStorageService {

    private final Path dataDirectory;
    private final Path carsFile;
    private final Path customersFile;
    private final Path bookingsFile;

    public FileStorageService() {
        this.dataDirectory = Path.of("data");
        this.carsFile = dataDirectory.resolve("cars.csv");
        this.customersFile = dataDirectory.resolve("customers.csv");
        this.bookingsFile = dataDirectory.resolve("bookings.csv");

        initializeStorage();
    }

    private void initializeStorage() {
        try {
            Files.createDirectories(dataDirectory);

            if (!Files.exists(carsFile)) {
                Files.createFile(carsFile);
            }

            if (!Files.exists(customersFile)) {
                Files.createFile(customersFile);
            }

            if (!Files.exists(bookingsFile)) {
                Files.createFile(bookingsFile);
            }

        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize data storage.", e);
        }
    }

    // =========================
    // CAR STORAGE
    // =========================

    public List<Car> loadCars() {
        List<Car> cars = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(carsFile);

            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }

                try {
                    Car car = parseCar(line);

                    if (car != null) {
                        cars.add(car);
                    }

                } catch (Exception e) {
                    System.err.println("Skipping invalid car record: " + line);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Unable to read cars data.", e);
        }

        return cars;
    }

    public void saveCars(List<Car> cars) {
        List<String> lines = new ArrayList<>();

        for (Car car : cars) {
            lines.add(carToCsv(car));
        }

        writeFile(carsFile, lines);
    }

    private Car parseCar(String line) {

        String[] parts = line.split(",", -1);

        if (parts.length != 10) {
            return null;
        }

        return new Car(
                parts[0],
                parts[1],
                parts[2],
                parts[3],
                CarCategory.valueOf(parts[4]),
                FuelType.valueOf(parts[5]),
                Transmission.valueOf(parts[6]),
                Integer.parseInt(parts[7]),
                Double.parseDouble(parts[8]),
                Boolean.parseBoolean(parts[9])
        );
    }

    private String carToCsv(Car car) {

        return String.join(",",
                clean(car.getId()),
                clean(car.getName()),
                clean(car.getBrand()),
                clean(car.getModel()),
                car.getCategory().name(),
                car.getFuelType().name(),
                car.getTransmission().name(),
                String.valueOf(car.getSeats()),
                String.valueOf(car.getPricePerDay()),
                String.valueOf(car.isAvailable())
        );
    }

    // =========================
    // CUSTOMER STORAGE
    // =========================

    public List<Customer> loadCustomers() {
        List<Customer> customers = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(customersFile);

            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }

                try {
                    Customer customer = parseCustomer(line);

                    if (customer != null) {
                        customers.add(customer);
                    }

                } catch (Exception e) {
                    System.err.println("Skipping invalid customer record: " + line);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Unable to read customers data.", e);
        }

        return customers;
    }

    public void saveCustomers(List<Customer> customers) {
        List<String> lines = new ArrayList<>();

        for (Customer customer : customers) {
            lines.add(customerToCsv(customer));
        }

        writeFile(customersFile, lines);
    }

    private Customer parseCustomer(String line) {

        String[] parts = line.split(",", -1);

        if (parts.length != 5) {
            return null;
        }

        return new Customer(
                parts[0],
                parts[1],
                parts[2],
                parts[3],
                parts[4]
        );
    }

    private String customerToCsv(Customer customer) {

        return String.join(",",
                clean(customer.getId()),
                clean(customer.getName()),
                clean(customer.getEmail()),
                clean(customer.getPhone()),
                clean(customer.getDrivingLicenceNumber())
        );
    }

    // =========================
    // BOOKING STORAGE
    // =========================

    public List<Booking> loadBookings() {
        List<Booking> bookings = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(bookingsFile);

            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }

                try {
                    Booking booking = parseBooking(line);

                    if (booking != null) {
                        bookings.add(booking);
                    }

                } catch (Exception e) {
                    System.err.println("Skipping invalid booking record: " + line);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Unable to read bookings data.", e);
        }

        return bookings;
    }

    public void saveBookings(List<Booking> bookings) {
        List<String> lines = new ArrayList<>();

        for (Booking booking : bookings) {
            lines.add(bookingToCsv(booking));
        }

        writeFile(bookingsFile, lines);
    }

    private Booking parseBooking(String line) {

        String[] parts = line.split(",", -1);

        if (parts.length != 8) {
            return null;
        }

        return new Booking(
                parts[0],
                parts[1],
                parts[2],
                LocalDate.parse(parts[3]),
                LocalDate.parse(parts[4]),
                Long.parseLong(parts[5]),
                Double.parseDouble(parts[6]),
                BookingStatus.valueOf(parts[7])
        );
    }

    private String bookingToCsv(Booking booking) {

        return String.join(",",
                clean(booking.getId()),
                clean(booking.getCustomerId()),
                clean(booking.getCarId()),
                booking.getStartDate().toString(),
                booking.getEndDate().toString(),
                String.valueOf(booking.getNumberOfDays()),
                String.valueOf(booking.getTotalAmount()),
                booking.getStatus().name()
        );
    }

    // =========================
    // GENERAL FILE METHOD
    // =========================

    private void writeFile(Path file, List<String> lines) {

        try {
            Files.write(
                    file,
                    lines,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

        } catch (IOException e) {
            throw new RuntimeException(
                    "Unable to write data to " + file,
                    e
            );
        }
    }

    private String clean(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace(",", " ")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}