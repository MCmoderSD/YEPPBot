package de.MCmoderSD.objects;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.CheerEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.enums.SubTier;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.sql.Timestamp;

import static de.MCmoderSD.core.BotClient.prefixes;
import static de.MCmoderSD.utilities.other.Format.*;

public class TwitchMessageEvent implements Serializable{

    // Constants

    // Event Information
    private final String eventId;
    private final Timestamp timestamp;

    // ID
    private final Integer channelId;
    private final Integer userId;

    // Attributes
    private final String channel;
    private final String user;

    // Message
    private final String message;

    // Additional Information
    private final Integer subMonths;
    private final SubTier subTier;
    private final Integer bits;

    // Flags
    public final boolean isCheer;
    public final boolean isCommand;
    public final boolean hasBotName;
    public final boolean hasYEPP;


    // Message Event
    public TwitchMessageEvent(ChannelMessageEvent event) {

        // Get Event Information
        eventId = event.getEventId();
        timestamp = new Timestamp(event.getFiredAt().getTimeInMillis());

        // Get ID's
        channelId = Integer.parseInt(trimMessage(event.getChannel().getId()));
        userId = Integer.parseInt(trimMessage(event.getUser().getId()));

        // Get Names
        channel = trimMessage(event.getChannel().getName().toLowerCase());
        user = trimMessage(event.getUser().getName().toLowerCase());

        // Get Message
        message = trimMessage(event.getMessage());

        // Set Additional Information
        subMonths = event.getSubscriberMonths();
        subTier = SubTier.getSubTier(event.getSubscriptionTier());
        bits = 0;

        // Set Flags
        isCheer = false;
        isCommand = isCommand(message);
        hasBotName = hasBotName(message);
        hasYEPP = message.contains("YEPP");
    }

    // Cheer Event
    public TwitchMessageEvent(CheerEvent event) {

        // Variables
        EventChannel eventChannel = event.getChannel();
        EventUser eventUser = event.getUser();

        // Get Event Information
        eventId = event.getEventId();
        timestamp = new Timestamp(event.getFiredAt().getTimeInMillis());

        // Get ID's
        channelId = Integer.parseInt(trimMessage(eventChannel.getId()));
        userId = Integer.parseInt(trimMessage(eventUser.getId()));

        // Get Names
        channel = trimMessage(eventChannel.getName().toLowerCase());
        user = trimMessage(eventUser.getName().toLowerCase());

        // Get Message
        message = trimMessage(event.getMessage());

        // Get Additional Information
        subMonths = event.getSubscriberMonths();
        subTier = SubTier.getSubTier(event.getSubscriptionTier());
        bits = event.getBits();

        // Set Flags
        isCheer = 0 < bits;
        isCommand = isCommand(message);
        hasBotName = hasBotName(message);
        hasYEPP = message.contains("YEPP");
    }

    // Manual Event
    public TwitchMessageEvent(String eventId, Timestamp timestamp, Integer channelId, Integer userId, String channel, String user, String message, @Nullable Integer subMonths, @Nullable SubTier subTier, @Nullable Integer bits) {

        // Set Parameters
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.channelId = channelId;
        this.userId = userId;
        this.channel = trimMessage(channel).toLowerCase();
        this.user = trimMessage(user).toLowerCase();
        this.message = trimMessage(message);
        this.subMonths = subMonths == null ? 0 : subMonths;
        this.subTier = subTier == null ? SubTier.NONE : subTier;
        this.bits = bits == null ? 0 : bits;
        assert bits != null;

        // Set Flags
        isCheer = 0 < bits;
        isCommand = isCommand(message);
        hasBotName = hasBotName(message);
        hasYEPP = message.contains("YEPP");
    }

    // Parse
    private static boolean isCommand(String message) {
        for (String prefix : prefixes) if (message.startsWith(prefix)) return true;
        for (String prefix : prefixes) if (message.contains(SPACE + prefix)) return true;
        return false;
    }

    private static boolean hasBotName(String message) {
        for (String name : BotClient.botNames) if (message.toLowerCase().contains(name)) return true;
        return false;
    }

    // Getters
    public String getEventId() {
        return eventId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

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

    public String getMessage() {
        return message;
    }

    public Integer getSubMonths() {
        return subMonths;
    }

    public SubTier getSubTier() {
        return subTier;
    }

    public Integer getBits() {
        return bits;
    }

    // Checks
    public boolean isCheer() {
        return isCheer;
    }

    public boolean isCommand() {
        return isCommand;
    }

    public boolean hasBotName() {
        return hasBotName;
    }

    public boolean hasYEPP() {
        return hasYEPP;
    }

    // Log
    public void logToConsole() {
        System.out.println(getLog());
    }

    public String getFormattedTimestamp() {
        return "[" + new java.text.SimpleDateFormat(TIMESTAMP_FORMAT).format(timestamp) + "]";
    }

    public String getLog() {
        return getFormattedTimestamp() + " <" + getChannel() + "> " + getUser() + ": " + getMessage();
    }

    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(data);
        stream.writeObject(this);
        stream.flush();
        return data.toByteArray();
    }

    public static TwitchMessageEvent fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        return (TwitchMessageEvent) new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
    }
}