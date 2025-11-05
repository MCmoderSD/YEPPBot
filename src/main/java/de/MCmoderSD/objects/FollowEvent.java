package de.MCmoderSD.objects;

import com.github.twitch4j.eventsub.events.ChannelFollowEvent;

import de.MCmoderSD.handlers.EventHandler;
import de.MCmoderSD.helix.objects.TwitchUser;

import java.io.Serializable;
import java.time.Instant;

public class FollowEvent implements Serializable {

    // Follow Information
    private final Instant followedAt;

    // Twitch Users
    private final TwitchUser channel;
    private final TwitchUser user;

    // Follow Event
    public FollowEvent(ChannelFollowEvent event, EventHandler eventHandler) {

        // Check Parameters
        if (event == null) throw new IllegalArgumentException("Event cannot be null");
        if (eventHandler == null) throw new IllegalArgumentException("EventHandler cannot be null");

        // Get Follower Information
        followedAt = event.getFollowedAt();
        channel = eventHandler.queryUser(Integer.parseInt(event.getBroadcasterUserId()), event.getBroadcasterUserName());
        user = eventHandler.queryUser(Integer.parseInt(event.getUserId()), event.getUserName());
    }

    // Getters
    public Instant getFollowedAt() {
        return followedAt;
    }

    public TwitchUser getChannel() {
        return channel;
    }

    public TwitchUser getUser() {
        return user;
    }
}