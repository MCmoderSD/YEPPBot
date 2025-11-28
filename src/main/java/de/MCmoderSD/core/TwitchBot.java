package de.MCmoderSD.core;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.TwitchClientHelper;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.helix.TwitchHelix;

import de.MCmoderSD.commands.*;

import de.MCmoderSD.database.Database;
import de.MCmoderSD.database.manager.*;

import de.MCmoderSD.handlers.EventHandler;
import de.MCmoderSD.handlers.MessageHandler;
import de.MCmoderSD.handlers.BirthdayHandler;
import de.MCmoderSD.handlers.LurkHandler;
import de.MCmoderSD.handlers.CommandHandler;

import de.MCmoderSD.helix.core.HelixHandler;
import de.MCmoderSD.helix.handler.ChannelHandler;
import de.MCmoderSD.helix.handler.ChatHandler;
import de.MCmoderSD.helix.handler.RoleHandler;
import de.MCmoderSD.helix.handler.StreamHandler;
import de.MCmoderSD.helix.handler.UserHandler;
import de.MCmoderSD.helix.objects.TwitchUser;

import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.server.core.Server;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.github.twitch4j.chat.util.TwitchChatLimitHelper.USER_JOIN_LIMIT;
import static de.MCmoderSD.sql.Driver.DatabaseType.MARIADB;
import static de.MCmoderSD.helix.core.HelixHandler.*;
import static de.MCmoderSD.utilities.ConfigValidator.*;
import static de.MCmoderSD.utilities.MessageHelper.*;
import static java.lang.Math.round;

public class TwitchBot {

    // Server
    private final Server server;

    // Database
    private final Database database;
    private final ChannelManager channelManager;
    private final CommandManager commandManager;
    private final EventLogManager eventLogManager;
    private final BirthdayManager birthdayManager;
    private final LurkManager lurkManager;
    private final QuoteManager quoteManager;

    // Configuration
    private final TwitchUser botUser;               // Bot User
    private final HashSet<TwitchUser> owners;       // Owners
    private final HashSet<String> botAliases;       // Bot Aliases
    private final ArrayList<String> prefixes;       // Command Prefixes
    private final String prefix;                    // Primary Command Prefix

    // Attributes
    private final TwitchClient client;              // Twitch Client
    private final TwitchChat chat;                  // Twitch Chat
    private final TwitchHelix helix;                // Twitch Helix
    private final TwitchClientHelper helper;        // Twitch Client Helper
    private final EventManager eventManager;        // Event Manager

    // Helix-Handlers
    private final HelixHandler helixHandler;        // Helix Handler
    private final UserHandler userHandler;          // User Handler
    private final ChatHandler chatHandler;          // Chat Handler
    private final RoleHandler roleHandler;          // Role Handler
    private final StreamHandler streamHandler;      // Stream Handler
    private final ChannelHandler channelHandler;    // Channel Handler

    // Bot-Handlers
    private final EventHandler eventHandler;        // Event Handler
    private final MessageHandler messageHandler;    // Message Handler
    private final BirthdayHandler birthdayHandler;  // Birthday Handler
    private final LurkHandler lurkHandler;          // Lurk Handler
    private final CommandHandler commandHandler;    // Command Handler

    // Constructor
    public TwitchBot(JsonNode twitchConfig, JsonNode databaseConfig, Server server) {

        // Check Parameters
        if (twitchConfig == null || twitchConfig.isNull() || twitchConfig.isEmpty()) throw new IllegalArgumentException("Twitch config cannot be null or empty");
        if (databaseConfig == null || databaseConfig.isNull() || databaseConfig.isEmpty()) throw new IllegalArgumentException("Database config cannot be null or empty");
        if (server == null) throw new IllegalArgumentException("Server cannot be null");

        // Validate Config
        if (!validateTwitchConfig(twitchConfig)) throw new IllegalArgumentException("Twitch config is invalid");
        if (!validateDatabaseConfig(databaseConfig)) throw new IllegalArgumentException("Database config is invalid");

        // Set Associations
        this.server = server;

        // Initialize Database
        database = new Database(MARIADB, databaseConfig, this);
        channelManager = database.getChannelManager();
        commandManager = database.getCommandManager();
        eventLogManager = database.getEventLogManager();
        birthdayManager = database.getBirthdayManager();
        lurkManager = database.getLurkManager();
        quoteManager = database.getQuoteManager();

        // Parse Owners
        HashSet<Integer> ownerIds = new HashSet<>();
        for (var owner : twitchConfig.get("owner")) ownerIds.add(owner.asInt());

        // Parse Bot Aliases
        botAliases = new HashSet<>();
        for (var alias : twitchConfig.get("botAlias")) botAliases.add(alias.asString());

        // Parse Config
        JsonNode applicationConfig = twitchConfig.get("application");
        JsonNode credentialConfig = applicationConfig.get("credentials");
        String oauthToken = twitchConfig.get("oauthToken").asString();
        String clientId = credentialConfig.get("clientId").asString();
        String clientSecret = credentialConfig.get("clientSecret").asString();

        // Parse Prefixes
        prefixes = new ArrayList<>();
        for (var prefix : twitchConfig.get("prefix")) prefixes.add(prefix.asString());
        prefix = prefixes.getFirst();

        // Initialize Twitch Client Builder
        TwitchClientBuilder clientBuilder = TwitchClientBuilder.builder();
        OAuth2Credential defaultAuthToken = new OAuth2Credential(PROVIDER, oauthToken);
        CredentialManager credentialManager = CredentialManagerBuilder.builder().build();

        // Configure Application
        clientBuilder = clientBuilder
                .withClientId(clientId)                     // Set Client ID
                .withClientSecret(clientSecret)             // Set Client Secret
                .withChatAccount(defaultAuthToken)          // Set OAuth Token (Bot Chat Account)
                .withDefaultAuthToken(defaultAuthToken)     // Set OAuth Token (Bot Account)
                .withCredentialManager(credentialManager);  // Set Credential Manager

        // Obtain Twitch Users
        botUser = obtainBotUser(defaultAuthToken);
        owners = obtainOwnerUsers(ownerIds, defaultAuthToken);
        owners.add(botUser);

        // Set Owners and Prefixes
        for (var owner : owners) clientBuilder =  clientBuilder.withBotOwnerId(owner.getId().toString());   // Set Owners
        for (var prefix : prefixes) clientBuilder = clientBuilder.withCommandTrigger(prefix);               // Set Prefixes

        // Enable Chat and Helix
        clientBuilder = clientBuilder
                .withEnableChat(true)   // Enable Chat
                .withEnableHelix(true); // Enable Helix

        // Configure Rate Limits
        clientBuilder = clientBuilder.withChatMaxJoinRetries(0); // Disable Limit

        // Build Twitch Client
        client = clientBuilder.build();

        // Initialize Attributes
        chat = client.getChat();
        helix = client.getHelix();
        helper = client.getClientHelper();
        eventManager = client.getEventManager();

        // Initialize HelixHandler and Sub-Handlers
        helixHandler = new HelixHandler(applicationConfig, databaseConfig, server, helix, credentialManager);
        userHandler = helixHandler.getUserHandler();
        chatHandler = helixHandler.getChatHandler();
        roleHandler = helixHandler.getRoleHandler();
        streamHandler = helixHandler.getStreamHandler();
        channelHandler = helixHandler.getChannelHandler();

        // Initialize Bot Handler
        eventHandler = new EventHandler(this);
        messageHandler = eventHandler.getMessageHandler();
        birthdayHandler = eventHandler.getBirthdayHandler();
        lurkHandler = eventHandler.getLurkHandler();
        commandHandler = eventHandler.getCommandHandler();

        // Initialize Commands
        new Lurk(this);
        new Moderate(this);
        new Ping(this);
        new Quote(this);
        new RoleSwap(this);
        new Say(this);
        new Shoutout(this);
        new Status(this);

        // Add Initial Channels from Config to Database
        HashSet<TwitchUser> configChannels = checkChannelConfig(twitchConfig, userHandler);
        for (var channel : configChannels) channelManager.joinChannel(channel);
        channelManager.joinChannel(botUser); // Ensure Bot Joins Its Own Channel

        // Obtain Channels from Database
        HashMap<TwitchUser, Boolean> channels = database.getChannelManager().getChannels();

        // Join with Rate Limit Handling
        var delay = round((((double) USER_JOIN_LIMIT.getRefillPeriodNanos() / (double) USER_JOIN_LIMIT.getCapacity()) * 1.1d) / 1_000_000d); // Delay in ms with 10% buffer
        for (var channel : channels.entrySet()) if (channel.getValue()) {
            try {
                joinChannel(channel.getKey());  // Join Channel
                Thread.sleep(delay);            // Wait to avoid rate limits
            } catch (InterruptedException e) {
                throw new RuntimeException("Failed to join channels from database: " + e.getMessage(), e);
            }
        }
    }

    // Helpers
    private static TwitchUser obtainBotUser(OAuth2Credential defaultAuthToken) {
        return new TwitchUser(TwitchClientBuilder.builder().withEnableHelix(true).build().getHelix().getUsers(defaultAuthToken.getAccessToken(), null, null).execute().getUsers().getFirst());
    }

    private static HashSet<TwitchUser> obtainOwnerUsers(HashSet<Integer> ownerIds, OAuth2Credential defaultAuthToken) {

        // Check Parameters
        if (ownerIds == null || ownerIds.isEmpty()) throw new IllegalArgumentException("Owner IDs cannot be null or empty");
        if (defaultAuthToken == null) throw new IllegalArgumentException("Default Auth Token cannot be null");

        // Variables
        HashSet<TwitchUser> owners = new HashSet<>();
        TwitchHelix tempHelix = TwitchClientBuilder.builder().withEnableHelix(true).build().getHelix();

        // Check size and chunk
        var size = ownerIds.size();
        if (size > 100) {
            for (var i = 0; i < size; i += 100) owners.addAll(obtainOwnerUsers(new HashSet<>(ownerIds.stream().toList().subList(i, Math.min(i + 100, size))), defaultAuthToken));
            return owners;
        }

        // Get Users
        var userList = tempHelix.getUsers(defaultAuthToken.getAccessToken(), ownerIds.stream().map(Object::toString).toList(), null).execute();
        if (userList == null) throw new IllegalStateException("Failed to get owner users");
        var users = userList.getUsers();
        if (users == null) throw new IllegalStateException("Failed to get owner users");

        // Create TwitchUser objects
        for (var user : users) owners.add(new TwitchUser(user));

        // Check if all owners were found
        if (owners.size() != ownerIds.size()) {
            HashSet<Integer> foundIds = new HashSet<>();
            for (var owner : owners) foundIds.add(owner.getId());
            for (var id : ownerIds) if (!foundIds.contains(id)) throw new IllegalStateException("Owner with ID " + id + " was not found");
        }

        // Return Owners
        return owners;
    }

    private static HashSet<TwitchUser> checkChannelConfig(JsonNode twitchConfig, UserHandler userHandler) {

        // Check Parameters
        if (twitchConfig == null || twitchConfig.isNull() || twitchConfig.isEmpty()) throw new IllegalArgumentException("Twitch config cannot be null or empty");
        if (userHandler == null) throw new IllegalArgumentException("UserHandler cannot be null");

        // Check Config
        if (!twitchConfig.has("channel") || twitchConfig.get("channel").isNull() || twitchConfig.get("channel").isEmpty() || !twitchConfig.get("channel").isArray()) return new HashSet<>();

        // Parse Channels
        JsonNode channels = twitchConfig.get("channel");
        HashSet<String> channelNames = new HashSet<>();
        for (var channel : channels) {
            if (channel == null || channel.isNull() || !channel.isString()) throw new IllegalArgumentException("Channel name cannot be null and must be a string");
            channelNames.add(channel.asString().toLowerCase());
        }

        // Obtain TwitchUsers
        return userHandler.getTwitchUsersByName(channelNames);
    }

    // Setters
    public boolean joinChannel(TwitchUser channel) {

        // Check Parameters
        if (channel == null) throw new IllegalArgumentException("TwitchUser cannot be null");

        // Check if Already Joined
        if (isChannelJoined(channel)) return true;

        // Join Channel
        chat.joinChannel(channel.getUsername());
        boolean success = isChannelJoined(channel);

        // Log
        if (success) {
            System.out.printf("%s%s Joined Channel: %s%s%n", BOLD, SYSTEM, channel.getDisplayName(), UNBOLD);
            database.getChannelManager().joinChannel(channel);
        } else System.out.printf("%s%s Failed to Join Channel: %s%s%n", BOLD, SYSTEM, channel.getDisplayName(), UNBOLD);

        // Return
        return success;
    }

    public boolean leaveChannel(TwitchUser channel) {

        // Check Parameters
        if (channel == null) throw new IllegalArgumentException("TwitchUser cannot be null");

        // Check if Joined
        if (!isChannelJoined(channel)) return true;

        // Leave Channel
        chat.leaveChannel(channel.getUsername());
        boolean success = !isChannelJoined(channel);

        // Log
        if (success) {
            System.out.printf("%s%s Left Channel: %s%s%n", BOLD, SYSTEM, channel.getDisplayName(), UNBOLD);
            database.getChannelManager().leaveChannel(channel);
        } else System.out.printf("%s%s Failed to Leave Channel: %s%s%n", BOLD, SYSTEM, channel.getDisplayName(), UNBOLD);

        // Return
        return success;
    }

    public boolean sendMessage(MessageEvent event, String command, String message) {

        // Check Parameters
        if (event == null) throw new IllegalArgumentException("MessageEvent cannot be null");
        if (command == null || command.isBlank()) throw new IllegalArgumentException("Command cannot be null or blank");
        if (message == null || message.isBlank()) throw new IllegalArgumentException("Message cannot be null or blank");

        // Normalize Message
        message = normalizeMessage(message);

        // Check Message
        if (message.isBlank() || message.length() > 500) return false;

        // Get Channel
        boolean oldEvent = System.currentTimeMillis() - event.getFiredAt().toEpochMilli() > 60000;
        TwitchUser channel = oldEvent ? userHandler.getTwitchUser(event.getChannel().getId()) : event.getChannel();

        // Check Channel
        if (!isChannelJoined(channel)) if (!joinChannel(channel)) return false;

        // Send Message
        boolean success = chat.sendMessage(channel.getUsername(), message);

        // Log Message
        if (success) {
            System.out.printf("%s <%s> #%s: %s%n", BOT, channel.getDisplayName(), botUser.getDisplayName(), message);
            database.getCommandManager().logResponse(event, command, message);
        }

        // Return
        return success;
    }

    // Association Getter
    public Server getServer() {
        return server;
    }

    // Database Getters
    public Database getDatabase() {
        return database;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public EventLogManager getEventLogManager() {
        return eventLogManager;
    }

    public BirthdayManager getBirthdayManager() {
        return birthdayManager;
    }

    public LurkManager getLurkManager() {
        return lurkManager;
    }

    public QuoteManager getQuoteManager() {
        return quoteManager;
    }

    // Configuration Getters
    public TwitchUser getBotUser() {
        return botUser;
    }

    public HashSet<TwitchUser> getOwners() {
        return owners;
    }

    public HashSet<String> getBotAliases() {
        return botAliases;
    }

    public ArrayList<String> getPrefixes() {
        return prefixes;
    }

    public String getPrefix() {
        return prefix;
    }

    // Attribute Getters
    public TwitchClient getClient() {
        return client;
    }

    public TwitchChat getChat() {
        return chat;
    }

    public TwitchHelix getHelix() {
        return helix;
    }

    public TwitchClientHelper getHelper() {
        return helper;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    // Helix-Handler Getters
    public HelixHandler getHelixHandler() {
        return helixHandler;
    }

    public UserHandler getUserHandler() {
        return userHandler;
    }

    public ChatHandler getChatHandler() {
        return chatHandler;
    }

    public RoleHandler getRoleHandler() {
        return roleHandler;
    }

    public StreamHandler getStreamHandler() {
        return streamHandler;
    }

    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }

    // Bot Handler Getters
    public EventHandler getEventHandler() {
        return eventHandler;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public BirthdayHandler getBirthdayHandler() {
        return birthdayHandler;
    }

    public LurkHandler getLurkHandler() {
        return lurkHandler;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    // Checks
    public boolean isBot(TwitchUser user) {

        // Check Parameters
        if (user == null) throw new IllegalArgumentException("TwitchUser cannot be null");

        // Check Bot
        return botUser.getId().equals(user.getId());
    }

    public boolean isOwner(TwitchUser user) {

        // Check Parameters
        if (user == null) throw new IllegalArgumentException("TwitchUser cannot be null");

        // Check Owners
        for (var owner : owners) if (owner.getId().equals(user.getId())) return true;
        return false;
    }

    public boolean isBroadcaster(TwitchUser user, TwitchUser channel) {

        // Check Parameters
        if (user == null) throw new IllegalArgumentException("TwitchUser cannot be null");
        if (channel == null) throw new IllegalArgumentException("Channel TwitchUser cannot be null");

        // Check Broadcaster
        return user.getId().equals(channel.getId());
    }

    public boolean isModerator(TwitchUser user, TwitchUser channel) {

        // Check Parameters
        if (user == null) throw new IllegalArgumentException("TwitchUser cannot be null");
        if (channel == null) throw new IllegalArgumentException("Channel TwitchUser cannot be null");

        // Check Moderator
        if (isOwner(user)) return true;
        if (isBot(user)) return true;
        return roleHandler.isModerator(user, channel);
    }

    public boolean isPermitted(TwitchUser user, TwitchUser channel) {

        // Check Parameters
        if (user == null) throw new IllegalArgumentException("TwitchUser cannot be null");
        if (channel == null) throw new IllegalArgumentException("Channel TwitchUser cannot be null");

        // Check Permission
        if (isBot(user)) return true;
        if (isOwner(user)) return true;
        if (isBroadcaster(user, channel)) return true;
        return isModerator(user, channel);
    }

    public boolean isEditor(TwitchUser user, TwitchUser channel) {

        // Check Parameters
        if (user == null) throw new IllegalArgumentException("TwitchUser cannot be null");
        if (channel == null) throw new IllegalArgumentException("Channel TwitchUser cannot be null");

        // Check Editor
        if (isOwner(user)) return true;
        if (isBot(user)) return true;
        return roleHandler.isEditor(user, channel);
    }

    public boolean isVIP(TwitchUser user, TwitchUser channel) {

        // Check Parameters
        if (user == null) throw new IllegalArgumentException("TwitchUser cannot be null");
        if (channel == null) throw new IllegalArgumentException("Channel TwitchUser cannot be null");

        // Check VIP
        return roleHandler.isVIP(user, channel);
    }

    public boolean isSubscriber(TwitchUser user, TwitchUser channel) {

        // Check Parameters
        if (user == null) throw new IllegalArgumentException("TwitchUser cannot be null");
        if (channel == null) throw new IllegalArgumentException("Channel TwitchUser cannot be null");

        // Check Subscriber
        return roleHandler.isSubscriber(user, channel);
    }

    public boolean isFollower(TwitchUser user, TwitchUser channel) {

        // Check Parameters
        if (user == null) throw new IllegalArgumentException("TwitchUser cannot be null");
        if (channel == null) throw new IllegalArgumentException("Channel TwitchUser cannot be null");

        // Check Follower
        return roleHandler.isFollower(user, channel);
    }

    public boolean isChannelJoined(TwitchUser channel) {

        // Check Parameters
        if (channel == null) throw new IllegalArgumentException("TwitchUser cannot be null");

        // Check Channel
        return chat.isChannelJoined(channel.getUsername());
    }
}