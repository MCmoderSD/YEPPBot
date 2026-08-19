package de.MCmoderSD.api;

import de.MCmoderSD.api.controller.Controller;
import de.MCmoderSD.api.controller.JoinChannel;
import de.MCmoderSD.api.controller.LeaveChannel;
import de.MCmoderSD.api.controller.UpdateBlacklist;
import de.MCmoderSD.api.controller.UpdateCustomCommands;

import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.server.core.Server;

import java.util.HashMap;

import static de.MCmoderSD.utilities.Hasher.sha256Hex;
import static de.MCmoderSD.utilities.MessageHelper.*;

@SuppressWarnings("unused")
public class ApiHandler {

    // Constants
    public static final String BASE_PATH = "/api";

    // Associations
    private final TwitchBot twitchBot;
    private final Server server;

    // Attributes
    private final String key;
    private final HashMap<String, Controller> controllers;

    // Constructor
    public ApiHandler(TwitchBot twitchBot, String clientSecret) {

        // Check Parameters
        if (twitchBot == null) throw new IllegalArgumentException("TwitchBot cannot be null");
        if (clientSecret == null || clientSecret.isBlank()) throw new IllegalArgumentException("Client Secret cannot be null or blank");

        // Set Associations
        this.twitchBot = twitchBot;
        server = twitchBot.getServer();

        // Derive Key from Client Secret
        key = sha256Hex(clientSecret);

        // Initialize Controllers
        controllers = new HashMap<>();
        register(new JoinChannel(twitchBot, key));
        register(new LeaveChannel(twitchBot, key));
        register(new UpdateCustomCommands(twitchBot, key));
        register(new UpdateBlacklist(twitchBot, key));

        // Log
        System.out.printf("%s%s Registered %d API endpoints under %s%s%n", BOLD, API, controllers.size(), BASE_PATH, UNBOLD);
    }

    // Register Controller
    private void register(Controller controller) {

        // Check Parameters
        if (controller == null) throw new IllegalArgumentException("Controller cannot be null");

        // Variables
        var endpoint = controller.getEndpoint();

        // Check if Endpoint is already registered
        if (controllers.containsKey(endpoint)) throw new IllegalArgumentException("Endpoint " + endpoint + " is already registered");

        // Register Controller
        controllers.put(endpoint, controller);
        server.registerPrefixPath(getPath(endpoint), controller);
    }

    // Helper
    public static String getPath(String endpoint) {
        return BASE_PATH + "/" + endpoint;
    }

    // Association Getter
    public TwitchBot getTwitchBot() {
        return twitchBot;
    }

    public Server getServer() {
        return server;
    }

    // Getter
    public String getKey() {
        return key;
    }

    public HashMap<String, Controller> getControllers() {
        return controllers;
    }

    public Controller getController(String endpoint) {
        return controllers.get(endpoint);
    }
}
