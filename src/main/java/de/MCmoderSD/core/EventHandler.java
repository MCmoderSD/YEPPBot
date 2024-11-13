package de.MCmoderSD.core;

import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.events.ChannelClipCreatedEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.events.ChannelViewerCountUpdateEvent;
import com.github.twitch4j.events.ChannelFollowCountUpdateEvent;
import com.github.twitch4j.events.ChannelChangeGameEvent;
import com.github.twitch4j.events.ChannelChangeTitleEvent;
import com.github.twitch4j.eventsub.events.ChannelCheerEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.ChannelModeratorAddEvent;
import com.github.twitch4j.eventsub.events.ChannelModeratorRemoveEvent;
import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscribeEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscriptionGiftEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscriptionMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelVipAddEvent;
import com.github.twitch4j.eventsub.events.ChannelVipRemoveEvent;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.objects.TwitchRoleEvent;
import de.MCmoderSD.utilities.database.manager.LogManager;

import java.util.HashMap;

@SuppressWarnings("unused")
public class EventHandler {

    // Associations
    private final Frame frame;
    private final LogManager logManager;
    private final MessageHandler messageHandler;

    // Attributes
    private final HashMap<Integer, TwitchMessageEvent> lastMessage;
    private final HashMap<Integer, TwitchMessageEvent> lastCheer;
    private final HashMap<Integer, TwitchMessageEvent> lastSubscription;

    private final HashMap<Integer, TwitchRoleEvent> lastVipAdd;
    private final HashMap<Integer, TwitchRoleEvent> lastVipRemove;
    private final HashMap<Integer, TwitchRoleEvent> lastModeratorAdd;
    private final HashMap<Integer, TwitchRoleEvent> lastModeratorRemove;

    private final HashMap<Integer, ChannelFollowEvent> lastFollow;
    private final HashMap<Integer, ChannelSubscribeEvent> lastSubscribe;
    private final HashMap<Integer, ChannelSubscriptionGiftEvent> lastSubscriptionGift;

    private final HashMap<Integer, ChannelRaidEvent> lastRaid;

    private final HashMap<Integer, ChannelClipCreatedEvent> lastClip;

    private final HashMap<Integer, ChannelGoLiveEvent> lastGoLive;
    private final HashMap<Integer, ChannelGoOfflineEvent> lastGoOffline;
    private final HashMap<Integer, ChannelChangeGameEvent> lastChangeGame;
    private final HashMap<Integer, ChannelChangeTitleEvent> lastChangeTitle;

    // Flags
    private boolean cli;
    private boolean log;

    // Constructor
    public EventHandler(BotClient botClient, Frame frame, LogManager logManager, EventManager eventManager, MessageHandler messageHandler) {

        // Init Associations
        this.frame = frame;
        this.logManager = logManager;
        this.messageHandler = messageHandler;

        // Get Config
        cli = botClient.isCli();
        log = !botClient.isCli();

        // Init HashMaps
        lastMessage = new HashMap<>();
        lastCheer = new HashMap<>();
        lastSubscription = new HashMap<>();

        lastVipAdd = new HashMap<>();
        lastVipRemove = new HashMap<>();
        lastModeratorAdd = new HashMap<>();

        lastModeratorRemove = new HashMap<>();
        lastFollow = new HashMap<>();
        lastSubscribe = new HashMap<>();

        lastSubscriptionGift = new HashMap<>();
        lastRaid = new HashMap<>();
        lastClip = new HashMap<>();

        lastGoLive = new HashMap<>();
        lastGoOffline = new HashMap<>();
        lastChangeGame = new HashMap<>();
        lastChangeTitle = new HashMap<>();

        // Message Events
        eventManager.onEvent(ChannelMessageEvent.class, this::handleMessageEvent);
        eventManager.onEvent(ChannelCheerEvent.class, this::handleCheerEvent);
        eventManager.onEvent(ChannelSubscriptionMessageEvent.class, this::handleSubscriptionMessageEvent);

        // Role Events
        eventManager.onEvent(ChannelVipAddEvent.class, event -> handleVIPRoleEvent(new TwitchRoleEvent(event)));
        eventManager.onEvent(ChannelVipRemoveEvent.class, event -> handleVIPRoleEvent(new TwitchRoleEvent(event)));
        eventManager.onEvent(ChannelModeratorAddEvent.class, event -> handleModeratorRoleEvent(new TwitchRoleEvent(event)));
        eventManager.onEvent(ChannelModeratorRemoveEvent.class, event -> handleModeratorRoleEvent(new TwitchRoleEvent(event)));

        // Loyalty Events
        eventManager.onEvent(ChannelFollowCountUpdateEvent.class, this::handleFollowCountEvent);
        eventManager.onEvent(ChannelFollowEvent.class, this::handleFollowEvent);
        eventManager.onEvent(ChannelSubscribeEvent.class, this::handleSubscribeEvent);
        eventManager.onEvent(ChannelSubscriptionGiftEvent.class, this::handleSubscriptionGiftEvent);

        // Raid Events
        eventManager.onEvent(ChannelRaidEvent.class, this::handleRaidEvent);

        // Clip Events
        eventManager.onEvent(ChannelClipCreatedEvent.class, this::handleClipCreationEvent);
        
        // Stream Events
        eventManager.onEvent(ChannelGoLiveEvent.class, this::handleGoLiveEvent);
        eventManager.onEvent(ChannelGoOfflineEvent.class, this::handleGoOfflineEvent);
        eventManager.onEvent(ChannelChangeGameEvent.class, this::handleChangeGameEvent);
        eventManager.onEvent(ChannelChangeTitleEvent.class, this::ChangeTitleEvent);
        eventManager.onEvent(ChannelViewerCountUpdateEvent.class, this::viewerCountEvent);
    }

    // Handle Events
    private void handleMessageEvent(ChannelMessageEvent event) {

        // Create new Thread
        new Thread(() -> {

            // TwitchMessageEvent
            TwitchMessageEvent messageEvent = new TwitchMessageEvent(event);
            lastMessage.replace(messageEvent.getChannelId(), messageEvent);

            // Log Message
            messageEvent.logToConsole();
            if (log) logManager.logMessage(messageEvent);

            // Update Frame
            if (!cli) frame.log(messageEvent);

            // Handle Message
            messageHandler.handleMessage(messageEvent);

        }).start();
    }

    private void handleCheerEvent(ChannelCheerEvent event) {

        // Create new Thread
        new Thread(() -> {

            // TwitchMessageEvent
            TwitchMessageEvent messageEvent = new TwitchMessageEvent(event);
            lastCheer.replace(messageEvent.getChannelId(), messageEvent);

            // Log Message
            messageEvent.logToConsole();
            if (log) logManager.logMessage(messageEvent);

            // Update Frame
            if (!cli) frame.log(messageEvent);

            // Handle Message
            messageHandler.handleMessage(messageEvent);

        }).start();
    }

    private void handleSubscriptionMessageEvent(ChannelSubscriptionMessageEvent event) {

        // Create new Thread
        new Thread(() -> {

            // TwitchMessageEvent
            TwitchMessageEvent messageEvent = new TwitchMessageEvent(event);
            lastSubscription.replace(messageEvent.getChannelId(), messageEvent);

            // Log Message
            messageEvent.logToConsole();
            if (log) logManager.logMessage(messageEvent);

            // Update Frame
            if (!cli) frame.log(messageEvent);

            // Handle Message
            messageHandler.handleMessage(messageEvent);

        }).start();
    }

    private void handleVIPRoleEvent(TwitchRoleEvent event) {

        // Create new Thread
        new Thread(() -> {

            // Log Message
            event.logToConsole();
            if (log) logManager.logRole(event);

        }).start();
    }

    private void handleModeratorRoleEvent(TwitchRoleEvent event) {

        // Create new Thread
        new Thread(() -> {

            // Log Message
            event.logToConsole();
            if (log) logManager.logRole(event);

        }).start();
    }

    private void handleFollowCountEvent(ChannelFollowCountUpdateEvent event) {

        // Create new Thread
        new Thread(() -> {

            // ToDo: Implement Follow Count Event

        }).start();
    }

    private void handleFollowEvent(ChannelFollowEvent event) {

        // Create new Thread
        new Thread(() -> {

            lastFollow.replace(Integer.parseInt(event.getBroadcasterUserId()), event);

            // Log Message
            if (log) logManager.logLoyalty(event);

        }).start();
    }

    private void handleSubscribeEvent(ChannelSubscribeEvent event) {

        // Create new Thread
        new Thread(() -> {

            lastSubscribe.replace(Integer.parseInt(event.getBroadcasterUserId()), event);

            // Log Message
            if (log) logManager.logLoyalty(event);

        }).start();
    }

    private void handleSubscriptionGiftEvent(ChannelSubscriptionGiftEvent event) {

        // Create new Thread
        new Thread(() -> {

            lastSubscriptionGift.replace(Integer.parseInt(event.getBroadcasterUserId()), event);

            // Log Message
            if (log) logManager.logLoyalty(event);

        }).start();
    }

    private void handleRaidEvent(ChannelRaidEvent event) {

        // Create new Thread
        new Thread(() -> {

            lastRaid.replace(Integer.parseInt(event.getToBroadcasterUserId()), event);

            // Log Message
            if (log) logManager.logRaid(event);

        }).start();
    }

    private void handleClipCreationEvent(ChannelClipCreatedEvent event) {

            // Create new Thread
            new Thread(() -> {

                lastClip.replace(Integer.parseInt(event.getChannel().getId()), event);

                // ToDo: Implement Clip Creation Event

            }).start();
    }

    private void handleGoLiveEvent(ChannelGoLiveEvent event) {

        // Create new Thread
        new Thread(() -> {

            lastGoLive.replace(Integer.parseInt(event.getChannel().getId()), event);

            // ToDo: Implement Go Live Event

        }).start();
    }

    private void handleGoOfflineEvent(ChannelGoOfflineEvent event) {

        // Create new Thread
        new Thread(() -> {

            lastGoOffline.replace(Integer.parseInt(event.getChannel().getId()), event);

            // ToDo: Implement Go Offline Event

        }).start();
    }

    private void handleChangeGameEvent(ChannelChangeGameEvent event) {

        // Create new Thread
        new Thread(() -> {

            lastChangeGame.replace(Integer.parseInt(event.getChannel().getId()), event);

            // ToDo: Implement Change Game Event

        }).start();
    }

    private void ChangeTitleEvent(ChannelChangeTitleEvent event) {

        // Create new Thread
        new Thread(() -> {

            lastChangeTitle.replace(Integer.parseInt(event.getChannel().getId()), event);

            // ToDo: Implement Change Title Event

        }).start();
    }

    private void viewerCountEvent(ChannelViewerCountUpdateEvent event) {

        // Create new Thread
        new Thread(() -> {

            // ToDo: Implement Viewer Count Event

        }).start();
    }

    // Getter
    public boolean isCli() {
        return cli;
    }

    public boolean isLog() {
        return log;
    }

    public HashMap<Integer, TwitchMessageEvent> getLastMessage() {
        return lastMessage;
    }

    public HashMap<Integer, TwitchMessageEvent> getLastCheer() {
        return lastCheer;
    }

    public HashMap<Integer, TwitchMessageEvent> getLastSubscription() {
        return lastSubscription;
    }

    public HashMap<Integer, TwitchRoleEvent> getLastVipAdd() {
        return lastVipAdd;
    }

    public HashMap<Integer, TwitchRoleEvent> getLastVipRemove() {
        return lastVipRemove;
    }

    public HashMap<Integer, TwitchRoleEvent> getLastModeratorAdd() {
        return lastModeratorAdd;
    }

    public HashMap<Integer, TwitchRoleEvent> getLastModeratorRemove() {
        return lastModeratorRemove;
    }

    public HashMap<Integer, ChannelFollowEvent> getLastFollow() {
        return lastFollow;
    }

    public HashMap<Integer, ChannelSubscribeEvent> getLastSubscribe() {
        return lastSubscribe;
    }

    public HashMap<Integer, ChannelSubscriptionGiftEvent> getLastSubscriptionGift() {
        return lastSubscriptionGift;
    }

    public HashMap<Integer, ChannelRaidEvent> getLastRaid() {
        return lastRaid;
    }

    public HashMap<Integer, ChannelClipCreatedEvent> getLastClip() {
        return lastClip;
    }

    public HashMap<Integer, ChannelGoLiveEvent> getLastGoLive() {
        return lastGoLive;
    }

    public HashMap<Integer, ChannelGoOfflineEvent> getLastGoOffline() {
        return lastGoOffline;
    }

    public HashMap<Integer, ChannelChangeGameEvent> getLastChangeGame() {
        return lastChangeGame;
    }

    public HashMap<Integer, ChannelChangeTitleEvent> getLastChangeTitle() {
        return lastChangeTitle;
    }

    public TwitchMessageEvent getLastMessage(Integer channelId) {
        if (!lastMessage.containsKey(channelId)) return null;
        return lastMessage.get(channelId);
    }

    public TwitchMessageEvent getLastCheer(Integer channelId) {
        if (!lastCheer.containsKey(channelId)) return null;
        return lastCheer.get(channelId);
    }

    public TwitchMessageEvent getLastSubscription(Integer channelId) {
        if (!lastSubscription.containsKey(channelId)) return null;
        return lastSubscription.get(channelId);
    }

    public TwitchRoleEvent getLastVipAdd(Integer channelId) {
        if (!lastVipAdd.containsKey(channelId)) return null;
        return lastVipAdd.get(channelId);
    }

    public TwitchRoleEvent getLastVipRemove(Integer channelId) {
        if (!lastVipRemove.containsKey(channelId)) return null;
        return lastVipRemove.get(channelId);
    }

    public TwitchRoleEvent getLastModeratorAdd(Integer channelId) {
        if (!lastModeratorAdd.containsKey(channelId)) return null;
        return lastModeratorAdd.get(channelId);
    }

    public TwitchRoleEvent getLastModeratorRemove(Integer channelId) {
        if (!lastModeratorRemove.containsKey(channelId)) return null;
        return lastModeratorRemove.get(channelId);
    }

    public ChannelFollowEvent getLastFollow(Integer channelId) {
        if (!lastFollow.containsKey(channelId)) return null;
        return lastFollow.get(channelId);
    }

    public ChannelSubscribeEvent getLastSubscribe(Integer channelId) {
        if (!lastSubscribe.containsKey(channelId)) return null;
        return lastSubscribe.get(channelId);
    }

    public ChannelSubscriptionGiftEvent getLastSubscriptionGift(Integer channelId) {
        if (!lastSubscriptionGift.containsKey(channelId)) return null;
        return lastSubscriptionGift.get(channelId);
    }

    public ChannelRaidEvent getLastRaid(Integer channelId) {
        if (!lastRaid.containsKey(channelId)) return null;
        return lastRaid.get(channelId);
    }

    public ChannelClipCreatedEvent getLastClip(Integer channelId) {
        if (!lastClip.containsKey(channelId)) return null;
        return lastClip.get(channelId);
    }

    public ChannelGoLiveEvent getLastGoLive(Integer channelId) {
        if (!lastGoLive.containsKey(channelId)) return null;
        return lastGoLive.get(channelId);
    }

    public ChannelGoOfflineEvent getLastGoOffline(Integer channelId) {
        if (!lastGoOffline.containsKey(channelId)) return null;
        return lastGoOffline.get(channelId);
    }

    public ChannelChangeGameEvent getLastChangeGame(Integer channelId) {
        if (!lastChangeGame.containsKey(channelId)) return null;
        return lastChangeGame.get(channelId);
    }

    public ChannelChangeTitleEvent getLastChangeTitle(Integer channelId) {
        if (!lastChangeTitle.containsKey(channelId)) return null;
        return lastChangeTitle.get(channelId);
    }

    // Setter
    public void setLog(boolean log) {
        this.log = log;
    }

    public void setCli(boolean cli) {
        this.cli = cli;
    }

    public void clearAll() {
        clearLastMessage();
        clearLastCheer();
        clearLastSubscription();
        clearLastVipAdd();
        clearLastVipRemove();
        clearLastModeratorAdd();
        clearLastModeratorRemove();
        clearLastFollow();
        clearLastSubscribe();
        clearLastSubscriptionGift();
        clearLastRaid();
        clearLastClip();
        clearLastGoLive();
        clearLastGoOffline();
        clearLastChangeGame();
        clearLastChangeTitle();
    }

    public void clearLastMessage() {
        lastMessage.clear();
    }

    public void clearLastCheer() {
        lastCheer.clear();
    }

    public void clearLastSubscription() {
        lastSubscription.clear();
    }

    public void clearLastVipAdd() {
        lastVipAdd.clear();
    }

    public void clearLastVipRemove() {
        lastVipRemove.clear();
    }

    public void clearLastModeratorAdd() {
        lastModeratorAdd.clear();
    }

    public void clearLastModeratorRemove() {
        lastModeratorRemove.clear();
    }

    public void clearLastFollow() {
        lastFollow.clear();
    }

    public void clearLastSubscribe() {
        lastSubscribe.clear();
    }

    public void clearLastSubscriptionGift() {
        lastSubscriptionGift.clear();
    }

    public void clearLastRaid() {
        lastRaid.clear();
    }

    public void clearLastClip() {
        lastClip.clear();
    }

    public void clearLastGoLive() {
        lastGoLive.clear();
    }

    public void clearLastGoOffline() {
        lastGoOffline.clear();
    }

    public void clearLastChangeGame() {
        lastChangeGame.clear();
    }

    public void clearLastChangeTitle() {
        lastChangeTitle.clear();
    }
}