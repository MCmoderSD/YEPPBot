package de.MCmoderSD.objects;

import com.github.twitch4j.eventsub.events.ChannelModeratorAddEvent;
import com.github.twitch4j.eventsub.events.ChannelModeratorRemoveEvent;
import com.github.twitch4j.eventsub.events.ChannelVipAddEvent;
import com.github.twitch4j.eventsub.events.ChannelVipRemoveEvent;

import java.io.*;
import java.sql.Timestamp;

import static de.MCmoderSD.utilities.other.Format.*;

@SuppressWarnings("unused")
public class TwitchRoleEvent implements Serializable {

    // Constants
    private final Timestamp timestamp;

    // ID
    private final Integer channelId;
    private final Integer userId;

    // Attributes
    private final String channel;
    private final String user;

    private final Role role;
    private final boolean added;

    // VIP Add
    public TwitchRoleEvent(ChannelVipAddEvent event) {

        // Set Timestamp
        timestamp = new Timestamp(System.currentTimeMillis());

        // Get ID's
        channelId = Integer.valueOf(event.getBroadcasterUserId());
        userId = Integer.valueOf(event.getUserId());

        // Get Names
        channel = trimMessage(event.getBroadcasterUserName());
        user = trimMessage(event.getUserName());

        // Get Role
        role = Role.VIP;
        added = true;
    }

    // VIP Remove
    public TwitchRoleEvent(ChannelVipRemoveEvent event) {

        // Set Timestamp
        timestamp = new Timestamp(System.currentTimeMillis());

        // Get ID's
        channelId = Integer.valueOf(event.getBroadcasterUserId());
        userId = Integer.valueOf(event.getUserId());

        // Get Names
        channel = trimMessage(event.getBroadcasterUserName());
        user = trimMessage(event.getUserName());

        // Get Role
        role = Role.VIP;
        added = false;
    }

    // Moderator Add Event
    public TwitchRoleEvent(ChannelModeratorAddEvent event) {

        // Set Timestamp
        timestamp = new Timestamp(System.currentTimeMillis());

        // Get ID's
        channelId = Integer.valueOf(event.getBroadcasterUserId());
        userId = Integer.valueOf(event.getUserId());

        // Get Names
        channel = trimMessage(event.getBroadcasterUserName());
        user = trimMessage(event.getUserName());

        // Get Role
        role = Role.MOD;
        added = true;
    }

    // Moderator Remove Event
    public TwitchRoleEvent(ChannelModeratorRemoveEvent event) {

        // Set Timestamp
        timestamp = new Timestamp(System.currentTimeMillis());

        // Get ID's
        channelId = Integer.valueOf(event.getBroadcasterUserId());
        userId = Integer.valueOf(event.getUserId());

        // Get Names
        channel = trimMessage(event.getBroadcasterUserName());
        user = trimMessage(event.getUserName());

        // Get Role
        role = Role.MOD;
        added = false;
    }

    // Getters
    public Integer getChannelId() {
        return channelId;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getChannel() {
        return channel;
    }

    public String getUser() {
        return user;
    }

    public Role getRole() {
        return role;
    }

    public boolean isAdded() {
        return added;
    }

    // Log
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return "[" + new java.text.SimpleDateFormat("dd-MM-yyyy|HH:mm:ss").format(timestamp) + "]";
    }

    public String getLog() {
        return getFormattedTimestamp() + SPACE + EVENT + " <" + getChannel() + "> " + getUser() + ": " + (isAdded() ? "Added" : "Removed") + SPACE + getRole();
    }

    public void logToConsole() {
        System.out.println(getLog());
    }

    public enum Role {
        VIP, MOD
    }

    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(data);
        stream.writeObject(this);
        stream.flush();
        return data.toByteArray();
    }

    public static TwitchRoleEvent fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        return (TwitchRoleEvent) new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
    }
}