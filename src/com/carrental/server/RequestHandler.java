package com.carrental.server;

import com.carrental.exception.BookingException;
import com.carrental.exception.CarNotFoundException;
import com.carrental.exception.ValidationException;
import com.carrental.model.Booking;
import com.carrental.model.BookingStatus;
import com.carrental.model.Car;
import com.carrental.model.CarCategory;
import com.carrental.model.Customer;
import com.carrental.model.FuelType;
import com.carrental.model.Transmission;
import com.carrental.service.BookingService;
import com.carrental.service.CarService;
import com.carrental.util.IdGenerator;
import com.carrental.util.ValidationUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class RequestHandler implements HttpHandler {

    private final CarService carService;
    private final BookingService bookingService;

    public RequestHandler(
            CarService carService,
            BookingService bookingService) {

        this.carService = carService;
        this.bookingService = bookingService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        try {

            String method =
                    exchange.getRequestMethod()
                            .toUpperCase(Locale.ROOT);

            String path =
                    exchange.getRequestURI()
                            .getPath();

            /*
             * Browser preflight request.
             */
            if ("OPTIONS".equals(method)) {
                handleOptions(exchange);
                return;
            }

            /*
             * API requests.
             */
            if (path.startsWith("/api")) {
                handleApi(exchange, method, path);
                return;
            }

            /*
             * Everything else is a frontend file.
             */
            handleStaticFile(exchange, path);

        } catch (CarNotFoundException e) {

            ResponseUtil.sendError(
                    exchange,
                    404,
                    e.getMessage()
            );

        } catch (ValidationException e) {

            ResponseUtil.sendError(
                    exchange,
                    400,
                    e.getMessage()
            );

        } catch (BookingException e) {

            ResponseUtil.sendError(
                    exchange,
                    409,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            ResponseUtil.sendError(
                    exchange,
                    400,
                    e.getMessage()
            );

        } catch (Exception e) {

            e.printStackTrace();

            ResponseUtil.sendError(
                    exchange,
                    500,
                    "Internal server error."
            );
        }
    }

    // =========================================================
    // OPTIONS
    // =========================================================

    private void handleOptions(
            HttpExchange exchange) throws IOException {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Content-Type"
        );

        exchange.sendResponseHeaders(
                204,
                -1
        );

        exchange.close();
    }

    // =========================================================
    // API ROUTER
    // =========================================================

    private void handleApi(
            HttpExchange exchange,
            String method,
            String path) throws Exception {

        /*
         * GET /api/cars
         * GET /api/cars/{id}
         */
        if (path.equals("/api/cars")
                || path.startsWith("/api/cars/")) {

            handleCars(exchange, method, path);
            return;
        }

        /*
         * GET /api/bookings
         * POST /api/bookings
         * DELETE /api/bookings/{id}
         */
        if (path.equals("/api/bookings")
                || path.startsWith("/api/bookings/")) {

            handleBookings(exchange, method, path);
            return;
        }

        /*
         * GET /api/stats
         */
        if (path.equals("/api/stats")
                && method.equals("GET")) {

            handleStatistics(exchange);
            return;
        }

        ResponseUtil.sendError(
                exchange,
                404,
                "API endpoint not found."
        );
    }

    // =========================================================
    // CAR ENDPOINTS
    // =========================================================

    private void handleCars(
            HttpExchange exchange,
            String method,
            String path) throws Exception {

        String carId = extractId(
                path,
                "/api/cars"
        );

        /*
         * GET /api/cars
         */
        if (method.equals("GET") && carId == null) {

            handleGetCars(exchange);
            return;
        }

        /*
         * GET /api/cars/{id}
         */
        if (method.equals("GET") && carId != null) {

            handleGetCar(exchange, carId);
            return;
        }

        /*
         * POST /api/cars
         */
        if (method.equals("POST") && carId == null) {

            handleAddCar(exchange);
            return;
        }

        /*
         * PUT /api/cars/{id}
         */
        if (method.equals("PUT") && carId != null) {

            handleUpdateCar(exchange, carId);
            return;
        }

        /*
         * DELETE /api/cars/{id}
         */
        if (method.equals("DELETE") && carId != null) {

            handleDeleteCar(exchange, carId);
            return;
        }

        ResponseUtil.sendError(
                exchange,
                405,
                "Method not allowed."
        );
    }

    private void handleGetCars(
            HttpExchange exchange) throws IOException {

        Map<String, String> query =
                parseQuery(
                        exchange.getRequestURI()
                                .getRawQuery()
                );

        String search = query.get("search");
        String category = query.get("category");

        List<Car> cars;

        if (search != null && !search.isBlank()) {

            cars = carService.searchCars(search);

        } else if (category != null && !category.isBlank()) {

            try {

                CarCategory carCategory =
                        CarCategory.valueOf(
                                category.toUpperCase()
                        );

                cars =
                        carService.filterByCategory(
                                carCategory
                        );

            } catch (IllegalArgumentException e) {

                ResponseUtil.sendError(
                        exchange,
                        400,
                        "Invalid car category."
                );

                return;
            }

        } else {

            cars = carService.getAllCars();
        }

        StringBuilder json =
                new StringBuilder();

        json.append("{");
        json.append("\"success\":true,");
        json.append("\"count\":")
                .append(cars.size())
                .append(",");
        json.append("\"cars\":[");

        for (int i = 0; i < cars.size(); i++) {

            json.append(carToJson(cars.get(i)));

            if (i < cars.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        json.append("}");

        ResponseUtil.sendJson(
                exchange,
                200,
                json.toString()
        );
    }

    private void handleGetCar(
            HttpExchange exchange,
            String carId) throws IOException {

        Car car =
                carService.getCarById(carId);

        String json =
                "{"
                        + "\"success\":true,"
                        + "\"car\":"
                        + carToJson(car)
                        + "}";

        ResponseUtil.sendJson(
                exchange,
                200,
                json
        );
    }

    private void handleAddCar(
            HttpExchange exchange) throws Exception {

        String body =
                readRequestBody(exchange);

        Map<String, String> data =
                parseJsonObject(body);

        Car car =
                carFromRequest(data, false);

        /*
         * Generate an ID if the frontend didn't provide one.
         */
        if (ValidationUtil.isBlank(car.getId())) {

            car.setId(
                    IdGenerator.generateCarId()
            );
        }

        carService.addCar(car);

        String json =
                "{"
                        + "\"success\":true,"
                        + "\"message\":\"Car added successfully.\","
                        + "\"car\":"
                        + carToJson(car)
                        + "}";

        ResponseUtil.sendJson(
                exchange,
                201,
                json
        );
    }

    private void handleUpdateCar(
            HttpExchange exchange,
            String carId) throws Exception {

        String body =
                readRequestBody(exchange);

        Map<String, String> data =
                parseJsonObject(body);

        Car updatedCar =
                carFromRequest(data, true);

        Car result =
                carService.updateCar(
                        carId,
                        updatedCar
                );

        String json =
                "{"
                        + "\"success\":true,"
                        + "\"message\":\"Car updated successfully.\","
                        + "\"car\":"
                        + carToJson(result)
                        + "}";

        ResponseUtil.sendJson(
                exchange,
                200,
                json
        );
    }

    private void handleDeleteCar(
            HttpExchange exchange,
            String carId) throws IOException {

        carService.deleteCar(carId);

        ResponseUtil.sendSuccess(
                exchange,
                "Car deleted successfully."
        );
    }

    // =========================================================
    // BOOKING ENDPOINTS
    // =========================================================

    private void handleBookings(
            HttpExchange exchange,
            String method,
            String path) throws Exception {

        String bookingId =
                extractId(
                        path,
                        "/api/bookings"
                );

        /*
         * GET /api/bookings
         */
        if (method.equals("GET")
                && bookingId == null) {

            handleGetBookings(exchange);
            return;
        }

        /*
         * POST /api/bookings
         */
        if (method.equals("POST")
                && bookingId == null) {

            handleCreateBooking(exchange);
            return;
        }

        /*
         * DELETE /api/bookings/{id}
         */
        if (method.equals("DELETE")
                && bookingId != null) {

            handleCancelBooking(
                    exchange,
                    bookingId
            );

            return;
        }

        ResponseUtil.sendError(
                exchange,
                405,
                "Method not allowed."
        );
    }

    private void handleGetBookings(
            HttpExchange exchange) throws IOException {

        Map<String, String> query =
                parseQuery(
                        exchange.getRequestURI()
                                .getRawQuery()
                );

        String search =
                query.get("search");

        List<Booking> bookings;

        if (search != null
                && !search.isBlank()) {

            bookings =
                    bookingService.searchBookings(
                            search
                    );

        } else {

            bookings =
                    bookingService.getAllBookings();
        }

        StringBuilder json =
                new StringBuilder();

        json.append("{");
        json.append("\"success\":true,");
        json.append("\"count\":")
                .append(bookings.size())
                .append(",");
        json.append("\"bookings\":[");

        for (int i = 0;
             i < bookings.size();
             i++) {

            json.append(
                    bookingToJson(
                            bookings.get(i)
                    )
            );

            if (i < bookings.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        json.append("}");

        ResponseUtil.sendJson(
                exchange,
                200,
                json.toString()
        );
    }

    private void handleCreateBooking(
            HttpExchange exchange) throws Exception {

        String body =
                readRequestBody(exchange);

        Map<String, String> data =
                parseJsonObject(body);

        String customerName =
                data.get("customerName");

        String email =
                data.get("email");

        String phone =
                data.get("phone");

        String licence =
                data.get("drivingLicenceNumber");

        String carId =
                data.get("carId");

        String startDateText =
                data.get("startDate");

        String endDateText =
                data.get("endDate");

        String customerId =
                data.get("customerId");

        if (ValidationUtil.isBlank(customerId)) {

            customerId =
                    IdGenerator.generateCustomerId();
        }

        String bookingId =
                data.get("bookingId");

        if (ValidationUtil.isBlank(bookingId)) {

            bookingId =
                    IdGenerator.generateBookingId();
        }

        Customer customer =
                new Customer(
                        customerId,
                        customerName,
                        email,
                        phone,
                        licence
                );

        LocalDate startDate =
                LocalDate.parse(startDateText);

        LocalDate endDate =
                LocalDate.parse(endDateText);

        Booking booking =
                bookingService.createBooking(
                        bookingId,
                        customer,
                        carId,
                        startDate,
                        endDate
                );

        String json =
                "{"
                        + "\"success\":true,"
                        + "\"message\":\"Booking created successfully.\","
                        + "\"booking\":"
                        + bookingToJson(booking)
                        + "}";

        ResponseUtil.sendJson(
                exchange,
                201,
                json
        );
    }

    private void handleCancelBooking(
            HttpExchange exchange,
            String bookingId) throws IOException {

        Booking booking =
                bookingService.cancelBooking(
                        bookingId
                );

        String json =
                "{"
                        + "\"success\":true,"
                        + "\"message\":\"Booking cancelled successfully.\","
                        + "\"booking\":"
                        + bookingToJson(booking)
                        + "}";

        ResponseUtil.sendJson(
                exchange,
                200,
                json
        );
    }

    // =========================================================
    // STATISTICS
    // =========================================================

    private void handleStatistics(
            HttpExchange exchange) throws IOException {

        long totalCars =
                carService.getTotalCars();

        long availableCars =
                carService.getAvailableCarsCount();

        long rentedCars =
                carService.getRentedCarsCount();

        long totalBookings =
                bookingService.getTotalBookingsCount();

        long activeBookings =
                bookingService.getActiveBookingsCount();

        String json =
                "{"
                        + "\"success\":true,"
                        + "\"statistics\":{"
                        + "\"totalCars\":"
                        + totalCars
                        + ","
                        + "\"availableCars\":"
                        + availableCars
                        + ","
                        + "\"rentedCars\":"
                        + rentedCars
                        + ","
                        + "\"totalBookings\":"
                        + totalBookings
                        + ","
                        + "\"activeBookings\":"
                        + activeBookings
                        + "}"
                        + "}";

        ResponseUtil.sendJson(
                exchange,
                200,
                json
        );
    }

    // =========================================================
    // CAR JSON
    // =========================================================

    private String carToJson(Car car) {

        return "{"
                + "\"id\":\""
                + ResponseUtil.escapeJson(car.getId())
                + "\","
                + "\"name\":\""
                + ResponseUtil.escapeJson(car.getName())
                + "\","
                + "\"brand\":\""
                + ResponseUtil.escapeJson(car.getBrand())
                + "\","
                + "\"model\":\""
                + ResponseUtil.escapeJson(car.getModel())
                + "\","
                + "\"category\":\""
                + car.getCategory().name()
                + "\","
                + "\"fuelType\":\""
                + car.getFuelType().name()
                + "\","
                + "\"transmission\":\""
                + car.getTransmission().name()
                + "\","
                + "\"seats\":"
                + car.getSeats()
                + ","
                + "\"pricePerDay\":"
                + car.getPricePerDay()
                + ","
                + "\"available\":"
                + car.isAvailable()
                + "}";
    }

    // =========================================================
    // BOOKING JSON
    // =========================================================

    private String bookingToJson(
            Booking booking) {

        Customer customer =
                bookingService.getCustomerById(
                        booking.getCustomerId()
                );

        String customerName =
                customer == null
                        ? ""
                        : customer.getName();

        Car car;

        try {

            car =
                    carService.getCarById(
                            booking.getCarId()
                    );

        } catch (CarNotFoundException e) {

            car = null;
        }

        String carName =
                car == null
                        ? ""
                        : car.getName();

        return "{"
                + "\"id\":\""
                + ResponseUtil.escapeJson(
                booking.getId()
        )
                + "\","
                + "\"customerId\":\""
                + ResponseUtil.escapeJson(
                booking.getCustomerId()
        )
                + "\","
                + "\"customerName\":\""
                + ResponseUtil.escapeJson(
                customerName
        )
                + "\","
                + "\"carId\":\""
                + ResponseUtil.escapeJson(
                booking.getCarId()
        )
                + "\","
                + "\"carName\":\""
                + ResponseUtil.escapeJson(
                carName
        )
                + "\","
                + "\"startDate\":\""
                + booking.getStartDate()
                + "\","
                + "\"endDate\":\""
                + booking.getEndDate()
                + "\","
                + "\"numberOfDays\":"
                + booking.getNumberOfDays()
                + ","
                + "\"totalAmount\":"
                + booking.getTotalAmount()
                + ","
                + "\"status\":\""
                + booking.getStatus().name()
                + "\""
                + "}";
    }

    // =========================================================
    // CREATE CAR FROM REQUEST
    // =========================================================

    private Car carFromRequest(
            Map<String, String> data,
            boolean update) {

        String id =
                data.get("id");

        String name =
                data.get("name");

        String brand =
                data.get("brand");

        String model =
                data.get("model");

        String categoryText =
                data.get("category");

        String fuelText =
                data.get("fuelType");

        String transmissionText =
                data.get("transmission");

        String seatsText =
                data.get("seats");

        String priceText =
                data.get("pricePerDay");

        String availableText =
                data.get("available");

        if (update
                && ValidationUtil.isBlank(id)) {

            id = "";
        }

        CarCategory category =
                parseCategory(categoryText);

        FuelType fuelType =
                parseFuelType(fuelText);

        Transmission transmission =
                parseTransmission(
                        transmissionText
                );

        int seats =
                parseInteger(
                        seatsText,
                        "seats"
                );

        double price =
                parseDouble(
                        priceText,
                        "pricePerDay"
                );

        boolean available =
                !("false".equalsIgnoreCase(
                        availableText
                ));

        return new Car(
                id,
                name,
                brand,
                model,
                category,
                fuelType,
                transmission,
                seats,
                price,
                available
        );
    }

    private CarCategory parseCategory(
            String value) {

        if (ValidationUtil.isBlank(value)) {
            throw new ValidationException(
                    "Car category is required."
            );
        }

        try {

            return CarCategory.valueOf(
                    value.trim().toUpperCase()
            );

        } catch (IllegalArgumentException e) {

            throw new ValidationException(
                    "Invalid car category."
            );
        }
    }

    private FuelType parseFuelType(
            String value) {

        if (ValidationUtil.isBlank(value)) {
            throw new ValidationException(
                    "Fuel type is required."
            );
        }

        try {

            return FuelType.valueOf(
                    value.trim().toUpperCase()
            );

        } catch (IllegalArgumentException e) {

            throw new ValidationException(
                    "Invalid fuel type."
            );
        }
    }

    private Transmission parseTransmission(
            String value) {

        if (ValidationUtil.isBlank(value)) {
            throw new ValidationException(
                    "Transmission is required."
            );
        }

        try {

            return Transmission.valueOf(
                    value.trim().toUpperCase()
            );

        } catch (IllegalArgumentException e) {

            throw new ValidationException(
                    "Invalid transmission."
            );
        }
    }

    private int parseInteger(
            String value,
            String fieldName) {

        if (ValidationUtil.isBlank(value)) {

            throw new ValidationException(
                    fieldName + " is required."
            );
        }

        try {

            return Integer.parseInt(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            throw new ValidationException(
                    fieldName
                            + " must be a valid number."
            );
        }
    }

    private double parseDouble(
            String value,
            String fieldName) {

        if (ValidationUtil.isBlank(value)) {

            throw new ValidationException(
                    fieldName + " is required."
            );
        }

        try {

            return Double.parseDouble(
                    value.trim()
            );

        } catch (NumberFormatException e) {

            throw new ValidationException(
                    fieldName
                            + " must be a valid number."
            );
        }
    }

    // =========================================================
    // REQUEST BODY
    // =========================================================

    private String readRequestBody(
            HttpExchange exchange)
            throws IOException {

        InputStream inputStream =
                exchange.getRequestBody();

        byte[] bytes =
                inputStream.readAllBytes();

        return new String(
                bytes,
                StandardCharsets.UTF_8
        );
    }

    // =========================================================
    // SIMPLE JSON PARSER
    // =========================================================

    private Map<String, String> parseJsonObject(
            String json) {

        Map<String, String> result =
                new HashMap<>();

        if (json == null || json.isBlank()) {
            return result;
        }

        String content =
                json.trim();

        if (content.startsWith("{")) {
            content = content.substring(1);
        }

        if (content.endsWith("}")) {
            content =
                    content.substring(
                            0,
                            content.length() - 1
                    );
        }

        List<String> pairs =
                splitJsonPairs(content);

        for (String pair : pairs) {

            int colonIndex =
                    findColon(pair);

            if (colonIndex < 0) {
                continue;
            }

            String key =
                    pair.substring(
                            0,
                            colonIndex
                    ).trim();

            String value =
                    pair.substring(
                            colonIndex + 1
                    ).trim();

            key =
                    removeQuotes(key);

            value =
                    removeQuotes(value);

            result.put(
                    key,
                    unescapeJson(value)
            );
        }

        return result;
    }

    private List<String> splitJsonPairs(
            String content) {

        List<String> pairs =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean insideQuotes = false;
        boolean escaped = false;

        for (int i = 0;
             i < content.length();
             i++) {

            char c =
                    content.charAt(i);

            if (c == '\\'
                    && !escaped) {

                escaped = true;
                current.append(c);
                continue;
            }

            if (c == '"'
                    && !escaped) {

                insideQuotes =
                        !insideQuotes;
            }

            if (c == ','
                    && !insideQuotes) {

                pairs.add(
                        current.toString()
                );

                current.setLength(0);

            } else {

                current.append(c);
            }

            escaped = false;
        }

        if (!current.isEmpty()) {

            pairs.add(
                    current.toString()
            );
        }

        return pairs;
    }

    private int findColon(String text) {

        boolean insideQuotes = false;
        boolean escaped = false;

        for (int i = 0;
             i < text.length();
             i++) {

            char c =
                    text.charAt(i);

            if (c == '\\'
                    && !escaped) {

                escaped = true;
                continue;
            }

            if (c == '"'
                    && !escaped) {

                insideQuotes =
                        !insideQuotes;
            }

            if (c == ':'
                    && !insideQuotes) {

                return i;
            }

            escaped = false;
        }

        return -1;
    }

    private String removeQuotes(
            String value) {

        value = value.trim();

        if (value.length() >= 2
                && value.startsWith("\"")
                && value.endsWith("\"")) {

            return value.substring(
                    1,
                    value.length() - 1
            );
        }

        return value;
    }

    private String unescapeJson(
            String value) {

        return value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    // =========================================================
    // URL QUERY PARAMETERS
    // =========================================================

    private Map<String, String> parseQuery(
            String query) {

        Map<String, String> result =
                new HashMap<>();

        if (query == null
                || query.isBlank()) {

            return result;
        }

        String[] pairs =
                query.split("&");

        for (String pair : pairs) {

            String[] parts =
                    pair.split("=", 2);

            String key =
                    URLDecoder.decode(
                            parts[0],
                            StandardCharsets.UTF_8
                    );

            String value =
                    parts.length > 1
                            ? URLDecoder.decode(
                            parts[1],
                            StandardCharsets.UTF_8
                    )
                            : "";

            result.put(key, value);
        }

        return result;
    }

    // =========================================================
    // PATH ID
    // =========================================================

    private String extractId(
            String path,
            String basePath) {

        if (!path.startsWith(basePath)) {
            return null;
        }

        String remaining =
                path.substring(
                        basePath.length()
                );

        if (remaining.isBlank()
                || remaining.equals("/")) {

            return null;
        }

        if (remaining.startsWith("/")) {

            remaining =
                    remaining.substring(1);
        }

        if (remaining.contains("/")) {

            remaining =
                    remaining.substring(
                            0,
                            remaining.indexOf("/")
                    );
        }

        return URLDecoder.decode(
                remaining,
                StandardCharsets.UTF_8
        );
    }

    // =========================================================
    // STATIC FRONTEND
    // =========================================================

    private void handleStaticFile(
            HttpExchange exchange,
            String path) throws IOException {

        if (path.equals("/")) {
            path = "/index.html";
        }

        /*
         * Prevent attempts to access files outside
         * the frontend directory.
         */
        String relativePath =
                path.substring(
                        path.startsWith("/")
                                ? 1
                                : 0
                );

        if (relativePath.contains("..")) {

            ResponseUtil.sendError(
                    exchange,
                    403,
                    "Access denied."
            );

            return;
        }

        Path frontendDirectory =
                Path.of("frontend")
                        .toAbsolutePath()
                        .normalize();

        Path requestedFile =
                frontendDirectory
                        .resolve(relativePath)
                        .normalize();

        if (!requestedFile.startsWith(
                frontendDirectory)) {

            ResponseUtil.sendError(
                    exchange,
                    403,
                    "Access denied."
            );

            return;
        }

        if (!Files.exists(requestedFile)
                || !Files.isRegularFile(
                requestedFile)) {

            ResponseUtil.sendError(
                    exchange,
                    404,
                    "Frontend file not found."
            );

            return;
        }

        byte[] content =
                Files.readAllBytes(
                        requestedFile
                );

        String contentType =
                getContentType(
                        requestedFile.toString()
                );

        exchange.getResponseHeaders().set(
                "Content-Type",
                contentType
        );

        exchange.sendResponseHeaders(
                200,
                content.length
        );

        try (var output =
                     exchange.getResponseBody()) {

            output.write(content);
        }
    }

    private String getContentType(
            String fileName) {

        String lower =
                fileName.toLowerCase(
                        Locale.ROOT
                );

        if (lower.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }

        if (lower.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }

        if (lower.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }

        if (lower.endsWith(".json")) {
            return "application/json; charset=UTF-8";
        }

        if (lower.endsWith(".png")) {
            return "image/png";
        }

        if (lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")) {

            return "image/jpeg";
        }

        if (lower.endsWith(".svg")) {
            return "image/svg+xml";
        }

        return "application/octet-stream";
    }
}
