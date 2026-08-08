package de.MCmoderSD.main;

import de.MCmoderSD.commands.Weather;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.helix.enums.Scope;
import de.MCmoderSD.helix.handler.UserHandler;
import de.MCmoderSD.helix.handler.ChatHandler;
import de.MCmoderSD.helix.handler.RoleHandler;
import de.MCmoderSD.helix.handler.StreamHandler;
import de.MCmoderSD.helix.handler.ChannelHandler;

import de.MCmoderSD.json.JsonUtility;
import de.MCmoderSD.openai.core.OpenAI;
import de.MCmoderSD.server.core.Server;
import de.MCmoderSD.utilities.MessageHelper;
import de.MCmoderSD.utilities.TokenGrabber;

import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Stream;
import java.util.ArrayList;

import static de.MCmoderSD.helix.enums.Scope.*;
import static de.MCmoderSD.utilities.MessageHelper.ICON;

public class Main {

    // Constants
    public static final String VERSION = "1.26.0-PTB-1";

    // Flags
    public static boolean DEBUG = false;

    // Attributes
    private static final long UPTIME = System.nanoTime();

    void main(String[] args) {
        try {

            // Initialize Twitch Bot
            var twitchBot = init(parseArguments(args));

            // Token Grabber active
            if (twitchBot == null) return;

            // var all = Scope.values();
            var used = Stream.concat(

                    // Handlers Required Scopes
                    Stream.of(
                            UserHandler.REQUIRED_SCOPES,
                            ChatHandler.REQUIRED_SCOPES,
                            RoleHandler.REQUIRED_SCOPES,
                            StreamHandler.REQUIRED_SCOPES,
                            ChannelHandler.REQUIRED_SCOPES
                    ).flatMap(Stream::of),

                    // Additional Scopes
                    Stream.of(
                            CHANNEL_EDIT_COMMERCIAL,
                            CHANNEL_READ_EDITORS,
                            CHANNEL_MANAGE_MODERATORS,
                            CHANNEL_MANAGE_RAIDS,
                            CHANNEL_READ_SUBSCRIPTIONS,
                            CHANNEL_READ_VIPS,
                            CHANNEL_MANAGE_VIPS,
                            MODERATION_READ,
                            MODERATOR_MANAGE_BANNED_USERS,
                            MODERATOR_MANAGE_CHAT_MESSAGES,
                            MODERATOR_READ_CHATTERS,
                            MODERATOR_READ_FOLLOWERS,
                            MODERATOR_MANAGE_SHOUTOUTS,
                            USER_READ_BLOCKED_USERS,
                            USER_MANAGE_BLOCKED_USERS,
                            USER_READ_EMAIL
                    )

            ).distinct().toArray(Scope[]::new);

            IO.println("Authenticate: " + twitchBot.getHelixHandler().getAuthorizationUrl(used));

        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to initialize Twitch Bot", e);
        }

        // Print Uptime
        IO.println("Twitch Bot startup took " + ((System.nanoTime() - UPTIME) / 1_000_000) + " ms");
        IO.println("Twitch Bot is now running. Version: " + VERSION);
    }

    private static TwitchBot init(ArrayList<String> args) throws IOException, URISyntaxException {

        // Print Startup Message
        System.out.printf("%s%nYEPPBot v%s is starting up...%n", ICON, VERSION);

        // Check Arguments
        var argSize = args.size();
        var dev = args.contains("-dev");
        var debug = args.contains("-debug");

        // Set Debug Mode
        if (debug || dev) {
            DEBUG = true;
            System.out.printf("%s %s%n", MessageHelper.DEBUG, "Debug mode is enabled");
        }

        // Load Config
        JsonNode config;
        var jsonUtility = JsonUtility.getInstance();
        if (argSize > 1 && (args.contains("-config") || args.contains("-c"))) {

            // Determine Config Path
            var index = args.indexOf("-config");
            if (index == -1) index = args.indexOf("-c");
            if (index + 1 >= argSize) throw new IllegalArgumentException("No config file path provided after " + args.get(index));

            // Load Config
            config = jsonUtility.loadFile(args.get(index + 1));

        } else if (dev) config = jsonUtility.loadResource("/config/dev-config.json");
        else config = jsonUtility.loadResource("/config/config.json");

        // Check Config
        if (config == null || config.isNull() || config.isEmpty()) throw new IllegalArgumentException("Config file is missing or empty");
        if (!config.has("twitch") || config.get("twitch").isNull() || config.get("twitch").isEmpty()) throw new IllegalArgumentException("Config file missing 'twitch' section");
        if (!config.has("database") || config.get("database").isNull() || config.get("database").isEmpty()) throw new IllegalArgumentException("Config file missing 'database' section");
        if (!config.has("server") || config.get("server").isNull() || config.get("server").isEmpty()) throw new IllegalArgumentException("Config file missing 'server' section");

        // Get Config Sections
        var twitchConfig = config.get("twitch");
        var databaseConfig = config.get("database");
        var serverConfig = config.get("server");

        // Initialize and Start Server
        var server = new Server(serverConfig);
        server.start();

        // Init OpenAI
        var openAI = initOpenAI(config);
        if (openAI == null) System.err.println("Warning: OpenAI configuration is missing. OpenAI features will be unavailable.");

        // Initialize Weather API if Configured
        if (config.has("openweathermap")) Weather.api = config.get("openweathermap");
        else System.err.println("Warning: OpenWeatherMap configuration is missing. Weather command will be unavailable.");

        // Initialize Token Grabber if needed
        if (needsTokenGrabber(twitchConfig, server)) return null;

        // Initialize Twitch Bot
        return new TwitchBot(twitchConfig, databaseConfig, server, openAI);
    }

    private static ArrayList<String> parseArguments(String[] args) {
        var arguments = new ArrayList<String>();
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
        var missingToken = !twitchConfig.has("oauthToken") || twitchConfig.get("oauthToken").isNull() || !twitchConfig.get("oauthToken").isString();
        if (!missingToken && !twitchConfig.get("oauthToken").asString().isBlank()) return false;

        // Check Application Config
        if (!twitchConfig.has("application") || twitchConfig.get("application").isNull() || twitchConfig.get("application").isEmpty()) throw new IllegalArgumentException("Twitch config missing 'application' section");

        // Start Token Grabber
        new TokenGrabber(twitchConfig.get("application"), server);
        return true;
    }

    private static OpenAI initOpenAI(JsonNode config) {

        // Check Config
        if (config == null || config.isNull() || config.isEmpty()) throw new IllegalArgumentException("Config cannot be null or empty");
        if (!config.has("openai") || config.get("openai").isNull() || config.get("openai").isEmpty()) return null;

        // Get OpenAI Config
        var openAIConfig = config.get("openai");

        // Check API Key
        if (!openAIConfig.has("apiKey") || openAIConfig.get("apiKey").isNull() || !openAIConfig.get("apiKey").isString()) throw new IllegalArgumentException("OpenAI config missing 'apiKey'");
        if (!openAIConfig.has("organization") || openAIConfig.get("organization").isNull() || !openAIConfig.get("organization").isString()) throw new IllegalArgumentException("OpenAI config missing 'organization'");
        if (!openAIConfig.has("project") || openAIConfig.get("project").isNull() || !openAIConfig.get("project").isString()) throw new IllegalArgumentException("OpenAI config missing 'project'");

        // Parse Config
        var apiKey = openAIConfig.get("apiKey").asString();
        var organization = openAIConfig.get("organization").asString();
        var project = openAIConfig.get("project").asString();

        if (!apiKey.startsWith("sk-")) throw new  IllegalArgumentException("Invalid OpenAI API Key");
        if (!organization.startsWith("org-")) throw new IllegalArgumentException("Invalid OpenAI Organization ID");
        if (!project.startsWith("proj_")) throw new IllegalArgumentException("Invalid OpenAI Project");

        // Initialize and return OpenAI instance
        return new OpenAI(apiKey, organization, project);
    }
}