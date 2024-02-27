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
import de.MCmoderSD.events.ReplyYepp;
import de.MCmoderSD.events.StoppedLurk;
import de.MCmoderSD.events.Yepp;

import java.util.HashMap;

import static de.MCmoderSD.utilities.Calculate.*;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class BotClient {

    // Attributes
    private final TwitchClient client;
    private final TwitchChat chat;
    private final CommandHandler commandHandler;
    private final InteractionHandler interactionHandler;

    // Variables
    private final HashMap<String, String> lurkChannel = new HashMap<>(); // Users
    private final HashMap<String, Long> lurkTime = new HashMap<>(); // Time

    // Constructor
    @SuppressWarnings("CodeBlock2Expr")
    public BotClient(String botName, String botToken, String prefix, String[] admins, String[] channels) {

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

        // Init CommandHandler
        commandHandler = new CommandHandler(prefix);
        interactionHandler = new InteractionHandler(botName, lurkChannel, lurkTime);

        // Init Commands
        initCommands(admins);

        // Init Interactions
        initInteractions();

        // Init the EventListener
        EventManager eventManager = client.getEventManager();

        // Message Event
        eventManager.onEvent(ChannelMessageEvent.class, event -> {

            // Console Output
            System.out.printf("%s %s <%s> %s: %s%s", logTimestamp(), MESSAGE, event.getChannel().getName(), event.getUser().getName(), event.getMessage(), BREAK);

            // Handle Interaction
            interactionHandler.handleInteraction(event);

            // Handle Command
            commandHandler.handleCommand(event);
        });

        // Follow Event
        eventManager.onEvent(ChannelFollowEvent.class, event -> {
            System.out.printf("%s %s <%s> %s -> Followed%s", logTimestamp(), FOLLOW, event.getBroadcasterUserName(), event.getUserName(), BREAK);
        });

        // Sub Event
        eventManager.onEvent(ChannelSubscribeEvent.class, event -> {
            System.out.printf("%s %s <%s> %s -> Subscribed %s%s", logTimestamp(), SUBSCRIBE, event.getBroadcasterUserName(), event.getUserName(), event.getTier(), BREAK);
        });
    }

    // Init Commands
    public void initCommands(String[] admins) {
        new Status(commandHandler, chat);
        new Play(commandHandler, chat);
        new Join(commandHandler, chat);
        new Joke(commandHandler, chat);
        new Lurk(commandHandler, chat, lurkChannel, lurkTime);
        new Fact(commandHandler, chat);
        new Weather(commandHandler, chat);
        new Say(commandHandler, chat, admins);
    }

    // Init Interactions
    public void initInteractions() {
        new Yepp(interactionHandler, chat);
        new ReplyYepp(interactionHandler, chat);
        new StoppedLurk(interactionHandler, chat, lurkChannel, lurkTime);
    }

    // Methods
    public void joinChannel(String channel) {
        chat.joinChannel(channel);
    }

    public void leaveChannel(String channel) {
        chat.leaveChannel(channel);
    }

    public void sendMessage(String channel, String message) {
        chat.sendMessage(channel, message);
    }

    public void close() {
        client.close();
    }
}