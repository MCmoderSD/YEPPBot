package de.MCmoderSD.utilities.database.manager;

import com.github.twitch4j.chat.events.channel.RaidEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscribeEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscriptionGiftEvent;

import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.JavaAudioLibrary.AudioFile;
import de.MCmoderSD.enums.LoyaltyType;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.objects.TwitchRoleEvent;
import de.MCmoderSD.utilities.database.SQL;

import java.sql.*;

import static de.MCmoderSD.utilities.other.Format.*;

public class LogManager {
    
    // Associations
    private final SQL sql;
    
    // Constructor
    public LogManager(SQL sql) {

        // Set associations
        this.sql = sql;

        // Initialize
        initTables();
    }

    // Initialize Tables
    private void initTables() {
        try {

            // Variables
            Connection connection = sql.getConnection();

            // Condition for creating tables
            String condition = "CREATE TABLE IF NOT EXISTS ";

            // SQL statement for creating the message log table
            connection.prepareStatement(condition +
                """
                MessageLog (
                timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                channelId INT NOT NULL,
                userId INT NOT NULL,
                message VARCHAR(500),
                bits INT NOT NULL DEFAULT 0,
                subMonths INT NOT NULL DEFAULT 0,
                subTier ENUM('NONE', 'TIER1', 'TIER2', 'TIER3', 'PRIME') NOT NULL DEFAULT 'NONE',
                FOREIGN KEY (channelId) REFERENCES Users(id),
                FOREIGN KEY (userId) REFERENCES Users(id)
                ) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=1 CHARSET=utf8mb4
                """
            ).execute();

            // SQL statement for creating the command log table
            connection.prepareStatement(condition +
                    """
                    CommandLog (
                    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    channelId INT NOT NULL,
                    userId INT NOT NULL,
                    command TEXT NOT NULL,
                    args VARCHAR(500),
                    bits INT NOT NULL DEFAULT 0,
                    subMonths INT NOT NULL DEFAULT 0,
                    subTier ENUM('NONE', 'TIER1', 'TIER2', 'TIER3', 'PRIME') NOT NULL DEFAULT 'NONE',
                    FOREIGN KEY (channelId) REFERENCES Users(id),
                    FOREIGN KEY (userId) REFERENCES Users(id)
                    ) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=1 CHARSET=utf8mb4
                    """
            ).execute();

            // SQL statement for creating the response log table
            connection.prepareStatement(condition +
                    """
                    ResponseLog (
                    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    channelId INT NOT NULL,
                    userId INT NOT NULL,
                    command TEXT NOT NULL,
                    args VARCHAR(500),
                    response VARCHAR(500),
                    bits INT NOT NULL DEFAULT 0,
                    subMonths INT NOT NULL DEFAULT 0,
                    subTier ENUM('NONE', 'TIER1', 'TIER2', 'TIER3', 'PRIME') NOT NULL DEFAULT 'NONE',
                    FOREIGN KEY (channelId) REFERENCES Users(id),
                    FOREIGN KEY (userId) REFERENCES Users(id)
                    ) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=1 CHARSET=utf8mb4
                    """
            ).execute();

            // SQL statement for creating the role log table
            connection.prepareStatement(condition +
                    """
                    RoleLog (
                    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    channelId INT NOT NULL,
                    userId INT NOT NULL,
                    role ENUM('VIP', 'MOD') NOT NULL,
                    added BIT NOT NULL,
                    FOREIGN KEY (channelId) REFERENCES Users(id),
                    FOREIGN KEY (userId) REFERENCES Users(id)
                    ) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=1 CHARSET=utf8mb4
                    """
            ).execute();

            // SQL statement for creating the loyalty log table
            connection.prepareStatement(condition +
                    """
                    LoyaltyLog (
                    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    channelId INT NOT NULL,
                    userId INT NOT NULL,
                    type ENUM('FOLLOW', 'SUBSCRIPTION', 'GIFT') NOT NULL,
                    subTier ENUM('NONE', 'TIER1', 'TIER2', 'TIER3', 'PRIME') NOT NULL DEFAULT 'NONE',
                    giftAmount INT,
                    giftTotal INT,
                    FOREIGN KEY (channelId) REFERENCES Users(id),
                    FOREIGN KEY (userId) REFERENCES Users(id)
                    ) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=1 CHARSET=utf8mb4
                    """
            ).execute();

            // SQL statement for creating the raid log table
            connection.prepareStatement(condition +
                    """
                    RaidLog (
                    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    channelId INT NOT NULL,
                    raiderId INT NOT NULL,
                    viewerAmount INT NOT NULL DEFAULT 0,
                    FOREIGN KEY (channelId) REFERENCES Users(id),
                    FOREIGN KEY (raiderId) REFERENCES Users(id)
                    ) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=1 CHARSET=utf8mb4
                    """
            ).execute();

            // SQL statement for creating the tts log table
            connection.prepareStatement(condition +
                    """
                    TTSLog (
                    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    channelId INT NOT NULL,
                    userId INT NOT NULL,
                    message VARCHAR(500),
                    audioData LONGBLOB NOT NULL,
                    bits INT NOT NULL DEFAULT 0,
                    subMonths INT NOT NULL DEFAULT 0,
                    subTier ENUM('NONE', 'TIER1', 'TIER2', 'TIER3', 'PRIME') NOT NULL DEFAULT 'NONE',
                    FOREIGN KEY (channelId) REFERENCES Users(id),
                    FOREIGN KEY (userId) REFERENCES Users(id)
                    ) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=1 CHARSET=utf8mb4
                    """
            ).execute();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Log Message
    public void logMessage(TwitchMessageEvent event) {
        new Thread(() -> {

            // Log message
            try {
                if (!sql.isConnected()) sql.connect(); // connect

                // Variables
                var channelId = event.getChannelId();
                var userId = event.getUserId();

                // Check Channel and User
                sql.checkCache(userId, event.getUser(), false);
                sql.checkCache(channelId, event.getChannel(), true);

                // Prepare statement
                PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                        "INSERT INTO MessageLog (timestamp, channelId, userId, message, bits, subMonths, subTier) VALUES (?, ?, ?, ?, ?, ?, ?)"
                );

                // Set values and execute
                preparedStatement.setTimestamp(1, event.getTimestamp());    // set timestamp
                preparedStatement.setInt(2, channelId);                     // set channel
                preparedStatement.setInt(3, userId);                        // set user
                preparedStatement.setString(4, event.getMessage());         // set message
                preparedStatement.setInt(5, event.getBits());               // set bits
                preparedStatement.setInt(6, event.getSubMonths());          // set subMonths
                preparedStatement.setString(7, event.getSubTier().name());  // set subTier
                preparedStatement.executeUpdate(); // execute

                // Close resources
                preparedStatement.close();

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Log Command
    public void logCommand(TwitchMessageEvent event, String trigger, String args) {
        new Thread(() -> {

            // Variables
            var channelId = event.getChannelId();
            var userId = event.getUserId();
            var channel = event.getChannel();
            var user = event.getUser();

            try {
                if (!sql.isConnected()) sql.connect(); // connect

                // Check Channel and User
                sql.checkCache(userId, user, false);
                sql.checkCache(channelId, channel, true);

                // Prepare statement
                PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                        "INSERT INTO CommandLog (timestamp, channelId, userId, command, args, bits, subMonths, subTier) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                );

                // Set values and execute
                preparedStatement.setTimestamp(1, event.getTimestamp());    // set timestamp
                preparedStatement.setInt(2, channelId);                     // set channel
                preparedStatement.setInt(3, userId);                        // set user
                preparedStatement.setString(4, trigger);                    // set command
                preparedStatement.setString(5, args);                       // set args
                preparedStatement.setInt(6, event.getBits());               // set bits
                preparedStatement.setInt(7, event.getSubMonths());          // set subMonths
                preparedStatement.setString(8, event.getSubTier().name());  // set subTier
                preparedStatement.executeUpdate(); // execute

                // Close resources
                preparedStatement.close();

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Log Command Response
    public void logResponse(TwitchMessageEvent event, String command, String response) {
        new Thread(() -> {

            // Variables
            var channelId = event.getChannelId();
            var userId = event.getUserId();
            var channel = event.getChannel();
            var user = event.getUser();

            try {
                if (!sql.isConnected()) sql.connect(); // connect

                // Check Channel and User
                sql.checkCache(userId, user, false);
                sql.checkCache(channelId, channel, true);

                // Prepare statement
                PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                        "INSERT INTO ResponseLog (timestamp, channelId, userId, command, args, response, bits, subMonths, subTier) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                );

                // Set values and execute
                preparedStatement.setTimestamp(1, event.getTimestamp());    // set timestamp
                preparedStatement.setInt(2, channelId);                     // set channel
                preparedStatement.setInt(3, userId);                        // set user
                preparedStatement.setString(4, command);                    // set command
                preparedStatement.setString(5, event.getMessage());         // set args
                preparedStatement.setString(6, response);                   // set response
                preparedStatement.setInt(7, event.getBits());               // set bits
                preparedStatement.setInt(8, event.getSubMonths());          // set subMonths
                preparedStatement.setString(9, event.getSubTier().name());  // set subTier
                preparedStatement.executeUpdate(); // execute

                // Close resources
                preparedStatement.close();

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Log Bot Response
    public void logResponse(String channel, String user, String message, HelixHandler helixHandler) {
        new Thread(() -> {

            // Variables
            var channelId = helixHandler.getUser(channel).getId();
            var userId = helixHandler.getUser(user).getId();

            try {
                if (!sql.isConnected()) sql.connect(); // connect

                // Check Channel and User
                sql.checkCache(userId, user, false);
                sql.checkCache(channelId, channel, true);

                // Prepare statement
                PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                        "INSERT INTO ResponseLog (timestamp, channelId, userId, command, args, response) VALUES (?, ?, ?, ?, ?, ?)"
                );

                // Set values and execute
                preparedStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis())); // set timestamp
                preparedStatement.setInt(2, channelId);     // set channel
                preparedStatement.setInt(3, userId);        // set user
                preparedStatement.setString(4, USER);       // set command
                preparedStatement.setString(5, USER);       // set args
                preparedStatement.setString(6, message);    // set response
                preparedStatement.executeUpdate(); // execute

                // Close resources
                preparedStatement.close();

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    public void logRole(TwitchRoleEvent event) {
        new Thread(() -> {

            // Variables
            var channelId = event.getChannelId();
            var userId = event.getUserId();

            // Log to console
            System.out.println(event.getLog());

            try {
                if (!sql.isConnected()) sql.connect(); // connect

                // Check Channel and User
                sql.checkCache(userId, event.getUser(), false);
                sql.checkCache(channelId, event.getChannel(), true);

                // Prepare statement
                PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                        "INSERT INTO RoleLog (timestamp, channelId, userId, role, added) VALUES (?, ?, ?, ?, ?)"
                );

                // Set values and execute
                preparedStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis())); // set timestamp
                preparedStatement.setInt(2, channelId);                 // set channel
                preparedStatement.setInt(3, userId);                    // set user
                preparedStatement.setString(4, event.getRole().name()); // set role
                preparedStatement.setInt(5, event.isAdded() ? 1 : 0);   // set added
                preparedStatement.executeUpdate(); // execute

                // Close resources
                preparedStatement.close();

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    public void logLoyalty(ChannelFollowEvent event) {
        new Thread(() -> {

            // Variables
            var channelId = Integer.parseInt(event.getBroadcasterUserId());
            var userId = Integer.parseInt(event.getUserId());
            var channel = event.getBroadcasterUserName();
            var user = event.getUserName();

            // Log to console
            System.out.printf("%s %s <%s> %s: Followed%n", getFormattedTimestamp(), LoyaltyType.FOLLOW.getTag(), channel, user);

            try {
                if (!sql.isConnected()) sql.connect(); // connect

                // Check Channel and User
                sql.checkCache(userId, user, false);
                sql.checkCache(channelId, channel, true);

                // Prepare statement
                PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                        "INSERT INTO LoyaltyLog (timestamp, channelId, userId, type) VALUES (?, ?, ?, ?)"
                );

                // Set values and execute
                preparedStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis())); // set timestamp
                preparedStatement.setInt(2, channelId);                     // set channel
                preparedStatement.setInt(3, userId);                        // set user
                preparedStatement.setString(4, LoyaltyType.FOLLOW.name());  // set type
                preparedStatement.executeUpdate(); // execute

                // Close resources
                preparedStatement.close();

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    public void logLoyalty(ChannelSubscribeEvent event) {
        new Thread(() -> {

            // Variables
            var channelId = Integer.parseInt(event.getBroadcasterUserId());
            var userId = Integer.parseInt(event.getUserId());
            var channel = event.getBroadcasterUserName();
            var user = event.getUserName();
            var tier = event.getTier().ordinalName().toUpperCase();

            // Log to console
            System.out.printf("%s %s <%s> %s: Subscribed with %s%n", getFormattedTimestamp(), LoyaltyType.SUBSCRIPTION.getTag(), channel, user, tier);

            try {
                if (!sql.isConnected()) sql.connect(); // connect

                // Check Channel and User
                sql.checkCache(userId, user, false);
                sql.checkCache(channelId, channel, true);

                // Prepare statement
                PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                        "INSERT INTO LoyaltyLog (timestamp, channelId, userId, type, subTier) VALUES (?, ?, ?, ?, ?)"
                );

                // Set values and execute
                preparedStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis())); // set timestamp
                preparedStatement.setInt(2, channelId);                             // set channel
                preparedStatement.setInt(3, userId);                                // set user
                preparedStatement.setString(4, LoyaltyType.SUBSCRIPTION.name());    // set type
                preparedStatement.setString(5, tier);                               // set tier
                preparedStatement.executeUpdate(); // execute

                // Close resources
                preparedStatement.close();

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    public void logLoyalty(ChannelSubscriptionGiftEvent event) {
        new Thread(() -> {

            // Variables
            var channelId = Integer.parseInt(event.getBroadcasterUserId());
            var userId = Integer.parseInt(event.getUserId());
            var channel = event.getBroadcasterUserName();
            var user = event.getUserName();
            var giftAmount = event.getTotal();
            var giftTotal = event.getCumulativeTotal() == null ? 0 : event.getCumulativeTotal();
            var tier = event.getTier().ordinalName().toUpperCase();

            // Log to console
            System.out.printf("%s %s <%s> %s: Gifted %d %s subs and has gifted %d subs in total%n",
                    getFormattedTimestamp(),
                    LoyaltyType.GIFT.getTag(),
                    channel,
                    user,
                    giftAmount,
                    tier,
                    giftTotal
            );

            try {
                if (!sql.isConnected()) sql.connect(); // connect

                // Check Channel and User
                sql.checkCache(userId, user, false);
                sql.checkCache(channelId, channel, true);

                // Prepare statement
                PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                        "INSERT INTO LoyaltyLog (timestamp, channelId, userId, type, subTier, giftAmount, giftTotal) VALUES (?, ?, ?, ?, ?, ?, ?)"
                );

                // Set values and execute
                preparedStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis())); // set timestamp
                preparedStatement.setInt(2, channelId);                     // set channel
                preparedStatement.setInt(3, userId);                        // set user
                preparedStatement.setString(4, LoyaltyType.GIFT.name());    // set type
                preparedStatement.setString(5, tier);                       // set tier
                preparedStatement.setInt(6, giftAmount);                    // set giftAmount
                preparedStatement.setInt(7, giftTotal);                     // set giftTotal
                preparedStatement.executeUpdate(); // execute

                // Close resources
                preparedStatement.close();

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    public void logRaid(RaidEvent event) {
        new Thread(() -> {

            // Variables
            EventChannel channel = event.getChannel();
            EventUser raider = event.getRaider();
            var channelId = Integer.parseInt(channel.getId());
            var raiderId = Integer.parseInt(raider.getId());
            String channelName = channel.getName().toLowerCase();
            String raiderName = raider.getName().toLowerCase();
            var viewer = event.getViewers();

            // Log to console
            System.out.printf("%s %s <%s> by %s with %d viewers%n", getFormattedTimestamp(), RAID, channelName, raiderName, viewer);

            try {
                if (!sql.isConnected()) sql.connect(); // connect

                // Check Channel and User
                sql.checkCache(raiderId, raiderName, false);
                sql.checkCache(channelId, channelName, true);

                // Prepare statement
                PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                        "INSERT INTO RaidLog (timestamp, channelId, raiderId, viewerAmount) VALUES (?, ?, ?, ?)"
                );

                // Set values and execute
                preparedStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis())); // set timestamp
                preparedStatement.setInt(2, channelId); // set channel
                preparedStatement.setInt(3, raiderId);  // set raider
                preparedStatement.setInt(4, viewer);    // set viewerAmount
                preparedStatement.executeUpdate(); // execute

                // Close resources
                preparedStatement.close();

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    public void logTTS(TwitchMessageEvent event, AudioFile audioFile) {
        new Thread(() -> {

            // Variables
            var channelId = event.getChannelId();
            var userId = event.getUserId();
            var channel = event.getChannel();
            var user = event.getUser();

            try {
                if (!sql.isConnected()) sql.connect(); // connect

                // Check Channel and User
                sql.checkCache(userId, user, false);
                sql.checkCache(channelId, channel, true);

                // Prepare statement
                PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                        "INSERT INTO " + "TTSLog" + " (timestamp, channelId, userId, message, audioData, bits, subMonths, subTier) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                );

                // Set values and execute
                preparedStatement.setTimestamp(1, event.getTimestamp());    // set timestamp
                preparedStatement.setInt(2, channelId);                     // set channel
                preparedStatement.setInt(3, userId);                        // set user
                preparedStatement.setString(4, event.getMessage());         // set message
                preparedStatement.setBytes(5, audioFile.getAudioData());    // set audioData
                preparedStatement.setInt(6, event.getBits());               // set bits
                preparedStatement.setInt(7, event.getSubMonths());          // set subMonths
                preparedStatement.setString(8, event.getSubTier().name());  // set subTier
                preparedStatement.executeUpdate(); // execute

                // Close resources
                preparedStatement.close();

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }
}