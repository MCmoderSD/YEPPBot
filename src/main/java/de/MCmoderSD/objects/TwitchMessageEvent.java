package de.MCmoderSD.objects;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.CheerEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;

import de.MCmoderSD.core.BotClient;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;

import static de.MCmoderSD.core.BotClient.prefixes;
import static de.MCmoderSD.utilities.other.Format.*;

@SuppressWarnings("unused")
public class TwitchMessageEvent {

    // Constants
    private final Timestamp timestamp = new Timestamp(System.currentTimeMillis());

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
    private final String subTier;
    private final Integer bits;

    // Flags
    public final boolean isCheer;
    public final boolean isCommand;
    public final boolean hasBotName;
    public final boolean hasYEPP;


    // Message Event
    public TwitchMessageEvent(ChannelMessageEvent event) {

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
        subTier = parseSubTier(event.getSubscriptionTier());
        bits = 0;

        // Set Flags
        isCheer = false;
        isCommand = isCommand(message);
        hasBotName = hasBotName(message);
        hasYEPP = message.contains("YEPP");
    }

    // Cheer Event
    public TwitchMessageEvent(CheerEvent event) {

        // Get Event Information
        EventChannel eventChannel = event.getChannel();
        EventUser eventUser = event.getUser();

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
        subTier = parseSubTier(event.getSubscriptionTier());
        bits = event.getBits();

        // Set Flags
        isCheer = 0 < bits;
        isCommand = isCommand(message);
        hasBotName = hasBotName(message);
        hasYEPP = message.contains("YEPP");
    }

    // Manual Event
    public TwitchMessageEvent(Integer channelId, Integer userId, String channel, String user, String message, @Nullable Integer subMonths, @Nullable Integer subTier, @Nullable Integer bits) {

        // Set Parameters
        this.channelId = channelId;
        this.userId = userId;
        this.channel = trimMessage(channel).toLowerCase();
        this.user = trimMessage(user).toLowerCase();
        this.message = trimMessage(message);
        this.subMonths = subMonths == null ? 0 : subMonths;
        this.subTier = subTier == null ? "NONE" : parseSubTier(subTier);
        this.bits = bits == null ? 0 : bits;
        assert bits != null;

        // Set Flags
        isCheer = 0 < bits;
        isCommand = isCommand(message);
        hasBotName = hasBotName(message);
        hasYEPP = message.contains("YEPP");
    }

    // Manual Event
    public TwitchMessageEvent(Integer channelId, Integer userId, String channel, String user, String message, @Nullable Integer subMonths, @Nullable String subTier, @Nullable Integer bits) {

        // Set Parameters
        this.channelId = channelId;
        this.userId = userId;
        this.channel = trimMessage(channel).toLowerCase();
        this.user = trimMessage(user).toLowerCase();
        this.message = trimMessage(message);
        this.subMonths = subMonths == null ? 0 : subMonths;
        this.subTier = subTier == null ? "NONE" : subTier;
        this.bits = bits == null ? 0 : bits;
        assert bits != null;

        // Set Flags
        isCheer = 0 < bits;
        isCommand = isCommand(message);
        hasBotName = hasBotName(message);
        hasYEPP = message.contains("YEPP");
    }

    // Parse
    private static String parseSubTier(int tier) {
        return tier == 0 ? "NONE" : "TIER" + tier;
    }

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

    public String getSubTier() {
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return "[" + new java.text.SimpleDateFormat(TIMESTAMP_FORMAT).format(timestamp) + "]";
    }

    public String getLog() {
        return getFormattedTimestamp() + " <" + getChannel() + "> " + getUser() + ": " + getMessage();
    }
}