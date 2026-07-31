package de.MCmoderSD.api.controller;

import de.MCmoderSD.api.objects.ApiResponse;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.helix.handler.UserHandler;
import de.MCmoderSD.helix.objects.TwitchUser;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.Methods;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static de.MCmoderSD.main.Main.DEBUG;
import static de.MCmoderSD.utilities.MessageHelper.*;

@SuppressWarnings("unused")
public abstract class Controller implements HttpHandler {

    // Constants
    public static final String API_KEY_HEADER = "X-Api-Key";
    public static final String BEARER_PREFIX = "Bearer ";

    // Associations
    protected final TwitchBot twitchBot;
    protected final UserHandler userHandler;

    // Attributes
    private final String endpoint;
    private final String key;

    // Constructor
    protected Controller(TwitchBot twitchBot, String endpoint, String key) {

        // Check Parameters
        if (twitchBot == null) throw new IllegalArgumentException("TwitchBot cannot be null");
        if (endpoint == null || endpoint.isBlank() || endpoint.contains(SPACE)) throw new IllegalArgumentException("Endpoint cannot be null, blank or contain spaces");
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Key cannot be null or blank");

        // Set Associations
        this.twitchBot = twitchBot;
        userHandler = twitchBot.getUserHandler();

        // Set Attributes
        this.endpoint = endpoint;
        this.key = key;
    }

    // Handle Request
    @Override
    public void handleRequest(HttpServerExchange exchange) {

        // Dispatch to Worker Thread, since the Controller blocks
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        // Check Method
        if (!Methods.POST.equals(exchange.getRequestMethod())) {
            ApiResponse.methodNotAllowed("Endpoint " + endpoint + " only accepts POST requests").send(exchange);
            return;
        }

        // Check Authorization
        if (!isAuthorized(exchange)) {
            System.out.printf("%s%s %s Unauthorized request on endpoint: %s%s%n", BOLD, API, WARNING, endpoint, UNBOLD);
            ApiResponse.unauthorized("Invalid or missing API key").send(exchange);
            return;
        }

        // Parse User ID
        var userId = parseUserId(exchange.getRelativePath());
        if (userId == null) {
            ApiResponse.badRequest("Invalid or missing user ID, expected: /" + endpoint + "/{userID}").send(exchange);
            return;
        }

        // Resolve Channel
        TwitchUser channel;
        try {
            channel = userHandler.getTwitchUser(userId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            ApiResponse.notFound("No Twitch user found with ID: " + userId).send(exchange);
            return;
        }

        // Execute Controller
        try {
            var response = execute(channel);
            if (DEBUG) System.out.printf("%s%s %s: %s%s%n", BOLD, API, endpoint, response.message(), UNBOLD);
            response.send(exchange);
        } catch (Exception e) {
            System.err.printf("%s%s %s Failed to execute endpoint %s: %s%s%n", BOLD, API, ERROR, endpoint, e.getMessage(), UNBOLD);
            ApiResponse.internalError("Failed to execute endpoint " + endpoint).send(exchange);
        }
    }

    // Check Authorization
    private boolean isAuthorized(HttpServerExchange exchange) {

        // Get Headers
        var headers = exchange.getRequestHeaders();
        var provided = headers.getFirst(Headers.AUTHORIZATION);

        // Fallback to API Key Header
        if (provided == null || provided.isBlank()) provided = headers.getFirst(API_KEY_HEADER);
        if (provided == null || provided.isBlank()) return false;

        // Strip Bearer Prefix
        provided = provided.trim();
        if (provided.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) provided = provided.substring(BEARER_PREFIX.length()).trim();

        // Compare in Constant Time
        return MessageDigest.isEqual(
                provided.toLowerCase().getBytes(StandardCharsets.UTF_8),
                key.getBytes(StandardCharsets.UTF_8)
        );
    }

    // Parse User ID
    private static Integer parseUserId(String path) {

        // Check Path
        if (path == null || path.isBlank()) return null;

        // Trim Slashes
        while (path.startsWith("/")) path = path.substring(1);
        while (path.endsWith("/")) path = path.substring(0, path.length() - 1);

        // Check Segment
        if (path.isBlank() || path.contains("/")) return null;
        for (var character : path.toCharArray()) if (character < '0' || character > '9') return null;

        // Parse User ID
        try {
            var userId = Integer.parseInt(path);
            return userId > 0 ? userId : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Execute Controller
    protected abstract ApiResponse execute(TwitchUser channel);

    // Getter
    public String getEndpoint() {
        return endpoint;
    }
}
