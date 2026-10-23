package com.carrental.service;

import com.carrental.exception.BookingException;
import com.carrental.exception.CarNotFoundException;
import com.carrental.exception.ValidationException;
import com.carrental.model.Booking;
import com.carrental.model.BookingStatus;
import com.carrental.model.Car;
import com.carrental.model.Customer;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class BookingService {

    private final FileStorageService storageService;
    private final CarService carService;

    private final List<Booking> bookings;
    private final List<Customer> customers;

    public BookingService(
            FileStorageService storageService,
            CarService carService) {

        this.storageService = storageService;
        this.carService = carService;

        this.bookings =
                new ArrayList<>(storageService.loadBookings());

        this.customers =
                new ArrayList<>(storageService.loadCustomers());
    }

    // =========================
    // GET ALL BOOKINGS
    // =========================

    public synchronized List<Booking> getAllBookings() {
        return new ArrayList<>(bookings);
    }

    // =========================
    // GET BOOKING BY ID
    // =========================

    public synchronized Booking getBookingById(String id) {

        return bookings.stream()
                .filter(booking ->
                        booking.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElseThrow(() ->
                        new BookingException(
                                "Booking not found with ID: " + id
                        )
                );
    }

    // =========================
    // CREATE BOOKING
    // =========================

    public synchronized Booking createBooking(
            String bookingId,
            Customer customer,
            String carId,
            LocalDate startDate,
            LocalDate endDate) {

        validateBookingInput(
                bookingId,
                customer,
                carId,
                startDate,
                endDate
        );

        Car car;

        try {
            car = carService.getCarById(carId);
        } catch (CarNotFoundException e) {
            throw e;
        }

        if (!car.isAvailable()) {
            throw new BookingException(
                    "This car is currently unavailable."
            );
        }

        if (hasOverlappingBooking(
                carId,
                startDate,
                endDate)) {

            throw new BookingException(
                    "This car is already booked for the selected dates."
            );
        }

        Customer savedCustomer = saveOrUpdateCustomer(customer);

        long numberOfDays =
                ChronoUnit.DAYS.between(
                        startDate,
                        endDate
                );

        double totalAmount =
                numberOfDays * car.getPricePerDay();

        Booking booking = new Booking(
                bookingId,
                savedCustomer.getId(),
                carId,
                startDate,
                endDate,
                numberOfDays,
                totalAmount,
                BookingStatus.CONFIRMED
        );

        bookings.add(booking);

        storageService.saveBookings(bookings);

        return booking;
    }

    // =========================
    // CANCEL BOOKING
    // =========================

    public synchronized Booking cancelBooking(String id) {

        Booking booking = getBookingById(id);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingException(
                    "Booking is already cancelled."
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);

        storageService.saveBookings(bookings);

        updateCarAvailabilityAfterCancellation(
                booking.getCarId()
        );

        return booking;
    }

    // =========================
    // SEARCH BOOKINGS
    // =========================

    public synchronized List<Booking> searchBookings(
            String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return getAllBookings();
        }

        String search =
                keyword.trim().toLowerCase(Locale.ROOT);

        return bookings.stream()
                .filter(booking ->
                        booking.getId()
                                .toLowerCase(Locale.ROOT)
                                .contains(search)
                                || booking.getCustomerId()
                                .toLowerCase(Locale.ROOT)
                                .contains(search)
                                || booking.getCarId()
                                .toLowerCase(Locale.ROOT)
                                .contains(search)
                )
                .collect(Collectors.toList());
    }

    // =========================
    // CUSTOMER BOOKINGS
    // =========================

    public synchronized List<Booking> getBookingsForCustomer(
            String customerId) {

        return bookings.stream()
                .filter(booking ->
                        booking.getCustomerId()
                                .equalsIgnoreCase(customerId))
                .collect(Collectors.toList());
    }

    // =========================
    // ACTIVE BOOKINGS
    // =========================

    public synchronized long getActiveBookingsCount() {

        return bookings.stream()
                .filter(booking ->
                        booking.getStatus()
                                == BookingStatus.CONFIRMED)
                .count();
    }

    // =========================
    // TOTAL BOOKINGS
    // =========================

    public synchronized long getTotalBookingsCount() {
        return bookings.size();
    }

    // =========================
    // CUSTOMER LOOKUP
    // =========================

    public synchronized Customer getCustomerById(
            String customerId) {

        return customers.stream()
                .filter(customer ->
                        customer.getId()
                                .equalsIgnoreCase(customerId))
                .findFirst()
                .orElse(null);
    }

    // =========================
    // BOOKING OVERLAP CHECK
    // =========================

    public synchronized boolean hasOverlappingBooking(
            String carId,
            LocalDate requestedStart,
            LocalDate requestedEnd) {

        return bookings.stream()
                .filter(booking ->
                        booking.getCarId()
                                .equalsIgnoreCase(carId))
                .filter(booking ->
                        booking.getStatus()
                                == BookingStatus.CONFIRMED)
                .anyMatch(booking ->
                        datesOverlap(
                                booking.getStartDate(),
                                booking.getEndDate(),
                                requestedStart,
                                requestedEnd
                        )
                );
    }

    private boolean datesOverlap(
            LocalDate existingStart,
            LocalDate existingEnd,
            LocalDate requestedStart,
            LocalDate requestedEnd) {

        return !requestedEnd.isBefore(existingStart)
                && !requestedStart.isAfter(existingEnd);
    }

    // =========================
    // SAVE CUSTOMER
    // =========================

    private Customer saveOrUpdateCustomer(
            Customer customer) {

        Customer existingCustomer =
                getCustomerById(customer.getId());

        if (existingCustomer != null) {

            existingCustomer.setName(customer.getName());
            existingCustomer.setEmail(customer.getEmail());
            existingCustomer.setPhone(
                    customer.getPhone()
            );
            existingCustomer.setDrivingLicenceNumber(
                    customer.getDrivingLicenceNumber()
            );

            storageService.saveCustomers(customers);

            return existingCustomer;
        }

        customers.add(customer);

        storageService.saveCustomers(customers);

        return customer;
    }

    // =========================
    // UPDATE CAR AVAILABILITY
    // =========================

    private void updateCarAvailabilityAfterCancellation(
            String carId) {

        boolean hasActiveBooking =
                bookings.stream()
                        .filter(booking ->
                                booking.getCarId()
                                        .equalsIgnoreCase(carId))
                        .anyMatch(booking ->
                                booking.getStatus()
                                        == BookingStatus.CONFIRMED);

        if (!hasActiveBooking) {
            carService.updateAvailability(
                    carId,
                    true
            );
        }
    }

    // =========================
    // VALIDATION
    // =========================

    private void validateBookingInput(
            String bookingId,
            Customer customer,
            String carId,
            LocalDate startDate,
            LocalDate endDate) {

        if (isBlank(bookingId)) {
            throw new ValidationException(
                    "Booking ID cannot be empty."
            );
        }

        if (customer == null) {
            throw new ValidationException(
                    "Customer information is required."
            );
        }

        if (isBlank(customer.getId())) {
            throw new ValidationException(
                    "Customer ID cannot be empty."
            );
        }

        if (isBlank(customer.getName())) {
            throw new ValidationException(
                    "Customer name cannot be empty."
            );
        }

        if (isBlank(customer.getEmail())
                || !isValidEmail(customer.getEmail())) {

            throw new ValidationException(
                    "Please provide a valid email address."
            );
        }

        if (isBlank(customer.getPhone())
                || !isValidPhone(customer.getPhone())) {

            throw new ValidationException(
                    "Please provide a valid phone number."
            );
        }

        if (isBlank(customer.getDrivingLicenceNumber())) {
            throw new ValidationException(
                    "Driving licence number cannot be empty."
            );
        }

        if (isBlank(carId)) {
            throw new ValidationException(
                    "Car ID cannot be empty."
            );
        }

        if (startDate == null) {
            throw new ValidationException(
                    "Start date is required."
            );
        }

        if (endDate == null) {
            throw new ValidationException(
                    "End date is required."
            );
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new ValidationException(
                    "Rental start date cannot be in the past."
            );
        }

        if (endDate.isBefore(startDate)) {
            throw new ValidationException(
                    "End date cannot be before start date."
            );
        }

        long days =
                ChronoUnit.DAYS.between(
                        startDate,
                        endDate
                );

        if (days < 1) {
            throw new ValidationException(
                    "Rental duration must be at least 1 day."
            );
        }
    }

    private boolean isValidEmail(String email) {

        return email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        );
    }

    private boolean isValidPhone(String phone) {

        return phone.matches("\\d{10}");
    }

    private boolean isBlank(String value) {

        return value == null || value.isBlank();
    }
}
