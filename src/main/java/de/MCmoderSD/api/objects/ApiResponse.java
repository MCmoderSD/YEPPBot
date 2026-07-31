package de.MCmoderSD.api.objects;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

@SuppressWarnings("unused")
public record ApiResponse(int status, boolean success, String message) {

    // Constants
    public static final String CONTENT_TYPE = "application/json; charset=UTF-8";

    // Status Codes
    public static final int OK = 200;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int INTERNAL_ERROR = 500;

    // Constructor
    public ApiResponse {

        // Check Parameters
        if (status < 100 || status > 599) throw new IllegalArgumentException("Status code must be between 100 and 599");
        if (message == null || message.isBlank()) throw new IllegalArgumentException("Message cannot be null or blank");
    }

    // Factories
    public static ApiResponse ok(String message) {
        return new ApiResponse(OK, true, message);
    }

    public static ApiResponse badRequest(String message) {
        return new ApiResponse(BAD_REQUEST, false, message);
    }

    public static ApiResponse unauthorized(String message) {
        return new ApiResponse(UNAUTHORIZED, false, message);
    }

    public static ApiResponse notFound(String message) {
        return new ApiResponse(NOT_FOUND, false, message);
    }

    public static ApiResponse methodNotAllowed(String message) {
        return new ApiResponse(METHOD_NOT_ALLOWED, false, message);
    }

    public static ApiResponse internalError(String message) {
        return new ApiResponse(INTERNAL_ERROR, false, message);
    }

    // Send Response
    public void send(HttpServerExchange exchange) {

        // Check Parameters
        if (exchange == null) throw new IllegalArgumentException("HttpServerExchange cannot be null");

        // Send Response
        exchange.setStatusCode(status);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, CONTENT_TYPE);
        exchange.getResponseSender().send(toJson());
    }

    // Serialize Response
    public String toJson() {
        return "{\"success\":" + success + ",\"status\":" + status + ",\"message\":\"" + escape(message) + "\"}";
    }

    // Escape JSON String
    private static String escape(String value) {

        // Variables
        var builder = new StringBuilder(value.length() + 16);

        // Escape Characters
        for (var character : value.toCharArray()) switch (character) {
            case '"' -> builder.append("\\\"");
            case '\\' -> builder.append("\\\\");
            case '\b' -> builder.append("\\b");
            case '\f' -> builder.append("\\f");
            case '\n' -> builder.append("\\n");
            case '\r' -> builder.append("\\r");
            case '\t' -> builder.append("\\t");
            default -> {
                if (character < 0x20) builder.append(String.format("\\u%04x", (int) character));
                else builder.append(character);
            }
        }

        // Return
        return builder.toString();
    }
}
