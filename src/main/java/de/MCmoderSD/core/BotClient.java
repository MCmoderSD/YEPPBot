package de.MCmoderSD.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelCheerEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscriptionMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelVipAddEvent;
import com.github.twitch4j.eventsub.events.ChannelVipRemoveEvent;
import com.github.twitch4j.eventsub.events.ChannelModeratorAddEvent;
import com.github.twitch4j.eventsub.events.ChannelModeratorRemoveEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscribeEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscriptionGiftEvent;
import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import com.github.twitch4j.helix.TwitchHelix;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.commands.Birthday;
import de.MCmoderSD.commands.Conversation;
import de.MCmoderSD.commands.Counter;
import de.MCmoderSD.commands.CustomCommand;
import de.MCmoderSD.commands.CustomTimers;
import de.MCmoderSD.commands.Fact;
import de.MCmoderSD.commands.Gif;
import de.MCmoderSD.commands.Help;
import de.MCmoderSD.commands.Horoscope;
import de.MCmoderSD.commands.Info;
import de.MCmoderSD.commands.Insult;
import de.MCmoderSD.commands.Join;
import de.MCmoderSD.commands.Joke;
import de.MCmoderSD.commands.Lurk;
import de.MCmoderSD.commands.Moderate;
import de.MCmoderSD.commands.Ping;
import de.MCmoderSD.commands.Play;
import de.MCmoderSD.commands.Prompt;
import de.MCmoderSD.commands.Say;
import de.MCmoderSD.commands.Status;
import de.MCmoderSD.commands.TTS;
import de.MCmoderSD.commands.Translate;
import de.MCmoderSD.commands.Weather;
import de.MCmoderSD.commands.Whitelist;
import de.MCmoderSD.commands.Wiki;
import de.MCmoderSD.main.Credentials;
import de.MCmoderSD.main.Main;
import de.MCmoderSD.objects.AudioFile;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.objects.TwitchRoleEvent;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.database.manager.LogManager;
import de.MCmoderSD.utilities.json.JsonUtility;
import de.MCmoderSD.utilities.other.Reader;
import de.MCmoderSD.utilities.server.AudioBroadcast;
import de.MCmoderSD.utilities.server.Server;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static de.MCmoderSD.main.Main.Argument.*;
import static de.MCmoderSD.utilities.other.Calculate.*;


@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class BotClient {

    public static final String PROVIDER = "twitch";

    // Associations
    private final Main main;
    private final MySQL mySQL;
    private final Frame frame;
    private final Server server;
    private final AudioBroadcast audioBroadcast;

    // Utilities
    private final JsonUtility jsonUtility;
    private final Reader reader;

    // Constants
    public static String botName;
    public static String[] botNames;
    public static String prefix;
    public static HashSet<String> admins;
    public static HelixHandler.Scope[] requiredScopes = {
            HelixHandler.Scope.USER_READ_EMAIL,
            HelixHandler.Scope.CHAT_READ,
            HelixHandler.Scope.BITS_READ,
            HelixHandler.Scope.CHANNEL_READ_EDITORS,
            HelixHandler.Scope.USER_READ_FOLLOWS,
            HelixHandler.Scope.MODERATOR_READ_FOLLOWERS,
            HelixHandler.Scope.MODERATION_READ,
            HelixHandler.Scope.CHANNEL_READ_VIPS,
            HelixHandler.Scope.CHANNEL_READ_SUBSCRIPTIONS,
            HelixHandler.Scope.CHANNEL_MANAGE_RAIDS,
    };

    // Credentials
    private final JsonNode botConfig;
    private final Integer botId;
    private final String botToken;
    private final String clientId;
    private final String clientSecret;
    private final OAuth2Credential defaultAuthToken;
    private final CredentialManager credentialManager;

    // Client
    private final TwitchClient client;
    private final TwitchChat chat;
    private final TwitchHelix helix;
    private final EventManager eventManager;

    // Handler
    private final HelixHandler helixHandler;
    private final MessageHandler messageHandler;

    // Constructor
    public BotClient(Main main) {

        // Get Associations
        Credentials credentials = main.getCredentials();
        mySQL = main.getMySQL();
        frame = main.getFrame();
        this.main = main;

        // Get Utilities
        jsonUtility = main.getJsonUtility();
        reader = main.getReader();

        // Init Credentials
        botConfig = credentials.getBotConfig();
        botId = botConfig.get("botId").asInt();
        botToken = botConfig.get("botToken").asText();
        clientId = botConfig.get("clientId").asText();
        clientSecret = botConfig.get("clientSecret").asText();

        // Init Credential Manager
        defaultAuthToken = new OAuth2Credential(PROVIDER, botToken);
        credentialManager = CredentialManagerBuilder.builder().build();

        // Init Bot
        botNames = botConfig.get("botName").asText().toLowerCase().split(", ");
        botName = botNames[0];
        prefix = botConfig.get("prefix").asText();
        admins = new HashSet<>(Arrays.asList(botConfig.get("admins").asText().toLowerCase().split("; ")));

        // Init Server
        JsonNode httpsServerConfig = credentials.getHttpsServerConfig();
        if (!hasArg(DEV) && httpsServerConfig.get("hostname").asText().contains(".")) server = new Server(this, httpsServerConfig);
        else {
            String hostname = hasArg(HOST) ? Main.arguments[0] : httpsServerConfig.get("hostname").asText();
            int port = hasArg(PORT) ? Integer.parseInt(Main.arguments[1]) : httpsServerConfig.get("port").asInt();
            server = new Server(this, hostname, port, httpsServerConfig.get("keystore").asText(), botConfig);
        }

        // Init Client
        client = TwitchClientBuilder.builder()
                .withBotOwnerId(botId.toString())
                .withClientId(clientId)
                .withClientSecret(clientSecret)
                .withCredentialManager(credentialManager)
                .withDefaultAuthToken(defaultAuthToken)
                .withChatAccount(defaultAuthToken)
                .withChatCommandsViaHelix(true)
                .withEnableHelix(true)
                .withEnableChat(true)
                .build();

        // Init Modules
        chat = client.getChat();
        helix = client.getHelix();
        eventManager = client.getEventManager();

        // Init Helix Handler
        helixHandler = new HelixHandler(this, mySQL, server);

        // Init Audio Broadcast
        audioBroadcast = new AudioBroadcast(server);

        // Join Channels
        Set<String> channelList = new HashSet<>();
        if (credentials.validateChannelList()) channelList.addAll(credentials.getChannelList());
        if (!hasArg(DEV)) channelList.addAll(mySQL.getChannelManager().getActiveChannels());
        channelList = channelList.stream().map(String::toLowerCase).collect(Collectors.toSet());
        channelList.remove(botName);
        joinChannel(channelList);

        // Event Handler
        messageHandler = new MessageHandler(this, mySQL, main.getFrame());

        // Message Events
        eventManager.onEvent(ChannelMessageEvent.class, event -> messageHandler.handleMessage(new TwitchMessageEvent(event)));              // chat:read
        eventManager.onEvent(ChannelCheerEvent.class, event -> messageHandler.handleMessage(new TwitchMessageEvent(event)));                // bits:read
        eventManager.onEvent(ChannelSubscriptionMessageEvent.class, event -> messageHandler.handleMessage(new TwitchMessageEvent(event)));  // channel_subscriptions:read

        // Initialize LogManager
        LogManager logManager = mySQL.getLogManager();

        // Role Events
        eventManager.onEvent(ChannelVipAddEvent.class, event -> logManager.logRole(new TwitchRoleEvent(event)));            // channel_vips:read
        eventManager.onEvent(ChannelVipRemoveEvent.class, event -> logManager.logRole(new TwitchRoleEvent(event)));         // channel_vips:read
        eventManager.onEvent(ChannelModeratorAddEvent.class, event -> logManager.logRole(new TwitchRoleEvent(event)));      // moderation:read
        eventManager.onEvent(ChannelModeratorRemoveEvent.class, event -> logManager.logRole(new TwitchRoleEvent(event)));   // moderation:read

        // Loyalty Events
        eventManager.onEvent(ChannelFollowEvent.class, logManager::logLoyalty);                                             // user:read:follows
        eventManager.onEvent(ChannelSubscribeEvent.class, logManager::logLoyalty);                                          // channel_subscriptions:read
        eventManager.onEvent(ChannelSubscriptionGiftEvent.class, logManager::logLoyalty);                                   // channel_subscriptions:read

        // Raid Events
        eventManager.onEvent(ChannelRaidEvent.class, logManager::logRaid);                                                  // channel:mange:raids

        // Validate Configs
        boolean astrology = credentials.hasAstrology();
        boolean weather = credentials.hasOpenWeatherMap();
        boolean giphy = credentials.hasGiphy();
        boolean openAI = credentials.validateOpenAIConfig();
        boolean openAIChat = credentials.validateOpenAIChatConfig();
        boolean openAIImage = credentials.validateOpenAIImageConfig();
        boolean openAITTS = credentials.validateOpenAITTSConfig();

        // Initialize Commands
        new Birthday(this, messageHandler, mySQL);
        if (openAIChat) new Conversation(this, messageHandler, main.getOpenAI());
        new Counter(this, messageHandler, mySQL);
        new CustomCommand(this, messageHandler, mySQL);
        new CustomTimers(this, messageHandler, mySQL);
        new Fact(this, messageHandler, mySQL);
        if (giphy) new Gif(this, messageHandler, credentials);
        new Help(this, messageHandler, mySQL);
        if (openAIChat) new Horoscope(this, messageHandler, mySQL, main.getOpenAI());
        new Info(this, messageHandler, helixHandler);
        new Insult(this, messageHandler, mySQL);
        new Join(this, messageHandler);
        new Joke(this, messageHandler, mySQL);
        new Lurk(this, messageHandler, mySQL);
        new Moderate(this, messageHandler, mySQL, helixHandler);
        new Ping(this, messageHandler);
        new Play(this, messageHandler);
        if (openAIChat) new Prompt(this, messageHandler, main.getOpenAI());
        new Say(this, messageHandler);
        new Status(this, messageHandler);
        if (openAIChat) new Translate(this, messageHandler, main.getOpenAI());
        if (openAITTS) new TTS(this, messageHandler, mySQL, main.getOpenAI());
        if (openAIChat && weather) new Weather(this, messageHandler, main.getOpenAI(), credentials);
        new Whitelist(this, messageHandler, mySQL);
        if (openAIChat) new Wiki(this, messageHandler, main.getOpenAI());

        // Show UI
        if (hasArg(CLI)) return;
        frame.setVisible(true);
        frame.requestFocusInWindow();
    }


    // Bot Controls

    // Credential
    public void addCredential(String accessToken) {
        credentialManager.addCredential(PROVIDER, new OAuth2Credential(PROVIDER, accessToken));
    }

    // Write
    public void write(String channel, String message) {

        // Check Message
        if (message.isEmpty() || message.isBlank()) return;

        // Update Frame
        if (!hasArg(CLI)) frame.log(USER, channel, botName, message);

        // Log
        mySQL.getLogManager().logResponse(channel, botName, message, helixHandler);
        System.out.printf("%s %s <%s> %s: %s%s", logTimestamp(), USER, channel, botName, message, BREAK);

        // Send Message
        chat.sendMessage(channel, message);
    }

    // Respond
    public void respond(TwitchMessageEvent event, String command, String message) {

        // Variables
        var channel = event.getChannel();

        // Update Frame
        if (!(message.isEmpty() || message.isBlank()) && !hasArg(CLI)) frame.log(RESPONSE, channel, botName, message);

        // Log
        mySQL.getLogManager().logResponse(event, command, message);
        System.out.printf("%s%s %s <%s> Executed: %s%s%s", BOLD, logTimestamp(), COMMAND, channel, command + ": " + event.getMessage(), BREAK, UNBOLD);
        if (!(message.isEmpty() || message.isBlank())) System.out.printf("%s%s %s <%s> %s: %s%s%s", BOLD, logTimestamp(), RESPONSE, channel, botName, message, UNBOLD, BREAK);

        // Send Messag
        if (!(message.isEmpty() || message.isBlank())) chat.sendMessage(channel, message);
    }

    // Send Audio
    public void sendAudio(TwitchMessageEvent event, AudioFile audioFile) {

        // Log
        mySQL.getLogManager().logTTS(event, audioFile);

        // Play Audio
        audioBroadcast.play(event.getChannel(), audioFile);

        // Update Frame
        if (!hasArg(CLI)) audioFile.play();
    }

    // Join Channel
    public void joinChannel(String channel) {

        // Format Channel
        channel = channel.toLowerCase();

        // Check Channel
        if (channel.isEmpty() || channel.isBlank()) return;
        if (channel.length() < 3 || channel.length() > 25) return;
        if (channel.contains(" ") || chat.isChannelJoined(channel) || botName.equals(channel)) return;

        // Log
        System.out.printf("%s%s %s Joined Channel: %s%s%s", BOLD, logTimestamp(), SYSTEM, channel.toLowerCase(), BREAK, UNBOLD);

        // Join Channel
        chat.joinChannel(channel);

        // Register Broadcast
        audioBroadcast.registerBroadcast(channel);
    }

    // Bulk Join
    public void joinChannel(Set<String> channels) {
        new Thread(() -> {
            try {
                for (String channel : channels) {
                    joinChannel(channel);
                    Thread.sleep(250); // Prevent rate limit
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    // Leave Channel
    public void leaveChannel(String channel) {

        // Format Channel
        channel = channel.toLowerCase();

        // Check Channel
        if (channel.isEmpty() || channel.isBlank()) return;
        if (channel.length() < 3 || channel.length() > 25) return;
        if (channel.contains(" ") || !chat.isChannelJoined(channel) || botName.equals(channel)) return;

        // Leave Channel
        if (chat.leaveChannel(channel)) System.out.printf("%s%s %s Left Channel: %s%s%s", BOLD, logTimestamp(), SYSTEM, channel.toLowerCase(), BREAK, UNBOLD);
        else System.out.printf("%s%s %s Failed to Leave Channel: %s%s%s", BOLD, logTimestamp(), SYSTEM, channel.toLowerCase(), BREAK, UNBOLD);

        // Unregister Broadcast
        if (audioBroadcast.unregisterBroadcast(channel)) System.out.printf("%s%s %s Unregistered Broadcast: %s%s%s", BOLD, logTimestamp(), SYSTEM, channel.toLowerCase(), BREAK, UNBOLD);
        else System.out.printf("%s%s %s Failed to Unregister Broadcast: %s%s%s", BOLD, logTimestamp(), SYSTEM, channel.toLowerCase(), BREAK, UNBOLD);
    }

    // Bulk Leave
    public void leaveChannel(Set<String> channels) {
        new Thread(() -> {
            try {
                for (String channel : channels) {
                    leaveChannel(channel);
                    Thread.sleep(250); // Prevent rate limit
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    // Disconnect
    public void disconnectChat() {
        chat.disconnect();
    }

    // Close Chat
    public void closeChat() {
        chat.close();
    }

    // Close
    public void close() {
        client.close();
    }

    // Getter

    // Checker
    public boolean hasArg(Main.Argument arg) {
        return main.hasArg(arg);
    }

    public boolean isAdmin(TwitchMessageEvent event) {
        return admins.contains(event.getUser());
    }

    public boolean isPermitted(TwitchMessageEvent event) {
        return isModerator(event) || isBroadcaster(event);
    }

    public boolean isBroadcaster(TwitchMessageEvent event) {
        return event.getChannelId() == event.getUserId();
    }

    public boolean isEditor(TwitchMessageEvent event) {
        HashSet<Integer> ids = new HashSet<>();
        helixHandler.getEditors(event.getChannelId()).forEach(twitchUser -> ids.add(twitchUser.getId()) );
        return ids.contains(event.getUserId());
    }

    public boolean isModerator(TwitchMessageEvent event) {
        HashSet<Integer> ids = new HashSet<>();
        helixHandler.getModerators(event.getChannelId()).forEach(twitchUser -> ids.add(twitchUser.getId()) );
        return ids.contains(event.getUserId());
    }

    public boolean isVIP(TwitchMessageEvent event) {
        HashSet<Integer> ids = new HashSet<>();
        helixHandler.getVIPs(event.getChannelId()).forEach(twitchUser -> ids.add(twitchUser.getId()) );
        return ids.contains(event.getUserId());
    }

    public boolean isFollowing(TwitchMessageEvent event) {
        HashSet<Integer> ids = new HashSet<>();
        helixHandler.getFollowers(event.getChannelId()).forEach(twitchUser -> ids.add(twitchUser.getId()) );
        return ids.contains(event.getUserId());
    }

    public boolean isInChannel(String channel) {
        return chat.isChannelJoined(channel);
    }

    // Attribute Getter
    public String getProvider() {
        return PROVIDER;
    }

    public String getBotName() {
        return botName;
    }

    public String[] getBotNames() {
        return botNames;
    }

    public String getPrefix() {
        return prefix;
    }

    public HashSet<String> getAdmins() {
        return admins;
    }

    public HelixHandler.Scope[] getRequiredScopes() {
        return requiredScopes;
    }

    public JsonNode getConfig() {
        return botConfig;
    }

    public Integer getBotId() {
        return botId;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public OAuth2Credential getDefaultAuthToken() {
        return defaultAuthToken;
    }

    public CredentialManager getCredentialManager() {
        return credentialManager;
    }

    // Module Getter
    public TwitchClient getClient() {
        return client;
    }

    public TwitchChat getChat() {
        return chat;
    }

    public TwitchHelix getHelix() {
        return helix;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public HelixHandler getHelixHandler() {
        return helixHandler;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    // Utility Getter
    public JsonUtility getJsonUtility() {
        return jsonUtility;
    }

    public Reader getReader() {
        return reader;
    }
}