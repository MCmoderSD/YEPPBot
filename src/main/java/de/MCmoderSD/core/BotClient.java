package de.MCmoderSD.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.eventsub.events.*;

import com.github.twitch4j.helix.TwitchHelix;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.commands.*;
import de.MCmoderSD.main.Credentials;
import de.MCmoderSD.main.Main;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.json.JsonUtility;
import de.MCmoderSD.utilities.other.Reader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static de.MCmoderSD.utilities.other.Calculate.*;

@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
public class BotClient {

    // Associations
    private final Main main;
    private final MySQL mySQL;
    private final Frame frame;

    // Utilities
    private final JsonUtility jsonUtility;
    private final Reader reader;

    // Constants
    public static String botName;
    public static String prefix;
    public static ArrayList<String> admins;

    // Attributes
    private final TwitchClient client;
    private final TwitchChat chat;
    private final TwitchHelix helix;
    private final EventManager eventManager;

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

        // Load Bot Config
        JsonNode botConfig = credentials.getBotConfig();
        botName = botConfig.get("botName").asText().toLowerCase();
        prefix = botConfig.get("prefix").asText();
        admins = new ArrayList<>(Arrays.asList(botConfig.get("admins").asText().toLowerCase().split("; ")));

        // Init Bot Credential
        OAuth2Credential botCredential = new OAuth2Credential("twitch", botConfig.get("botToken").asText());

        // Init Client
        client = TwitchClientBuilder.builder()
                .withDefaultAuthToken(botCredential)
                .withChatAccount(botCredential)
                .withEnableChat(true)
                .withEnableHelix(true)
                .build();

        // Init Modules
        chat = client.getChat();
        helix = client.getHelix();
        eventManager = client.getEventManager();

        // Join Channels
        ArrayList<String> channelList = new ArrayList<>(Collections.singleton(botName));
        if (credentials.validateChannelList()) channelList.addAll(credentials.getChannelList());
        if (!hasArg("dev")) channelList.addAll(mySQL.getChannelManager().getActiveChannels());
        joinChannel(channelList);

        // Event Handler
        MessageHandler messageHandler = new MessageHandler(this, mySQL, main.getFrame());

        // Message Events
        eventManager.onEvent(ChannelMessageEvent.class, event -> messageHandler.handleMessage(new TwitchMessageEvent(event)));
        eventManager.onEvent(ChannelCheerEvent.class, event -> messageHandler.handleMessage(new TwitchMessageEvent(event)));
        eventManager.onEvent(ChannelSubscriptionMessageEvent.class, event -> messageHandler.handleMessage(new TwitchMessageEvent(event)));

        // Validate Configs
        boolean openAI = credentials.validateOpenAIConfig();
        boolean weather = credentials.validateWeatherConfig();
        boolean giphy = credentials.validateGiphyConfig();

        // Initialize Commands
        new Join(this, messageHandler);
        new Ping(this, messageHandler);
        new Play(this, messageHandler);
        if (openAI) new Prompt(this, messageHandler, main.getOpenAI());
        new Say(this, messageHandler);
        new Status(this, messageHandler);
        if (openAI) new Translate(this, messageHandler, main.getOpenAI());
        if (openAI && weather) new Weather(this, messageHandler, main.getOpenAI(), main.getCredentials());
        if (openAI) new Wiki(this, messageHandler, main.getOpenAI());
    }

    // Methods
    private boolean checkModerator(TwitchMessageEvent event) {
        // ToDo Check if user is moderator
        return false;
    }

    // Setter
    public void write(String channel, String message) {

        // Update Frame
        if (!hasArg("cli")) frame.log(USER, channel, botName, message);

        // Log
        mySQL.getLogManager().logResponse(channel, botName, message);
        System.out.printf("%s %s <%s> %s: %s%s", logTimestamp(), USER, channel, botName, message, BREAK);

        // Send Message
        chat.sendMessage(channel, message);
    }

    public void respond(TwitchMessageEvent event, String command, String message) {

        // Variables
        var channel = event.getChannel();

        // Update Frame
        if (!hasArg("cli")) frame.log(RESPONSE, channel, botName, message);

        // Log
        mySQL.getLogManager().logResponse(event, command, message);
        System.out.printf("%s%s %s <%s> Executed: %s%s%s", BOLD, logTimestamp(), COMMAND, channel, command + ": " + event.getMessage(), BREAK, UNBOLD);
        System.out.printf("%s%s %s <%s> %s: %s%s%s", BOLD, logTimestamp(), RESPONSE, channel, botName, message, UNBOLD, BREAK);

        // Send Message
        chat.sendMessage(channel, message);
    }

    public void joinChannel(String channel) {
        if (chat.isChannelJoined(channel)) return;
        System.out.printf("%s%s %s Joined Channel: %s%s%s", BOLD, logTimestamp(), SYSTEM, channel.toLowerCase(), BREAK, UNBOLD);
        chat.joinChannel(channel);
    }

    public void joinChannel(ArrayList<String> channels) {
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

    public void leaveChannel(String channel) {
        if (!chat.isChannelJoined(channel)) return;
        System.out.printf("%s%s %s Leave Channel: %s%s%s", BOLD, logTimestamp(), SYSTEM, channel.toLowerCase(), BREAK, UNBOLD);
        chat.leaveChannel(channel);
    }

    public void leaveChannel(ArrayList<String> channels) {
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

    public void close() {
        client.close();
    }


    // Getter
    public String getBotName() {
        return botName;
    }

    public String getPrefix() {
        return prefix;
    }

    public Set<String> getChannels() {
        return chat.getChannels();
    }

    public boolean hasArg(String arg) {
        return main.hasArg(arg);
    }

    public boolean isAdmin(String user) {
        return admins.contains(user);
    }

    public boolean isBroadcaster(TwitchMessageEvent event) {
        return event.getChannelId() == event.getUserId();
    }

    public boolean isModerator(TwitchMessageEvent event) {
        return checkModerator(event);
    }

    public boolean isInChannel(String channel) {
        return chat.isChannelJoined(channel);
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

    // Utility Getter
    public JsonUtility getJsonUtility() {
        return jsonUtility;
    }

    public Reader getReader() {
        return reader;
    }
}