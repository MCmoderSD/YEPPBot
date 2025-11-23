package de.MCmoderSD.main;

import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.helix.handler.*;
import de.MCmoderSD.json.JsonUtility;
import de.MCmoderSD.server.core.Server;
import de.MCmoderSD.utilities.MessageHelper;
import de.MCmoderSD.utilities.TokenGrabber;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static de.MCmoderSD.utilities.MessageHelper.ICON;

public class Main {

    // Constants
    public static final String VERSION = "1.25.0-PTB-1";

    // Flags
    public static boolean DEBUG = false;

    // Attributes
    private static final long UPTIME = System.nanoTime();

    @SuppressWarnings("CommentedOutCode")
    public static void main(String[] args) {
        try {

            // Initialize Twitch Bot
            TwitchBot twitchBot = init(parseArguments(args));

            // Token Grabber active
            if (twitchBot == null) return;

        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to initialize Twitch Bot", e);
        }

//        Scope[] all = Scope.values();
//        Scope[] used = new ArrayList<>(Arrays.asList(
//                UserHandler.REQUIRED_SCOPES,
//                ChatHandler.REQUIRED_SCOPES,
//                RoleHandler.REQUIRED_SCOPES,
//                StreamHandler.REQUIRED_SCOPES,
//                ChannelHandler.REQUIRED_SCOPES)
//        ).stream().flatMap(Stream::of).distinct().toArray(Scope[]::new);
//        System.out.println("Authenticate: " + twitchBot.getHelixHandler().getAuthorizationUrl(used));

        System.out.println("Twitch Bot startup took " + ((System.nanoTime() - UPTIME) / 1_000_000) + " ms");
        System.out.println("Twitch Bot is now running. Version: " + VERSION);
    }

    private static TwitchBot init(ArrayList<String> args) throws IOException, URISyntaxException {

        // Print Startup Message
        System.out.printf("%s%nYEPPBot v%s is starting up...%n", ICON, VERSION);

        // Check Arguments
        var argSize = args.size();
        boolean dev = args.contains("-dev");
        boolean debug = args.contains("-debug");

        // Set Debug Mode
        if (debug || dev) {
            DEBUG = true;
            System.out.println(MessageHelper.DEBUG + "Debug mode is enabled");
        }

        // Load Config
        JsonNode config;
        JsonUtility jsonUtility = JsonUtility.getInstance();
        if (argSize > 1 && (args.contains("-config") || args.contains("-c"))) {

            // Determine Config Path
            var index = args.indexOf("-config");
            if (index == -1) index = args.indexOf("-c");
            if (index + 1 >= argSize) throw new IllegalArgumentException("No config file path provided after " + args.get(index));

            // Load Config
            config = jsonUtility.load(args.get(index + 1), true);

        } else if (dev) config = JsonUtility.getInstance().load("/config/dev-config.json");
        else config = JsonUtility.getInstance().load("/config/config.json");


        // Check Config
        if (config == null || config.isNull() || config.isEmpty()) throw new IllegalArgumentException("Config file is missing or empty");
        if (!config.has("twitch") || config.get("twitch").isNull() || config.get("twitch").isEmpty()) throw new IllegalArgumentException("Config file missing 'twitch' section");
        if (!config.has("database") || config.get("database").isNull() || config.get("database").isEmpty()) throw new IllegalArgumentException("Config file missing 'database' section");
        if (!config.has("server") || config.get("server").isNull() || config.get("server").isEmpty()) throw new IllegalArgumentException("Config file missing 'server' section");

        // Get Config Sections
        JsonNode twitchConfig = config.get("twitch");
        JsonNode databaseConfig = config.get("database");
        JsonNode serverConfig = config.get("server");

        // Initialize and Start Server
        Server server = new Server(serverConfig);
        server.start();

        // Initialize Token Grabber if needed
        if (needsTokenGrabber(twitchConfig, server)) return null;

        // Initialize Twitch Bot
        return new TwitchBot(twitchConfig, databaseConfig, server);
    }

    private static ArrayList<String> parseArguments(String[] args) {
        ArrayList<String> arguments = new ArrayList<>();
        if (args != null) {
            for (var arg : args) {
                if (arg != null && !arg.isBlank()) {
                    if (!arg.startsWith("-")) arguments.add(arg.trim());
                    else {
                        while (arg.startsWith("-")) arg = arg.substring(1);
                        arguments.add("-" + arg.trim());
                    }
                }
            }
        }
        return arguments;
    }

    private static boolean needsTokenGrabber(JsonNode twitchConfig, Server server) {

        // Check Parameters
        if (server == null) throw new IllegalArgumentException("Server instance cannot be null");
        if (twitchConfig == null || twitchConfig.isNull() || twitchConfig.isEmpty()) throw new IllegalArgumentException("Twitch config cannot be null or empty");

        // Check OAuth Token
        boolean missingToken = !twitchConfig.has("oauthToken") || twitchConfig.get("oauthToken").isNull() || !twitchConfig.get("oauthToken").isString();
        if (!missingToken && !twitchConfig.get("oauthToken").asString().isBlank()) return false;

        // Check Application Config
        if (!twitchConfig.has("application") || twitchConfig.get("application").isNull() || twitchConfig.get("application").isEmpty()) throw new IllegalArgumentException("Twitch config missing 'application' section");

        // Start Token Grabber
        new TokenGrabber(twitchConfig.get("application"), server);
        return true;
    }
}