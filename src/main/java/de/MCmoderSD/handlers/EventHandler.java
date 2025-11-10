package de.MCmoderSD.handlers;

import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.chat.events.channel.ChannelMessageActionEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.RaidEvent;

import com.github.twitch4j.eventsub.domain.chat.Raid;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.database.Database;
import de.MCmoderSD.database.manager.ChannelManager;
import de.MCmoderSD.database.manager.EventLogManager;
import de.MCmoderSD.helix.core.HelixHandler;
import de.MCmoderSD.helix.handler.*;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.FollowEvent;
import de.MCmoderSD.objects.MessageEvent;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static de.MCmoderSD.utilities.MessageHelper.*;

public class EventHandler {

    // Associations
    private final TwitchBot twitchBot;

    // Database
    private final Database database;
    private final ChannelManager channelManager;
    private final EventLogManager eventLogManager;

    // Helix-Handlers
    private final UserHandler userHandler;
    private final StreamHandler streamHandler;

    // Bot Handlers
    private final MessageHandler messageHandler;
    private final BirthdayHandler birthdayHandler;
    private final LurkHandler lurkHandler;
    private final CommandHandler commandHandler;

    // Attributes
    private final ConcurrentHashMap<Integer, TwitchUser> userCache;
    private final ConcurrentHashMap<Integer, RaidEvent> raidCache;
    private final ConcurrentHashMap<Integer, ChannelFollowEvent> followCache;

    // Constructor
    public EventHandler(TwitchBot twitchBot) {

        // Check Parameters
        if (twitchBot == null) throw new IllegalArgumentException("TwitchBot cannot be null");

        // Set Associations
        this.twitchBot = twitchBot;

        // Set Database
        database = twitchBot.getDatabase();
        channelManager = database.getChannelManager();
        eventLogManager = database.getEventLogManager();

        // Set User Handler
        HelixHandler helixHandler = twitchBot.getHelixHandler();
        userHandler = helixHandler.getUserHandler();
        streamHandler = helixHandler.getStreamHandler();

        // Initialize Message Handler
        messageHandler = new MessageHandler(twitchBot);
        birthdayHandler = messageHandler.getBirthdayHandler();
        lurkHandler = messageHandler.getLurkHandler();
        commandHandler = messageHandler.getCommandHandler();

        // Event Manager
        EventManager eventManager = twitchBot.getEventManager();

        // Initialize Attributes
        userCache = new ConcurrentHashMap<>();
        raidCache = new ConcurrentHashMap<>();
        followCache = new ConcurrentHashMap<>();

        // Message Events
        eventManager.onEvent(ChannelMessageEvent.class, this::handleMessageEvent);
        eventManager.onEvent(ChannelMessageActionEvent.class, this::handleMessageEvent);

        // Raid Event
        eventManager.onEvent(RaidEvent.class, this::handleRaidEvent);

        // Follow Event
        eventManager.onEvent(ChannelFollowEvent.class, this::handleFollowEvent);
    }

    public TwitchUser queryUser(Integer id, String user) {

        // Check Parameters
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid user ID");
        if (user == null || user.isBlank()) throw new IllegalArgumentException("Invalid username");

        // Variables
        TwitchUser twitchUser;

        // Check Cache
        boolean isCached = userCache.containsKey(id);

        // Get User
        if (isCached) twitchUser = userCache.get(id);
        else twitchUser = userHandler.getTwitchUser(id);

        // Check if Update is Needed
        boolean needsUpdate = !twitchUser.getUsername().equalsIgnoreCase(user);

        // Needs Update
        if (!isCached || needsUpdate) {
            if (needsUpdate) userCache.clear(); // Invalidate Cache
            twitchUser = userHandler.getTwitchUser(user);
            database.addTwitchUser(twitchUser);
            userCache.put(id, twitchUser);
        }

        // Return User
        return twitchUser;
    }

    // Handle
    private void handleMessageEvent(ChannelMessageEvent event) {

        // Check if Event is Mirrored
        if (event.isMirrored()) return;

        new Thread(() -> {

            // Create MessageEvent
            var messageEvent = new MessageEvent(event, this);

            // ToDo DEBUG
            //System.out.println("Nonce: " + event.getNonce() + " Device Type: " + messageEvent.getDeviceType());
            System.out.printf("%s <%s> #%s: %s%n", DEBUG, messageEvent.getChannel().getDisplayName(), messageEvent.getUser().getDisplayName(), messageEvent.getMessage());

            // Log Message Event
            eventLogManager.logMessageEvent(messageEvent);

            // Handle Message
            messageHandler.handleMessage(messageEvent);

        }, "Handle-MessageEvent-" + event.getEventId()).start();
    }

    private void handleMessageEvent(ChannelMessageActionEvent event) {

        // Check if Event is Mirrored
        if (event.isMirrored()) return;

        new Thread(() -> {

            // Create MessageEvent
            var messageEvent = new MessageEvent(event, this);

            // ToDo DEBUG
            //System.out.println("Nonce: " + event.getNonce() + " Device Type: " + messageEvent.getDeviceType());
            System.out.printf("%s <%s> #%s: %s%n", DEBUG, messageEvent.getChannel().getDisplayName(), messageEvent.getUser().getDisplayName(), messageEvent.getMessage());

            // Log Message Event
            eventLogManager.logMessageEvent(messageEvent);

            // Handle Message
            messageHandler.handleMessage(messageEvent);

        }, "Handle-MessageActionEvent-" + event.getEventId()).start();
    }

    private void handleRaidEvent(RaidEvent event) {
        new Thread(() -> {

            // Create RaidEvent
            var raidEvent = new de.MCmoderSD.objects.RaidEvent(event, this);

            // ToDo DEBUG
            System.out.printf("%s %s raided %s with %d viewers%n", EVENT, raidEvent.getUser().getDisplayName(), raidEvent.getChannel().getDisplayName(), raidEvent.getViewers());

            // Log Raid Event
            eventLogManager.logRaidEvent(raidEvent);

            // Variables
            TwitchUser channel = raidEvent.getChannel();
            TwitchUser raider = raidEvent.getUser();

            // Cache Raid Event
            raidCache.put(channel.getId(), event);

            // Send Shoutout
            if (channelManager.getAutoShoutoutChannels().get(channel.getId())) streamHandler.sendShoutout(raider, channel);

        }, "Handle-RaidEvent-" + event.getEventId()).start();
    }

    private void handleFollowEvent(ChannelFollowEvent event) {
        new Thread(() -> {

            // Create FollowEvent
            var followEvent = new FollowEvent(event, this);

            // ToDo DEBUG
            System.out.printf("%s %s followed %s%n", EVENT, followEvent.getUser().getDisplayName(), followEvent.getChannel().getDisplayName());

            // Cache Follow Event
            followCache.put(followEvent.getChannel().getId(), event);

            // Log Follow Event
            eventLogManager.logFollowEvent(followEvent);

        }).start();
    }

    // Getters
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

    public HashMap<Integer, TwitchUser> getUserCache() {
        return new HashMap<>(userCache);
    }

    public HashMap<Integer, RaidEvent> getRaidCache() {
        return new HashMap<>(raidCache);
    }

    public HashMap<Integer, ChannelFollowEvent> getFollowCache() {
        return new HashMap<>(followCache);
    }

    public TwitchUser getTwitchUser(Integer id) {

        // Check Parameters
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid user ID");

        // Return User
        return userCache.getOrDefault(id, null);
    }

    public RaidEvent getRaidEvent(Integer channelId) {

        // Check Parameters
        if (channelId == null || channelId <= 0) throw new IllegalArgumentException("Invalid channel ID");

        // Return Raid Event
        return raidCache.getOrDefault(channelId, null);
    }

    public ChannelFollowEvent getFollowEvent(Integer channelId) {

        // Check Parameters
        if (channelId == null || channelId <= 0) throw new IllegalArgumentException("Invalid channel ID");

        // Return Follow Event
        return followCache.getOrDefault(channelId, null);
    }
}