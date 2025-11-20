package de.MCmoderSD.main;

import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.helix.enums.Scope;
import de.MCmoderSD.helix.handler.*;
import de.MCmoderSD.json.JsonUtility;
import de.MCmoderSD.server.core.Server;
import de.MCmoderSD.utilities.TokenGrabber;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class Main {

    // Constants
    public static final String VERSION = "1.25.0-PTB-1";

    // Flags
    public static boolean DEBUG = false;

    // Attributes
    private static final long UPTIME = System.nanoTime();

    public static void main(String[] args) throws IOException, URISyntaxException {

        // Load Config
        JsonNode config;
        if (args.length == 2 && args[0].equalsIgnoreCase("-c")) {
            config = JsonUtility.getInstance().load(args[1], true);
        }
        else if (args.length == 1 && args[0].equalsIgnoreCase("-dev")) {
            config = JsonUtility.getInstance().load("/config/dev-config.json");
            DEBUG = true;
        } else config = JsonUtility.getInstance().load("/config/config.json");

        // Check Config
        if (config == null || config.isNull() || config.isEmpty()) throw new IllegalArgumentException("Config file is missing or empty");
        if (!config.has("twitch") || config.get("twitch").isNull() || config.get("twitch").isEmpty()) throw new IllegalArgumentException("Config file missing 'twitch' section");
        if (!config.has("database") || config.get("database").isNull() || config.get("database").isEmpty()) throw new IllegalArgumentException("Config file missing 'database' section");
        if (!config.has("server") || config.get("server").isNull() || config.get("server").isEmpty()) throw new IllegalArgumentException("Config file missing 'server' section");

        // Initialize Server
        Server server = new Server(config.get("server"));
        server.start();

        // Check for missing OAuth Token
        JsonNode twitchConfig = config.get("twitch");
        if (twitchConfig.has("oauthToken") && (twitchConfig.get("oauthToken").isNull() || !twitchConfig.get("oauthToken").isString() || twitchConfig.get("oauthToken").asString().isBlank())) {
            System.out.println("OAuth Token is missing or invalid in config. Generating authorization URL...");
            if (twitchConfig.has("application") && !twitchConfig.get("application").isNull() && !twitchConfig.get("application").isEmpty()) new TokenGrabber(twitchConfig.get("application"), server);
            else System.out.println("Twitch application config is missing or invalid. Cannot generate authorization URL.");
            return;
        }

        // Initialize Twitch Bot
        TwitchBot twitchBot = new TwitchBot(config.get("twitch"), config.get("database"), server);

        Scope[] all = Scope.values();
        Scope[] used = new ArrayList<>(Arrays.asList(UserHandler.REQUIRED_SCOPES, ChatHandler.REQUIRED_SCOPES, RoleHandler.REQUIRED_SCOPES, StreamHandler.REQUIRED_SCOPES, ChannelHandler.REQUIRED_SCOPES))
                .stream()
                .flatMap(Stream::of)
                .distinct()
                .toArray(Scope[]::new);

        System.out.println("Authenticate: " + twitchBot.getHelixHandler().getAuthorizationUrl(used));
        System.out.println("Twitch Bot startup took " + ((System.nanoTime() - UPTIME) / 1_000_000) + " ms");
        System.out.println("Twitch Bot is now running. Version: " + VERSION);
    }
}