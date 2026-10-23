package com.carrental.server;

import com.carrental.service.BookingService;
import com.carrental.service.CarService;
import com.carrental.service.FileStorageService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class HttpServer {

    private final int port;

    private final FileStorageService storageService;
    private final CarService carService;
    private final BookingService bookingService;

    private java.util.concurrent.ExecutorService executorService;

    private com.sun.net.httpserver.HttpServer server;

    public HttpServer(int port) {

        this.port = port;

        this.storageService =
                new FileStorageService();

        this.carService =
                new CarService(storageService);

        this.bookingService =
                new BookingService(
                        storageService,
                        carService
                );
    }

    public void start() throws IOException {

        server =
                com.sun.net.httpserver.HttpServer.create(
                        new InetSocketAddress(port),
                        0
                );

        RequestHandler requestHandler =
                new RequestHandler(
                        carService,
                        bookingService
                );

        /*
         * API endpoint.
         *
         * All /api/... requests are handled here.
         */
        server.createContext(
                "/api",
                requestHandler
        );

        /*
         * Frontend files.
         *
         * Requests such as:
         *
         * /
         * /index.html
         * /css/style.css
         * /js/app.js
         *
         * are handled here.
         */
        server.createContext(
                "/",
                requestHandler
        );

        executorService =
                Executors.newFixedThreadPool(10);

        server.setExecutor(executorService);

        server.start();

        System.out.println();
        System.out.println("========================================");
        System.out.println("     CAR RENTAL MANAGEMENT SYSTEM");
        System.out.println("========================================");
        System.out.println();
        System.out.println(
                "Server started successfully!"
        );
        System.out.println(
                "Open: http://localhost:" + port
        );
        System.out.println();
    }

    public void stop() {

        if (server != null) {
            server.stop(1);
        }

        if (executorService != null) {
            executorService.shutdown();
        }

        System.out.println(
                "Car Rental server stopped."
        );
    }

    public CarService getCarService() {
        return carService;
    }

    public BookingService getBookingService() {
        return bookingService;
    }
}