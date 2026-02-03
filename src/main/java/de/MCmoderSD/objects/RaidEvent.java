package de.MCmoderSD.objects;

import de.MCmoderSD.handlers.EventHandler;
import de.MCmoderSD.helix.objects.TwitchUser;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class RaidEvent implements Serializable {

    // Event Information
    private final UUID id;                  // Event ID
    private final Instant firedAt;          // Fired At

    // Twitch Users
    private final TwitchUser channel;       // Channel  (target)
    private final TwitchUser user;          // Raider   (source)

    // Additional Information
    private final int viewers;              // Number of Viewers

    // Raid Event
    public RaidEvent(com.github.twitch4j.chat.events.channel.RaidEvent event, EventHandler eventHandler) {

        // Check Parameters
        if (event == null) throw new IllegalArgumentException("Event cannot be null");
        if (eventHandler == null) throw new IllegalArgumentException("EventHandler cannot be null");

        // Get Event Information
        id = UUID.fromString(event.getEventId());   // Event ID
        firedAt = event.getFiredAtInstant();        // Fired At

        // Get Twitch Users
        channel = eventHandler.queryUser(Integer.parseInt(event.getChannel().getId()), event.getChannel().getName());   // Channel (target)
        user = eventHandler.queryUser(Integer.parseInt(event.getRaider().getId()), event.getRaider().getName());        // Raider  (source)

        // Get Additional Information
        viewers = event.getViewers();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public Instant getFiredAt() {
        return firedAt;
    }

    public TwitchUser getChannel() {
        return channel;
    }

    public TwitchUser getUser() {
        return user;
    }

    public int getViewers() {
        return viewers;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firedAt, channel, user, viewers);
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == getClass() && hashCode() == obj.hashCode();
    }
}