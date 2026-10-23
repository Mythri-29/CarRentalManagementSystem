package com.carrental;

import com.carrental.server.HttpServer;

public class Main {

    public static void main(String[] args) {

        HttpServer server =
                new HttpServer(8080);

        try {

            server.start();

            /*
             * Keep the main thread alive while
             * the HTTP server handles requests.
             */
            Runtime.getRuntime()
                    .addShutdownHook(
                            new Thread(server::stop)
                    );

        } catch (Exception e) {

            System.err.println(
                    "Failed to start server."
            );

            e.printStackTrace();
        }
    }
}