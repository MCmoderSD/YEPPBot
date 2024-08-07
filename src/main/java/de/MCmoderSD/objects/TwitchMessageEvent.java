package de.MCmoderSD.objects;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelCheerEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscriptionMessageEvent;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.utilities.database.MySQL;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import static de.MCmoderSD.utilities.other.Calculate.*;

@SuppressWarnings("unused")
public class TwitchMessageEvent {

    // Constants
    private final Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    // ID
    private final int channelId;
    private final int userId;

    // Attributes
    private final String channel;
    private final String user;

    // Message
    private final String message;

    // Additional Information
    private final Integer subMonths;
    private final Integer subStreak;
    private final String subTier;
    private final Integer bits;


    public TwitchMessageEvent(ChannelMessageEvent event) {

        // Get ID's
        channelId = Integer.parseInt(trimMessage(event.getChannel().getId()));
        userId = Integer.parseInt(trimMessage(event.getUser().getId()));

        // Get Names
        channel = trimMessage(event.getChannel().getName());
        user = trimMessage(event.getUser().getName());

        // Get Message
        message = trimMessage(event.getMessage());

        // Set Additional Information
        subMonths = event.getSubscriberMonths();
        subStreak = null;
        subTier = event.getSubscriptionTier() == 0 ? "NONE" : "TIER" + event.getSubscriptionTier();
        bits = null;
    }

    public TwitchMessageEvent(ChannelCheerEvent event) {

        // Get ID's
        channelId = Integer.parseInt(trimMessage(event.getBroadcasterUserId()));
        userId = Integer.parseInt(trimMessage(event.getUserId()));

        // Get Names
        channel = trimMessage(event.getBroadcasterUserName());
        user = trimMessage(event.getUserName());

        // Get Message
        message = trimMessage(event.getMessage());

        // Get Additional Information
        subMonths = null;
        subStreak = null;
        subTier = null;
        bits = event.getBits();
    }

    public TwitchMessageEvent(ChannelSubscriptionMessageEvent event) {

        /// Get ID's
        channelId = Integer.parseInt(trimMessage(event.getBroadcasterUserId()));
        userId = Integer.parseInt(trimMessage(event.getUserId()));

        // Get Names
        channel = trimMessage(event.getBroadcasterUserName());
        user = trimMessage(event.getUserName());

        // Get Message
        message = trimMessage(event.getMessage().getText());

        // Get Additional Information
        subMonths = event.getCumulativeMonths();
        subStreak = event.getStreakMonths();
        subTier = event.getTier().ordinalName();
        bits = null;
    }

    // Methods
    public void logToMySQL(MySQL mySQL) {
        new Thread(() -> {

            // Check Channel and User
            mySQL.checkChannel(channelId, channel);
            mySQL.checkUser(userId, user);

            // Log message
            try {
                if (!mySQL.isConnected()) mySQL.connect(); // connect

                // Prepare statement
                String query = "INSERT INTO " + "MessageLog" + " (timestamp, type, channel_id, user_id, message) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
                preparedStatement.setTimestamp(1, timestamp); // set timestamp
                preparedStatement.setString(2, getType()); // set type
                preparedStatement.setInt(3, channelId); // set channel
                preparedStatement.setInt(4, userId); // set user
                preparedStatement.setString(5, message); // set message
                preparedStatement.executeUpdate(); // execute
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    public void logToConsole() {
        System.out.println(getLog());
    }

    // Getter
    public String getType() {
        if (isSub()) return SUBSCRIBE;
        if (isCheer()) return CHEER;
        return MESSAGE;
    }

    public int getChannelId() {
        return channelId;
    }

    public int getUserId() {
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

    public Integer getSubStreak() {
        return subStreak;
    }

    public String getSubTier() {
        return subTier;
    }

    public Integer getBits() {
        return bits;
    }

    // Checks
    public boolean isSub() {
        return subMonths != null && subStreak != null && subTier != null;
    }

    public boolean isCheer() {
        return bits != null;
    }

    public boolean hasMessage() {
        return !isSub() && !isCheer() && !(message == null || message.isEmpty());
    }

    public boolean hasYEPP() {
        return message.contains("YEPP");
    }

    public boolean hasBotName() {
        return message.contains(BotClient.botName);
    }

    public boolean hasCommand() {
        return message.startsWith(BotClient.prefix) || message.contains(" " + BotClient.prefix);
    }


    // Log
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return "[" + new java.text.SimpleDateFormat("dd-MM-yyyy|HH:mm:ss").format(timestamp) + "]";
    }

    public String getLog() {
        return getFormattedTimestamp() + " " + getType() + " <" + getChannel() + "> " + getUser() + ": " + getMessage();
    }
}