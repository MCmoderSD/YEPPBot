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
        subTier = event.getSubscriptionTier() == 0 ? "NONE" : "TIER" + event.getSubscriptionTier();
        bits = null;
    }

    // Cheer Event
    public TwitchMessageEvent(CheerEvent event) {

        //
        EventChannel channel = event.getChannel();
        EventUser user = event.getUser();

        // Get ID's
        channelId = Integer.parseInt(trimMessage(channel.getId()));
        userId = Integer.parseInt(trimMessage(user.getId()));

        // Get Names
        this.channel = trimMessage(channel.getName());
        this.user = trimMessage(user.getName());

        // Get Message
        message = trimMessage(event.getMessage());

        // Get Additional Information
        subMonths = null;
        subTier = null;
        bits = event.getBits();
    }

    // Manual Event
    public TwitchMessageEvent(Integer channelId, Integer userId, String channel, String user, String message, @Nullable Integer subMonths, @Nullable String subTier, @Nullable Integer bits) {
        this.channelId = channelId;
        this.userId = userId;
        this.channel = channel;
        this.user = user;
        this.message = message;
        this.subMonths = subMonths;
        this.subTier = subTier;
        this.bits = bits;
    }

    // Log
    public void logToConsole() {
        System.out.println(getLog());
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

    public String getSubTier() {
        return subTier;
    }

    public Integer getBits() {
        return bits;
    }

    // Checks
    public boolean isCheer() {
        return bits != null;
    }

    public boolean hasMessage() {
        return !(message == null || message.isEmpty() || message.isBlank());
    }

    public boolean hasYEPP() {
        return message.contains("YEPP");
    }

    public boolean hasBotName() {
        for (String name : BotClient.botNames) if (message.toLowerCase().contains(name)) return true;
        return false;
    }

    public boolean hasCommand() {
        for (String prefix : prefixes) if (message.startsWith(prefix)) return true;
        for (String prefix : prefixes) if (message.contains(" " + prefix)) return true;
        return false;
    }

    // Log
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return "[" + new java.text.SimpleDateFormat("dd-MM-yyyy|HH:mm:ss").format(timestamp) + "]";
    }

    public String getLog() {
        return getFormattedTimestamp() + " <" + getChannel() + "> " + getUser() + ": " + getMessage();
    }

    public Integer getLogSubMonths() {
        return subMonths == null ? 0 : subMonths;
    }

    public Integer getLogBits() {
        return bits == null ? 0 : bits;
    }
}