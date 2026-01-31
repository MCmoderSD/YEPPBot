package de.MCmoderSD.utilities;

import de.MCmoderSD.helix.enums.Scope;
import de.MCmoderSD.server.core.Server;
import de.MCmoderSD.server.modules.HtmlModule;

import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;

import static de.MCmoderSD.helix.utilities.ConfigValidator.validateApplicationConfig;
import static de.MCmoderSD.server.modules.HtmlModule.mountHtml;

public class TokenGrabber {

    // Endpoint
    private static final String AUTH_URL = "https://id.twitch.tv/oauth2/authorize";

    // Constructor
    public TokenGrabber(JsonNode application, Server server) {

        // Validate Parameters
        if (application == null || application.isNull() || application.isEmpty()) throw new IllegalArgumentException("Application config cannot be null or empty");
        if (server == null) throw new IllegalArgumentException("Server instance cannot be null");

        // Check Application Config
        if (!validateApplicationConfig(application)) throw new IllegalArgumentException("Invalid Application Config");

        // Get Twitch Credentials
        JsonNode credentials = application.get("credentials");
        String clientId = credentials.get("clientId").asString();
        String redirectURL = application.get("oAuthRedirectURL").asString();

        // Print Credentials
        IO.println("\nTwitch Application Credentials:");
        IO.println(" - Client ID: " + clientId);
        IO.println(" - OAuth Redirect URL: " + redirectURL);

        // Build Scopes
        StringBuilder scopeBuilder = new StringBuilder();
        for (var scope : Scope.values()) scopeBuilder.append(scope.getScope()).append("+");
        scopeBuilder.deleteCharAt(scopeBuilder.length() - 1);

        // Load HTML Resource
        String htmlPage;
        String resourcePath = "/html/TokenGrabber.html";
        try (InputStream stream = HtmlModule.class.getResourceAsStream(resourcePath)) {
            if (stream == null) throw new RuntimeException("Error loading resource: " + resourcePath);
            htmlPage = new String(stream.readAllBytes()).replaceAll("<%clientId%>", clientId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + resourcePath, e);
        }

        // Mount HTML Page
        mountHtml(server, htmlPage.getBytes(), redirectURL.substring(redirectURL.lastIndexOf('/')));

        // Authorization URL
        System.out.printf("\nPlease Authorize with your bot account, to obtain the OAuth Token:\n%s?client_id=%s&redirect_uri=%s&response_type=token&scope=%s%n%n%n%n",
                AUTH_URL,
                clientId,
                redirectURL,
                scopeBuilder
        );
    }
}