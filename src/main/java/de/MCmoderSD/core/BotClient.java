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

import static de.MCmoderSD.utilities.other.Calculate.*;

@SuppressWarnings({"unused"})
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
        admins = new ArrayList<>(Arrays.asList(botConfig.get("admins").asText().split("; ")));

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
        ArrayList<String> channelList = new ArrayList<>();
        if (getArg("dev")) channelList.addAll(credentials.getDevList());
        else {
            if (credentials.validateChannelList()) channelList.addAll(credentials.getChannelList());
            channelList.addAll(mySQL.getActiveChannels());
        }
        joinChannel(channelList);

        // Event Handler
        MessageHandler messageHandler = new MessageHandler(this, mySQL, main.getFrame());

        // Message Events
        eventManager.onEvent(ChannelMessageEvent.class, event -> messageHandler.handleMessage(new TwitchMessageEvent(event)));
        eventManager.onEvent(ChannelCheerEvent.class, event -> messageHandler.handleMessage(new TwitchMessageEvent(event)));
        eventManager.onEvent(ChannelSubscriptionMessageEvent.class, event -> messageHandler.handleMessage(new TwitchMessageEvent(event)));

        // Initialize Commands
        new Join(this, messageHandler);
        new Ping(this, messageHandler);
    }

    // Setter
    public void write(String channel, String message) {

        // Update Frame
        if (!getArg("cli")) frame.log(USER, channel, botName, message);

        // Log
        mySQL.logResponse(channel, botName, message);
        System.out.printf("%s %s <%s> %s: %s%s", logTimestamp(), USER, channel, botName, message, BREAK);

        // Send Message
        chat.sendMessage(channel, message);
    }

    public void respond(TwitchMessageEvent event, String command, String message) {

        // Variables
        var channel = event.getChannel();

        // Update Frame
        if (!getArg("cli")) frame.log(RESPONSE, channel, botName, message);

        // Log
        mySQL.logResponse(event, command, message);
        System.out.printf("%s%s %s <%s> Executed: %s%s%s", BOLD, logTimestamp(), COMMAND, channel, command + ": " + event.getMessage(), BREAK, UNBOLD);
        System.out.printf("%s%s %s <%s> %s: %s%s%s", BOLD, logTimestamp(), RESPONSE, channel, botName, message, UNBOLD, BREAK);

        // Send Message
        chat.sendMessage(channel, message);
    }

    public void joinChannel(String channel) {
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

    public boolean getArg(String arg) {
        return main.getArg(arg);
    }

    public boolean isAdmin(String user) {
        return admins.contains(user);
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