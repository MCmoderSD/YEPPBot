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
import de.MCmoderSD.utilities.other.OpenAI;

import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class BotClient {

    // Associations
    private final MySQL mySQL;
    private final OpenAI openAI;

    // Attributes
    private final TwitchClient client;
    private final TwitchChat chat;
    private final CommandHandler commandHandler;
    private final InteractionHandler interactionHandler;
    private final String botName;

    // Variables
    private final HashMap<String, String> lurkChannel; // Users
    private final HashMap<String, Long> lurkTime; // Time

    // Constructor
    public BotClient(String botName, String botToken, String prefix, String[] admins, String[] channels, MySQL mySQL, OpenAI openAI) {

        // Init Bot Name
        this.botName = botName;

        // Init MySQL
        this.mySQL = mySQL;

        // Init OpenAI
        this.openAI = openAI;

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

            // Log to MySQL
            mySQL.logMessage(event);

            // Console Output
            System.out.printf("%s %s <%s> %s: %s%s", logTimestamp(), MESSAGE, getChannel(event), getAuthor(event), getMessage(event), BREAK);

            // Handle Interaction
            interactionHandler.handleInteraction(event, botName);

            // Handle Command
            commandHandler.handleCommand(event, botName);
        });

        // Follow Event
        eventManager.onEvent(ChannelFollowEvent.class, event -> System.out.printf("%s %s <%s> %s -> Followed%s", logTimestamp(), FOLLOW, event.getBroadcasterUserName(), event.getUserName(), BREAK));

        // Sub Event
        eventManager.onEvent(ChannelSubscribeEvent.class, event -> System.out.printf("%s %s <%s> %s -> Subscribed %s%s", logTimestamp(), SUBSCRIBE, event.getBroadcasterUserName(), event.getUserName(), event.getTier(), BREAK));
    }

    // Init Commands
    public void initCommands(String[] admins, JsonNode whiteList, JsonNode blackList) {
        new Fact(mySQL, commandHandler, chat);
        new Gif(mySQL, commandHandler, chat);
        new Help(mySQL, commandHandler, chat, whiteList, blackList);
        new Insult(mySQL, commandHandler, chat);
        new Join(mySQL, commandHandler, chat);
        new JoinChat(mySQL, commandHandler, chat, admins);
        new Joke(mySQL, commandHandler, chat);
        // new Key(mySQL, commandHandler, chat); ToDo Fix
        new LeaveChat(mySQL, commandHandler, chat, admins);
        new Lurk(mySQL, commandHandler, chat, lurkChannel, lurkTime);
        new Play(mySQL, commandHandler, chat);
        new Prompt(mySQL, commandHandler, chat, openAI, botName);
        // new Rank(mySQL, commandHandler, chat); ToDo Fix
        new Say(mySQL, commandHandler, chat, admins);
        new Status(mySQL, commandHandler, chat);
        new Translate(mySQL, commandHandler, chat, openAI, botName);
        new Weather(mySQL, commandHandler, chat);
        new Wiki(mySQL, commandHandler, chat);
    }

    // Init Interactions
    public void initInteractions() {
        new ReplyYepp(mySQL, interactionHandler, chat);
        new StoppedLurk(mySQL, interactionHandler, chat, lurkChannel, lurkTime);
        new Yepp(mySQL, interactionHandler, chat);
    }

    // Methods
    public void joinChannel(String channel) {
        chat.joinChannel(channel);
    }

    @SuppressWarnings("unused")
    public void leaveChannel(String channel) {
        chat.leaveChannel(channel);
    }

    public void sendMessage(String channel, String message) {
        if (!chat.getChannels().contains(channel)) joinChannel(channel);
        if (message.length() > 500) message = message.substring(0, 500);
        if (message.isEmpty()) return;

        chat.sendMessage(channel, message);
        System.out.printf("%s %s <%s> %s: %s%s", logTimestamp(), USER, channel, botName, message, BREAK);
        mySQL.messageSent(channel, botName, message);
    }

    @SuppressWarnings("unused")
    public void close() {
        client.close();
    }
}