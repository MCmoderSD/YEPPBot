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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class BotClient {

    // Associations
    private final MySQL mySQL;

    // Attributes
    private final TwitchClient client;
    private final TwitchChat chat;
    private final CommandHandler commandHandler;
    private final InteractionHandler interactionHandler;
    private final String botName;

    // Constructor
    public BotClient(String botName, String botToken, String prefix, String[] admins, ArrayList<String> channels, MySQL mySQL, OpenAI openAI) {

        // Init Bot Name
        this.botName = botName;

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

        // Init Variables
        HashMap<String, String> lurkChannel = new HashMap<>();  // Channel, User
        HashMap<String, Long> lurkTime = new HashMap<>();       // Channel, Time

        // Init White and Blacklist
        JsonUtility jsonUtility = new JsonUtility();
        JsonNode whiteList = jsonUtility.load("/config/whitelist.json");
        JsonNode blackList = jsonUtility.load("/config/blacklist.json");

        // Init CommandHandler
        commandHandler = new CommandHandler(mySQL, chat, whiteList, blackList, prefix);
        interactionHandler = new InteractionHandler(mySQL, whiteList, blackList, lurkChannel);

        // Format admin names
        ArrayList <String> adminList = new ArrayList<>(Arrays.stream(admins).toList());

        // Init Commands
        new Counter(mySQL, commandHandler, chat, adminList);
        new CustomCommand(mySQL, commandHandler, chat, adminList);
        new Fact(mySQL, commandHandler, chat);
        new Gif(mySQL, commandHandler, chat);
        new Help(mySQL, commandHandler, chat, whiteList, blackList);
        new Insult(mySQL, commandHandler, chat);
        new Join(mySQL, commandHandler, chat);
        new Joke(mySQL, commandHandler, chat);
        // new Key(mySQL, commandHandler, chat); ToDo Make it work
        new Lurk(mySQL, commandHandler, chat, lurkChannel, lurkTime);
        new Moderate(mySQL, commandHandler, chat, adminList);
        new Ping(mySQL, commandHandler, chat);
        new Play(mySQL, commandHandler, chat);
        new Prompt(mySQL, commandHandler, chat, openAI, botName);
        // new Rank(mySQL, commandHandler, chat); ToDo Make it work
        new Say(mySQL, commandHandler, chat, adminList);
        new Status(mySQL, commandHandler, chat);
        new Translate(mySQL, commandHandler, chat, openAI, botName);
        new Weather(mySQL, commandHandler, chat);
        new Wiki(mySQL, commandHandler, chat ,openAI, botName);

        // Init Interactions
        new ReplyYepp(mySQL, interactionHandler, chat);
        new StoppedLurk(mySQL, interactionHandler, chat, lurkChannel, lurkTime);
        new Yepp(mySQL, interactionHandler, chat);

        // Register the Bot into all channels
        new Thread(() -> {
            for (String channel : channels) {
                try {
                    chat.joinChannel(channel);
                    System.out.printf("%s%s %s Joined Channel: %s%s%s", BOLD, logTimestamp(), SYSTEM, channel, BREAK, UNBOLD);
                    Thread.sleep(250); // Prevent rate limit
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            }
        }).start();

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

    // Methods
    public void sendMessage(String channel, String message) {
        if (!chat.getChannels().contains(channel)) joinChannel(channel);
        if (message.length() > 500) message = message.substring(0, 500);
        if (message.isEmpty()) return;

        chat.sendMessage(channel, message);
        System.out.printf("%s %s <%s> %s: %s%s", logTimestamp(), USER, channel, botName, message, BREAK);
        mySQL.messageSent(channel, botName, message);
    }

    // Setter
    public void joinChannel(String channel) {
        chat.joinChannel(channel);
    }

    @SuppressWarnings("unused")
    public void leaveChannel(String channel) {
        chat.leaveChannel(channel);
    }

    @SuppressWarnings("unused")
    public void close() {
        client.close();
    }

    // Getter
    @SuppressWarnings("unused")
    public ArrayList<String> getChannels() {
        return new ArrayList<>(chat.getChannels());
    }

    @SuppressWarnings("unused")
    public boolean isChannelJoined(String channel) {
        return chat.getChannels().contains(channel);
    }
}