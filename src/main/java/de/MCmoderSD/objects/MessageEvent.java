package de.MCmoderSD.objects;

import com.github.twitch4j.chat.events.channel.ChannelMessageActionEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;

import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.handlers.EventHandler;
import de.MCmoderSD.enums.SubTier;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;


import static java.time.format.DateTimeFormatter.ofPattern;
import static de.MCmoderSD.utilities.MessageHelper.*;

public class MessageEvent implements Serializable {

    // Event Information
    private final UUID id;                                          // Event ID
    private final Instant firedAt;                                  // Fired At

    // Twitch Users
    private final TwitchUser channel;                               // Channel  (source)
    private final TwitchUser user;                                  // User     (author)

    // Content
    private final String message;                                   // Message Content

    // Additional Information
    private final HashSet<CommandPermission> permissions;           // Command Permissions
    private final DeviceType deviceType;                            // Device Type
    private final SubTier subTier;                                  // Subscription Tier
    private final int subMonths;                                    // Subscriber Months

    // Flags
    private final boolean isAction;                                 // /me command
    private final boolean isHighlighted;                            // Highlighted Message
    private final boolean isFirstMessage;                           // Designated First Message
    private final boolean isUserIntroduction;                       // User Introduction Message
    private final boolean isSkipSubsModeMessage;                    // Skip Subs Mode Message

    // Message Event
    public MessageEvent(ChannelMessageEvent event, EventHandler eventHandler) {

        // Check Parameters
        if (event == null) throw new IllegalArgumentException("Event cannot be null");
        if (eventHandler == null) throw new IllegalArgumentException("EventHandler cannot be null");

        // Get Event Information
        id = UUID.fromString(event.getEventId());                   // Event ID
        firedAt = event.getFiredAtInstant();                        // Fired At

        // Get Twitch Users
        channel = eventHandler.queryUser(Integer.parseInt(event.getChannel().getId()), event.getChannel().getName());   // Channel  (source)
        user = eventHandler.queryUser(Integer.parseInt(event.getUser().getId()), event.getUser().getName());            // User     (author)

        // Get Message
        message = normalizeMessage(event.getMessage());             // Message

        // Parse Additional Information
        permissions = new HashSet<>(event.getPermissions());        // Command Permissions
        deviceType = DeviceType.getDeviceType(event);               // Device Type
        subTier = SubTier.getSubTier(event.getSubscriptionTier());  // Subscription Tier
        subMonths = event.getSubscriberMonths();                    // Subscriber Months

        // Parse Flags
        isAction = false;                                           // /me command
        isHighlighted = event.isHighlightedMessage();               // Highlighted Message
        isFirstMessage = event.isDesignatedFirstMessage();          // Designated First Message
        isUserIntroduction = event.isUserIntroduction();            // User Introduction Messag
        isSkipSubsModeMessage = event.isSkipSubsModeMessage();      // Skip Subs Mode Message
    }

    // Message Action Event
    public MessageEvent(ChannelMessageActionEvent event, EventHandler eventHandler) {

        // Check Parameters
        if (event == null) throw new IllegalArgumentException("Event cannot be null");
        if (eventHandler == null) throw new IllegalArgumentException("EventHandler cannot be null");

        // Get Event Information
        id = UUID.fromString(event.getEventId());                   // Event ID
        firedAt = event.getFiredAtInstant();                        // Fired At

        // Get Twitch Users
        channel = eventHandler.queryUser(Integer.parseInt(event.getChannel().getId()), event.getChannel().getName());   // Channel  (source)
        user = eventHandler.queryUser(Integer.parseInt(event.getUser().getId()), event.getUser().getName());            // User     (author)

        // Get Message
        message = normalizeMessage(event.getMessage());             // Message

        // Parse Additional Information
        permissions = new HashSet<>(event.getPermissions());        // Command Permissions
        deviceType = DeviceType.getDeviceType(event);               // Device Type
        subTier = SubTier.getSubTier(event.getSubscriptionTier());  // Subscription Tier
        subMonths = event.getSubscriberMonths();                    // Subscriber Months

        // Parse Flags
        isAction = true;                                            // /me command
        isHighlighted = false;                                      // Highlighted Message
        isFirstMessage = false;                                     // Designated First Message
        isUserIntroduction = false;                                 // User Introduction Messag
        isSkipSubsModeMessage = false;                              // Skip Subs Mode Message
    }

    // Methods
    public void printInfo() {
        IO.println("--- Message Event Info ---");
        IO.println("Event ID: " + id);
        IO.println("Fired At: " + ofPattern(TIMESTAMP_FORMAT).withZone(ZoneId.systemDefault()).format(firedAt));
        IO.println("Channel: " + channel.getDisplayName() + " (ID: " + channel.getId() + ")");
        IO.println("User: " + user.getDisplayName() +       " (ID: " + user.getId() + ")");
        IO.println("Message: " + message);
        IO.println("Permissions: " + Arrays.toString(permissions.toArray()));
        IO.println("Device Type: " + deviceType);
        IO.println("Subscription Tier: " + subTier);
        IO.println("Subscriber Months: " + subMonths);
        IO.println("Is Action: " + isAction);
        IO.println("Is Highlighted: " + isHighlighted);
        IO.println("Is First Message: " + isFirstMessage);
        IO.println("Is User Introduction: " + isUserIntroduction);
        IO.println("Is Skip Subs Mode Message: " + isSkipSubsModeMessage);
        IO.println("--------------------------");
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

    public String getMessage() {
        return message;
    }

    public HashSet<CommandPermission> getPermissions() {
        return permissions;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public SubTier getSubTier() {
        return subTier;
    }

    public int getSubMonths() {
        return subMonths;
    }

    public boolean isAction() {
        return isAction;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public boolean isFirstMessage() {
        return isFirstMessage;
    }

    public boolean isUserIntroduction() {
        return isUserIntroduction;
    }

    public boolean isSkipSubsModeMessage() {
        return isSkipSubsModeMessage;
    }

    public enum DeviceType implements Serializable {

        // Device Types
        WEB,
        IOS,
        ANDROID,
        UNKNOWN;

        // Determine Device Type by Nonce
        private static DeviceType byNonce(String nonce) {
            if (nonce == null || nonce.isBlank()) return UNKNOWN;
            if (nonce.length() == 32 && !nonce.contains("-") && nonce.equals(nonce.toLowerCase())) return WEB;
            if (nonce.length() == 36 && nonce.contains("-") && !nonce.equals(nonce.toLowerCase())) return IOS;
            if (nonce.length() == 36 && nonce.contains("-") && nonce.equals(nonce.toLowerCase())) return ANDROID;
            return UNKNOWN;
        }

        public static DeviceType getDeviceType(ChannelMessageEvent event) {
            return byNonce(event.getNonce());
        }

        public static DeviceType getDeviceType(ChannelMessageActionEvent event) {
            return byNonce(event.getNonce());
        }
    }
}