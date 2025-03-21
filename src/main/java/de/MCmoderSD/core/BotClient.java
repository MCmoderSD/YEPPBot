package de.MCmoderSD.core;

import com.fasterxml.jackson.databind.JsonNode;
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

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.enums.Scope;
import de.MCmoderSD.main.Credentials;
import de.MCmoderSD.main.Main;
import de.MCmoderSD.main.Terminal;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.objects.TwitchUser;
import de.MCmoderSD.openai.core.OpenAI;
import de.MCmoderSD.server.Server;
import de.MCmoderSD.server.modules.AudioBroadcast;
import de.MCmoderSD.utilities.database.SQL;
import de.MCmoderSD.JavaAudioLibrary.AudioFile;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

import static de.MCmoderSD.enums.Argument.*;
import static de.MCmoderSD.utilities.other.Format.*;

@SuppressWarnings({"unused", "SameReturnValue"})
public class BotClient {

    public static final String PROVIDER = "twitch";
    public static final long RATE_LIMIT = 600L;
    public static final Scope[] REQUIRED_SCOPES = {
            Scope.CHANNEL_BOT,
            Scope.MODERATION_READ,
            Scope.CHANNEL_READ_VIPS,
            Scope.MODERATOR_READ_FOLLOWERS,
            Scope.BITS_READ,
            Scope.CHANNEL_READ_SUBSCRIPTIONS,
            Scope.ANALYTICS_READ_EXTENSIONS,
            Scope.ANALYTICS_READ_GAMES,
            Scope.MODERATOR_MANAGE_SHOUTOUTS
    };

    // Associations
    private final SQL sql;
    private final Frame frame;
    private final Server server;
    private final AudioBroadcast audioBroadcast;

    // Constants
    public static String botName;
    public static String[] botNames;
    public static String prefix;
    public static String [] prefixes;
    public static HashSet<String> admins;

    // Credentials
    private final JsonNode botConfig;
    private final Integer botId;
    private final String botToken;
    private final String clientId;
    private final String clientSecret;
    private final OAuth2Credential defaultAuthToken;
    private final CredentialManager credentialManager;

    // Client Modules
    private final TwitchClient client;
    private final TwitchClientHelper helper;
    private final TwitchChat chat;
    private final TwitchHelix helix;
    private final EventManager eventManager;

    // Handler
    private final EventHandler eventHandler;
    private final HelixHandler helixHandler;
    private final MessageHandler messageHandler;

    // Attributes
    private boolean cli;
    private boolean log;

    // Constructor
    public BotClient(Credentials credentials, SQL sql, @Nullable Frame frame, @Nullable OpenAI openAI) {

        // Init Associations
        this.sql = sql;
        this.frame = frame;

        // Init Bot Credentials
        botConfig = credentials.getBotConfig();
        botId = botConfig.get("botId").asInt();
        botToken = botConfig.get("botToken").asText();
        clientId = botConfig.get("clientId").asText();
        clientSecret = botConfig.get("clientSecret").asText();

        // Init Credential Manager
        defaultAuthToken = new OAuth2Credential(PROVIDER, botToken);
        credentialManager = CredentialManagerBuilder.builder().build();

        // Init Bot Settings
        botNames = botConfig.get("botName").asText().toLowerCase().split(", ");
        botName = botNames[0];
        prefixes = botConfig.get("prefix").asText().split(SPACE);
        prefix = prefixes[0];
        admins = new HashSet<>(Arrays.asList(botConfig.get("admins").asText().toLowerCase().split("; ")));

        // Init Attributes
        cli = Main.terminal.hasArg(CLI) || Main.terminal.hasArg(CONTAINER);
        log = !Main.terminal.hasArg(NO_LOG);

        // Init Server
        JsonNode serverConfig = credentials.getServerConfig();
        boolean ssl = serverConfig.has("SSL");
        String hostname = serverConfig.get("hostname").asText();
        var port = Main.terminal.hasArg(CONTAINER) ? 443 : serverConfig.get("port").asInt();

        try {
            if (ssl) {  // SSL
                JsonNode sslConfig = serverConfig.get("SSL");
                String privkey = sslConfig.get("privkey").asText();
                String fullchain = sslConfig.get("fullchain").asText();
                String proxy = serverConfig.has("proxy") ? serverConfig.get("proxy").asText() : null;
                server = new Server(hostname, port, proxy, privkey, fullchain, serverConfig.get("host").asBoolean());
            } else {    // JKS
                hostname = Main.terminal.hasArg(HOST) ? Main.terminal.getArgs()[0] : hostname;
                port = Main.terminal.hasArg(PORT) && !Main.terminal.hasArg(CONTAINER) ? Integer.parseInt(Main.terminal.getArgs()[1]) : port;
                String proxy = serverConfig.has("proxy") ? serverConfig.get("proxy").asText() : null;
                server = new Server(hostname, port, proxy, serverConfig.get("JKS"), serverConfig.get("host").asBoolean());
            }
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException | KeyManagementException | CertificateException | InvalidKeySpecException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Start Server
        server.start();

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
        helper = client.getClientHelper();
        helix = client.getHelix();
        eventManager = client.getEventManager();

        // Init Helix Handler
        helixHandler = new HelixHandler(this, sql, server);

        // Init Audio Broadcast
        audioBroadcast = new AudioBroadcast(server);
        audioBroadcast.registerBroadcast(botName);

        // Init Handlers
        messageHandler = new MessageHandler(this, sql);
        eventHandler = new EventHandler(this, frame, sql, eventManager, messageHandler, helixHandler);

        // Check Modules
        boolean astrology = credentials.hasAstrology();
        boolean weather = credentials.hasOpenWeatherMap();
        boolean giphy = credentials.hasGiphy();
        boolean riot = credentials.hasRiot();
        boolean openAIChat = credentials.validateOpenAIConfig();

        // Loading Standard Commands
        new Join(this, messageHandler);
        new Ping(this, messageHandler);
        new Play(this, messageHandler);
        new Say(this, messageHandler);
        new Status(this, messageHandler);

        // Loading Database Commands
        new Counter(this, messageHandler, sql);
        new CustomCommand(this, messageHandler, sql);
        new Fact(this, messageHandler, sql);
        new Help(this, messageHandler, sql);
        new Insult(this, messageHandler, sql);
        new Joke(this, messageHandler, sql);
        new Lurk(this, messageHandler, sql);
        new Quote(this, messageHandler, sql);

        // Loading Twitch API Commands
        new Birthday(this, messageHandler, helixHandler, sql);
        new DickDestroyDecember(this, messageHandler, helixHandler, sql);
        new Info(this, messageHandler, helixHandler);
        new Moderate(this, messageHandler, helixHandler, sql);
        new NoNutNovember(this, messageHandler, helixHandler, sql);
        new Shoutout(this, messageHandler, eventHandler, helixHandler, sql);

        // Loading OpenAI Chat Commands
        if (openAIChat) {
            new Conversation(this, messageHandler, openAI, credentials.getOpenAIConfig());
            new Match(this, messageHandler, helixHandler, sql, openAI);
            new Translate(this, messageHandler, openAI);
            new Prompt(this, messageHandler, openAI);
            new Wiki(this, messageHandler, openAI);
        }

        // API & OpenAI Commands
        if (giphy || astrology || weather || riot) {
            JsonNode apiConfig = credentials.getAPIConfig();
            if (giphy) new Gif(this, messageHandler, apiConfig);
            if (openAIChat && astrology) new Horoscope(this, messageHandler, helixHandler, sql, openAI, apiConfig);
            if (openAIChat && weather) new Weather(this, messageHandler, openAI, apiConfig);
            if (riot) new Riot(this, messageHandler, apiConfig, credentials.SQLConfig());
        }

        // Show UI
        setUI(!cli);

        // Print Startup Complete
        System.out.printf("%sStart-Up Complete! Took: %dms%s%s%s", BOLD, (System.nanoTime() - Terminal.startTime) / 1_000_000, UNBOLD, BREAK, BREAK);

        // Join Channels
        HashSet<String> channelList = new HashSet<>();
        if (credentials.validateChannelList()) channelList.addAll(credentials.getChannelList());
        if (!Main.terminal.hasArg(DEV)) channelList.addAll(sql.getChannelManager().getActiveChannels());
        channelList = (HashSet<String>) channelList.stream().map(String::toLowerCase).collect(Collectors.toSet());
        channelList.remove(botName);
        joinChannel(channelList);
    }

    // UI
    private void setUI(boolean visible) {
        if (frame == null || !visible) return;
        frame.setVisible(true);
        frame.requestFocusInWindow();
    }

    // Credential
    public void addCredential(String accessToken) {
        credentialManager.addCredential(PROVIDER, new OAuth2Credential(PROVIDER, accessToken));
    }

    // Write
    public void write(String channel, String message) {

        // Normalize Message
        message = normalizeMessage(message);

        // Variables
        boolean tooLong = message.length() > 500;
        boolean valid = !(message.isBlank() || tooLong);

        // Update Frame
        if (!cli) {
            if (tooLong) frame.showMessage("The message was too long to send.", "Message Error");
            else if (valid) frame.log(channel, botName, message);
        }

        // Check
        if (!valid) return;

        // Log
        if (log) sql.getLogManager().logResponse(channel, botName, message, helixHandler);
        System.out.printf("%s %s <%s> %s: %s%s", getFormattedTimestamp(), USER, channel, botName, message, BREAK);

        // Send Message
        chat.sendMessage(channel, message);
    }

    // Respond
    public void respond(TwitchMessageEvent event, String command, String message) {

        // Normalize Message
        message = normalizeMessage(message);

        // Variables
        var channel = event.getChannel();
        boolean tooLong = message.length() > 500;
        boolean valid = !(message.isBlank() || tooLong);

        // Update Frame
        if (valid && !cli) frame.log(channel, botName, message);

        // Log
        if (valid && log) sql.getLogManager().logResponse(event, command, message);
        System.out.printf("%s%s %s <%s> Executed: %s%s%s", BOLD, getFormattedTimestamp(), COMMAND, channel, command + ": " + event.getMessage(), UNBOLD, BREAK);
        if (valid) System.out.printf("%s%s %s <%s> %s: %s%s%s", BOLD, getFormattedTimestamp(), RESPONSE, channel, botName, message, UNBOLD, BREAK);

        // Send Message
        if (valid) chat.sendMessage(channel, message);

        // Error Message
        if (tooLong) respond(event, command, "The response was too long to send. YEPP");
    }

    // Send Audio
    public void sendAudio(TwitchMessageEvent event, AudioFile audioFile) {

        // Log
        sql.getLogManager().logTTS(event, audioFile);

        // Play Audio
        audioBroadcast.play(event.getChannel(), audioFile.getAudioData());

        // Update Frame
        if (!cli) audioFile.play();
    }

    // Join Channel
    public void joinChannel(String channel) {

        // Format Channel
        channel = channel.toLowerCase();

        // Check Channel
        if (channel.isBlank()) return;
        if (channel.length() < 3 || channel.length() > 25) return;
        if (channel.contains(SPACE) || chat.isChannelJoined(channel) || botName.equals(channel)) return;

        // Get Channel ID
        var id = helixHandler.getUser(channel).getId();

        // Log
        System.out.printf("%s%s %s Joined Channel: %s%s%s", BOLD, getFormattedTimestamp(), SYSTEM, channel.toLowerCase(), UNBOLD, BREAK);

        // Join Channel
        chat.joinChannel(channel);

        // Enable Additional Event Listeners
        helper.enableStreamEventListener(channel);
        if (helixHandler.checkScope(id, Scope.MODERATOR_READ_FOLLOWERS)) helper.enableFollowEventListener(channel);    // moderator:read:followers
        helper.enableClipEventListener(channel);

        // Register Broadcast
        audioBroadcast.registerBroadcast(channel);
    }

    // Bulk Join
    public void joinChannel(HashSet<String> channels) {
        new Thread(() -> {
            try {
                for (String channel : channels) {
                    joinChannel(channel);
                    Thread.sleep(RATE_LIMIT); // Prevent rate limit
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
        if (channel.isBlank()) return;
        if (channel.length() < 3 || channel.length() > 25) return;
        if (channel.contains(SPACE) || !chat.isChannelJoined(channel) || botName.equals(channel)) return;

        // Leave Channel
        if (chat.leaveChannel(channel)) System.out.printf("%s%s %s Left Channel: %s%s%s", BOLD, getFormattedTimestamp(), SYSTEM, channel.toLowerCase(), UNBOLD, BREAK);
        else System.out.printf("%s%s %s Failed to Leave Channel: %s%s%s", BOLD, getFormattedTimestamp(), SYSTEM, channel.toLowerCase(), UNBOLD, BREAK);

        // Unregister Broadcast
        if (audioBroadcast.unregisterBroadcast(channel)) System.out.printf("%s%s %s Unregistered Broadcast: %s%s%s", BOLD, getFormattedTimestamp(), SYSTEM, channel.toLowerCase(), UNBOLD, BREAK);
        else System.out.printf("%s%s %s Failed to Unregister Broadcast: %s%s%s", BOLD, getFormattedTimestamp(), SYSTEM, channel.toLowerCase(), UNBOLD, BREAK);
    }

    // Bulk Leave
    public void leaveChannel(HashSet<String> channels) {
        new Thread(() -> {
            try {
                for (String channel : channels) {
                    leaveChannel(channel);
                    Thread.sleep(RATE_LIMIT); // Prevent rate limit
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }


    // Controls
    public void connectChat() {
        chat.connect();
    }

    public void reconnectChat() {
        chat.reconnect();
    }

    public void disconnectChat() {
        chat.disconnect();
    }

    public void closeChat() {
        chat.close();
    }

    public void close() {
        client.close();
    }

    public void setCli(boolean cli) {
        this.cli = cli;
    }

    public void setLog(boolean log) {
        this.log = log;
    }

    // Checker
    public boolean isAdmin(TwitchMessageEvent event) {
        return admins.contains(event.getUser());
    }

    public boolean isPermitted(TwitchMessageEvent event) {
        return isModerator(event) || isBroadcaster(event);
    }

    public boolean isBroadcaster(TwitchMessageEvent event) {
        return Objects.equals(event.getChannelId(), event.getUserId());
    }

    public boolean isModerator(TwitchMessageEvent event) {
        return helixHandler.isModerator(event.getChannelId(), event.getUserId());
    }

    public boolean isEditor(TwitchMessageEvent event) {
        HashSet<Integer> ids = new HashSet<>();
        HashSet<TwitchUser> editors = helixHandler.getEditors(event.getChannelId());
        if (editors.isEmpty()) return false;
        for (TwitchUser editor : editors) ids.add(editor.getId());
        return ids.contains(event.getUserId());
    }

    public boolean isVIP(TwitchMessageEvent event) {
        return helixHandler.isVIP(event.getChannelId(), event.getUserId());
    }

    public boolean isFollowing(TwitchMessageEvent event) {
        return helixHandler.isFollower(event.getChannelId(), event.getUserId());
    }

    public boolean isInChat(String channel) {
        return chat.isChannelJoined(channel);
    }

    public boolean isLog() {
        return log;
    }

    public boolean isCli() {
        return cli || Main.terminal.hasArg(CONTAINER);
    }

    // Getter
    public HashSet<String> getChannels() {
        return (HashSet<String>) chat.getChannels();
    }

    public long getLatency() {
        return chat.getLatency();
    }

    // Constants Getter
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

    public String[] getPrefixes() {
        return prefixes;
    }

    public HashSet<String> getAdmins() {
        return admins;
    }

    public Scope[] getRequiredScopes() {
        return REQUIRED_SCOPES;
    }

    // Credentials Getter
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

    public TwitchClientHelper getHelper() {
        return helper;
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

    // Handler Getter
    public EventHandler getEventHandler() {
        return eventHandler;
    }

    public HelixHandler getHelixHandler() {
        return helixHandler;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    // Association Getter
    public SQL getSQL() {
        return sql;
    }

    public Frame getFrame() {
        return frame;
    }

    public Server getServer() {
        return server;
    }

    public AudioBroadcast getAudioBroadcast() {
        return audioBroadcast;
    }
}