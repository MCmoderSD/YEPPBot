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

@SuppressWarnings("unused")
public class EventHandler {

    // Associations
    private final Frame frame;
    private final LogManager logManager;
    private final MessageHandler messageHandler;

    // Attributes
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

            // Log Message
            if (log) logManager.logLoyalty(event);

        }).start();
    }

    private void handleSubscribeEvent(ChannelSubscribeEvent event) {

        // Create new Thread
        new Thread(() -> {

            // Log Message
            if (log) logManager.logLoyalty(event);

        }).start();
    }

    private void handleSubscriptionGiftEvent(ChannelSubscriptionGiftEvent event) {

        // Create new Thread
        new Thread(() -> {

            // Log Message
            if (log) logManager.logLoyalty(event);

        }).start();
    }

    private void handleRaidEvent(ChannelRaidEvent event) {

        // Create new Thread
        new Thread(() -> {

            // Log Message
            if (log) logManager.logRaid(event);

        }).start();
    }

    private void handleClipCreationEvent(ChannelClipCreatedEvent event) {

            // Create new Thread
            new Thread(() -> {

                // ToDo: Implement Clip Creation Event

            }).start();
    }

    private void handleGoLiveEvent(ChannelGoLiveEvent event) {

        // Create new Thread
        new Thread(() -> {

            // ToDo: Implement Go Live Event

        }).start();
    }

    private void handleGoOfflineEvent(ChannelGoOfflineEvent event) {

        // Create new Thread
        new Thread(() -> {

            // ToDo: Implement Go Offline Event

        }).start();
    }

    private void handleChangeGameEvent(ChannelChangeGameEvent event) {

        // Create new Thread
        new Thread(() -> {

            // ToDo: Implement Change Game Event

        }).start();
    }

    private void ChangeTitleEvent(ChannelChangeTitleEvent event) {

        // Create new Thread
        new Thread(() -> {

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

    // Setter
    public void setLog(boolean log) {
        this.log = log;
    }

    public void setCli(boolean cli) {
        this.cli = cli;
    }
}