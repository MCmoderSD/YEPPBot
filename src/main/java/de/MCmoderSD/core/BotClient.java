package de.MCmoderSD.core;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscribeEvent;

import de.MCmoderSD.commands.*;
import de.MCmoderSD.events.*;

import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.json.JsonNode;
import de.MCmoderSD.utilities.json.JsonUtility;

import java.util.HashMap;

import static de.MCmoderSD.utilities.Calculate.*;

public class BotClient {

    // Associations
    @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
    private final MySQL mySQL;

    // Attributes
    private final TwitchClient client;
    private final TwitchChat chat;
    private final CommandHandler commandHandler;
    private final InteractionHandler interactionHandler;

    // Variables
    private final HashMap<String, String> lurkChannel; // Users
    private final HashMap<String, Long> lurkTime; // Time

    // Constructor
    public BotClient(String botName, String botToken, String prefix, String[] admins, String[] channels, MySQL mySQL) {

        // Init MySQL
        this.mySQL = mySQL;

        // Init Credential
        OAuth2Credential credential = new OAuth2Credential("twitch", botToken);

        // Init Client and Chat
        client = TwitchClientBuilder.builder()
                .withDefaultAuthToken(credential)
                .withChatAccount(credential)
                .withEnableChat(true)
                .withEnableHelix(true)
                .build();

        chat = client.getChat();

        // Register the Bot into all channels
        for (String channel : channels) {
            try {
                chat.joinChannel(channel);
                System.out.printf("%s%s %s Joined Channel: %s%s%s", BOLD, logTimestamp(), SYSTEM, channel, BREAK, UNBOLD);
                Thread.sleep(250); // Prevent rate limit
            } catch (InterruptedException e) {
                System.out.println("Error: " + e);
            }
        }

        // Init Variables
        lurkChannel = new HashMap<>();
        lurkTime = new HashMap<>();

        // Init White and Blacklist
        JsonUtility jsonUtility = new JsonUtility();
        JsonNode whiteList = jsonUtility.load("/config/whitelist.json");
        JsonNode blackList = jsonUtility.load("/config/blacklist.json");

        // Init CommandHandler
        commandHandler = new CommandHandler(mySQL, whiteList, blackList, prefix);
        interactionHandler = new InteractionHandler(mySQL, whiteList, blackList, lurkChannel);

        // Format admin names
        for (var i = 0; i < admins.length; i++) admins[i] = admins[i].toLowerCase();

        // Init Commands
        initCommands(admins, whiteList, blackList);

        // Init Interactions
        initInteractions();

        // Init the EventListener
        EventManager eventManager = client.getEventManager();

        // Message Event
        eventManager.onEvent(ChannelMessageEvent.class, event -> {

            // Console Output
            System.out.printf("%s %s <%s> %s: %s%s", logTimestamp(), MESSAGE, getChannel(event), getAuthor(event), getMessage(event), BREAK);
            mySQL.log(logDate(), logTime(), stripBrackets(MESSAGE), getChannel(event), getAuthor(event), getMessage(event));

            // Handle Interaction
            interactionHandler.handleInteraction(event, botName);

            // Handle Command
            commandHandler.handleCommand(event, botName);
        });

        // Follow Event
        eventManager.onEvent(ChannelFollowEvent.class, event -> {
            System.out.printf("%s %s <%s> %s -> Followed%s", logTimestamp(), FOLLOW, event.getBroadcasterUserName(), event.getUserName(), BREAK);
            mySQL.log(logDate(), logTime(), stripBrackets(FOLLOW), event.getBroadcasterUserName(), event.getUserName(), "Followed");
        });

        // Sub Event
        eventManager.onEvent(ChannelSubscribeEvent.class, event -> {
            System.out.printf("%s %s <%s> %s -> Subscribed %s%s", logTimestamp(), SUBSCRIBE, event.getBroadcasterUserName(), event.getUserName(), event.getTier(), BREAK);
            mySQL.log(logDate(), logTime(), stripBrackets(SUBSCRIBE), event.getBroadcasterUserName(), event.getUserName(), "Subscribed " + event.getTier());
        });
    }

    // Init Commands
    public void initCommands(String[] admins, JsonNode whiteList, JsonNode blackList) {
        new Fact(commandHandler, chat);
        new Help(commandHandler, chat, whiteList, blackList);
        new Insult(commandHandler, chat);
        new Join(commandHandler, chat);
        new JoinChat(commandHandler, chat, admins);
        new Joke(commandHandler, chat);
        new LeaveChat(commandHandler, chat, admins);
        new Lurk(commandHandler, chat, lurkChannel, lurkTime);
        new Play(commandHandler, chat);
        // new Prompt(commandHandler, chat); ToDo Make it work
        new Say(commandHandler, chat, admins);
        new Status(commandHandler, chat);
        new Weather(commandHandler, chat);
        new Wiki(commandHandler, chat);
    }

    // Init Interactions
    public void initInteractions() {
        new ReplyYepp(interactionHandler, chat);
        new StoppedLurk(interactionHandler, chat, lurkChannel, lurkTime);
        new Yepp(interactionHandler, chat);
    }

    // Methods
    @SuppressWarnings("unused")
    public void joinChannel(String channel) {
        chat.joinChannel(channel);
    }

    @SuppressWarnings("unused")
    public void leaveChannel(String channel) {
        chat.leaveChannel(channel);
    }

    @SuppressWarnings("unused")
    public void sendMessage(String channel, String message) {
        chat.sendMessage(channel, message);
    }

    @SuppressWarnings("unused")
    public void close() {
        client.close();
    }
}