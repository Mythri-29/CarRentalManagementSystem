package com.carrental.server;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class ResponseUtil {

    private ResponseUtil() {
        // Utility class
    }

    public static void sendJson(
            HttpExchange exchange,
            int statusCode,
            String json) throws IOException {

        byte[] response =
                json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

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
                statusCode,
                response.length
        );

        try (OutputStream outputStream =
                     exchange.getResponseBody()) {

            outputStream.write(response);
        }
    }

    public static void sendText(
            HttpExchange exchange,
            int statusCode,
            String content,
            String contentType) throws IOException {

        byte[] response =
                content.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                contentType
        );

        exchange.sendResponseHeaders(
                statusCode,
                response.length
        );

        try (OutputStream outputStream =
                     exchange.getResponseBody()) {

            outputStream.write(response);
        }
    }

    public static void sendError(
            HttpExchange exchange,
            int statusCode,
            String message) throws IOException {

        String json =
                "{\"success\":false,\"message\":\""
                        + escapeJson(message)
                        + "\"}";

        sendJson(exchange, statusCode, json);
    }

    public static void sendSuccess(
            HttpExchange exchange,
            String message) throws IOException {

        String json =
                "{\"success\":true,\"message\":\""
                        + escapeJson(message)
                        + "\"}";

        sendJson(exchange, 200, json);
    }

    public static String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
