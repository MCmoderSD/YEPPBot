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
import de.MCmoderSD.openai.objects.Rating;
import de.MCmoderSD.utilities.database.SQL;

import java.io.IOException;
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

            // SQL statement for creating the rating table
            connection.prepareStatement(condition +
                    """
                    Rating (
                    id VARCHAR(32) PRIMARY KEY,
                    flagged BIT NOT NULL DEFAULT FALSE,
                    rating BLOB NOT NULL
                    ) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=1 CHARSET=utf8mb4
                    """
            ).execute();

            // SQL statement for creating the rating flags table
            connection.prepareStatement(condition +
                    """
                    RatingFlags (
                    id VARCHAR(32) PRIMARY KEY,
                    harassment BIT NOT NULL DEFAULT FALSE,
                    harassmentThreatening BIT NOT NULL DEFAULT FALSE,
                    hate BIT NOT NULL DEFAULT FALSE,
                    hateThreatening BIT NOT NULL DEFAULT FALSE,
                    illicit BIT NOT NULL DEFAULT FALSE,
                    illicitViolent BIT NOT NULL DEFAULT FALSE,
                    selfHarm BIT NOT NULL DEFAULT FALSE,
                    selfHarmInstructions BIT NOT NULL DEFAULT FALSE,
                    selfHarmIntent BIT NOT NULL DEFAULT FALSE,
                    sexual BIT NOT NULL DEFAULT FALSE,
                    sexualMinors BIT NOT NULL DEFAULT FALSE,
                    violence BIT NOT NULL DEFAULT FALSE,
                    violenceGraphic BIT NOT NULL DEFAULT FALSE
                    ) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=1 CHARSET=utf8mb4
                    """
            ).execute();

            // SQL statement for creating the rating scores table
            connection.prepareStatement(condition +
                    """
                    RatingScores (
                    id VARCHAR(32) PRIMARY KEY,
                    harassment DOUBLE NOT NULL DEFAULT 0 CHECK (harassment >= 0 AND harassment <= 1),
                    harassmentThreatening DOUBLE NOT NULL DEFAULT 0 CHECK (harassmentThreatening >= 0 AND harassmentThreatening <= 1),
                    hate DOUBLE NOT NULL DEFAULT 0 CHECK (hate >= 0 AND hate <= 1),
                    hateThreatening DOUBLE NOT NULL DEFAULT 0 CHECK (hateThreatening >= 0 AND hate <= 1),
                    illicit DOUBLE NOT NULL DEFAULT 0 CHECK (illicit >= 0 AND illicit <= 1),
                    illicitViolent DOUBLE NOT NULL DEFAULT 0 CHECK (illicitViolent >= 0 AND illicitViolent <= 1),
                    selfHarm DOUBLE NOT NULL DEFAULT 0 CHECK (selfHarm >= 0 AND selfHarm <= 1),
                    selfHarmInstructions DOUBLE NOT NULL DEFAULT 0 CHECK (selfHarmInstructions >= 0 AND selfHarmInstructions <= 1),
                    selfHarmIntent DOUBLE NOT NULL DEFAULT 0 CHECK (selfHarmIntent >= 0 AND selfHarmIntent <= 1),
                    sexual DOUBLE NOT NULL DEFAULT 0 CHECK (sexual >= 0 AND sexual <= 1),
                    sexualMinors DOUBLE NOT NULL DEFAULT 0 CHECK (sexualMinors >= 0 AND sexualMinors <= 1),
                    violence DOUBLE NOT NULL DEFAULT 0 CHECK (violence >= 0 AND violence <= 1),
                    violenceGraphic DOUBLE NOT NULL DEFAULT 0 CHECK (violenceGraphic >= 0 AND violenceGraphic <= 1)
                    ) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=1 CHARSET=utf8mb4
                    """
            ).execute();

            // SQL statement for creating the event log table
            connection.prepareStatement(condition +
                    """
                    EventLog (
                    id VARCHAR (36) PRIMARY KEY,
                    ratingId VARCHAR(32) UNIQUE,
                    event LONGBLOB NOT NULL,
                    FOREIGN KEY (ratingId) REFERENCES Rating(id)
                    ) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=1 CHARSET=utf8mb4
                    """
            ).execute();

            // SQL statement for creating the message log table
            connection.prepareStatement(condition +
                """
                MessageLog (
                id VARCHAR (36) PRIMARY KEY,
                timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                channelId INT NOT NULL,
                userId INT NOT NULL,
                message VARCHAR(500),
                bits INT NOT NULL DEFAULT 0,
                subMonths INT NOT NULL DEFAULT 0,
                subTier ENUM('NONE', 'TIER1', 'TIER2', 'TIER3', 'PRIME') NOT NULL DEFAULT 'NONE',
                FOREIGN KEY (id) REFERENCES EventLog(id),
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
            PreparedStatement eventStatement = sql.getConnection().prepareStatement(
                    "INSERT INTO EventLog (id, event) VALUES (?, ?)"
            );

            // Set values and execute
            eventStatement.setString(1, event.getEventId());    // set messageId
            eventStatement.setBytes(2, event.getBytes());       // set event
            eventStatement.executeUpdate(); // execute

            // Prepare statement
            PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                    "INSERT INTO MessageLog (id, timestamp, channelId, userId, message, bits, subMonths, subTier) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            );

            // Set values and execute
            preparedStatement.setString(1, event.getEventId());         // set messageId
            preparedStatement.setTimestamp(2, event.getTimestamp());    // set timestamp
            preparedStatement.setInt(3, channelId);                     // set channel
            preparedStatement.setInt(4, userId);                        // set user
            preparedStatement.setString(5, event.getMessage());         // set message
            preparedStatement.setInt(6, event.getBits());               // set bits
            preparedStatement.setInt(7, event.getSubMonths());          // set subMonths
            preparedStatement.setString(8, event.getSubTier().name());  // set subTier
            preparedStatement.executeUpdate(); // execute

            // Close resources
            eventStatement.close();
            preparedStatement.close();

        } catch (SQLException | IOException e) {
            System.err.println(e.getMessage());
        }
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

    public void linkRating(String eventId, String ratingId) {
        try {
            if (!sql.isConnected()) sql.connect(); // connect

            // Prepare statement
            PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                    "UPDATE EventLog SET ratingId = ? WHERE id = ?"
            );

            // Set values and execute
            preparedStatement.setString(1, ratingId); // set ratingId
            preparedStatement.setString(2, eventId);  // set eventId
            preparedStatement.executeUpdate(); // execute

            // Close resources
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void addRating(String id, Rating rating) {
        try {
            if (!sql.isConnected()) sql.connect(); // connect

            // Variables
            byte[] ratingBytes = rating.getBytes();

            // Rating Flags
            Rating.Flag harassment = rating.getHarassment();
            Rating.Flag harassmentThreatening = rating.getHarassmentThreatening();
            Rating.Flag hate = rating.getHate();
            Rating.Flag hateThreatening = rating.getHateThreatening();
            Rating.Flag illicit = rating.getIllicit();
            Rating.Flag illicitViolent = rating.getIllicitViolent();
            Rating.Flag selfHarm = rating.getSelfHarm();
            Rating.Flag selfHarmInstructions = rating.getSelfHarmInstructions();
            Rating.Flag selfHarmIntent = rating.getSelfHarmIntent();
            Rating.Flag sexual = rating.getSexual();
            Rating.Flag sexualMinors = rating.getSexualMinors();
            Rating.Flag violence = rating.getViolence();
            Rating.Flag violenceGraphic = rating.getViolenceGraphic();

            // Prepared Statements
            PreparedStatement ratingStatement;
            PreparedStatement flagsStatement;
            PreparedStatement scoresStatement;

            // Insert Rating or Update
            ratingStatement = sql.getConnection().prepareStatement(
                    "INSERT INTO Rating (id, flagged, rating) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE flagged = ?, rating = ?"
            );

            // Insert Rating Flags or Update
            flagsStatement = sql.getConnection().prepareStatement(
                    """
                            INSERT INTO RatingFlags (id, harassment, harassmentThreatening, hate, hateThreatening, illicit, illicitViolent, selfHarm, selfHarmInstructions, selfHarmIntent, sexual, sexualMinors, violence, violenceGraphic)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            ON DUPLICATE KEY UPDATE harassment = ?, harassmentThreatening = ?, hate = ?, hateThreatening = ?, illicit = ?, illicitViolent = ?, selfHarm = ?, selfHarmInstructions = ?, selfHarmIntent = ?, sexual = ?, sexualMinors = ?, violence = ?, violenceGraphic = ?
                            """
            );

            // Insert Rating Flags or Update
            scoresStatement = sql.getConnection().prepareStatement(
                    """
                            INSERT INTO RatingScores (id, harassment, harassmentThreatening, hate, hateThreatening, illicit, illicitViolent, selfHarm, selfHarmInstructions, selfHarmIntent, sexual, sexualMinors, violence, violenceGraphic)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            ON DUPLICATE KEY UPDATE harassment = ?, harassmentThreatening = ?, hate = ?, hateThreatening = ?, illicit = ?, illicitViolent = ?, selfHarm = ?, selfHarmInstructions = ?, selfHarmIntent = ?, sexual = ?, sexualMinors = ?, violence = ?, violenceGraphic = ?
                            """
            );

            // Rating
            ratingStatement.setString(1, id);                                   // set id
            ratingStatement.setBoolean(2, rating.isFlagged());                  // set flagged
            ratingStatement.setBytes(3, ratingBytes);                           // set rating
            ratingStatement.setBoolean(4, rating.isFlagged());                  // set flagged
            ratingStatement.setBytes(5, ratingBytes);                           // set rating

            // Flags
            flagsStatement.setString(1, id);                                    // set id
            flagsStatement.setBoolean(2, harassment.flagged());                 // set harassment
            flagsStatement.setBoolean(3, harassmentThreatening.flagged());      // set harassmentThreatening
            flagsStatement.setBoolean(4, hate.flagged());                       // set hate
            flagsStatement.setBoolean(5, hateThreatening.flagged());            // set hateThreatening
            flagsStatement.setBoolean(6, illicit.flagged());                    // set illicit
            flagsStatement.setBoolean(7, illicitViolent.flagged());             // set illicitViolent
            flagsStatement.setBoolean(8, selfHarm.flagged());                   // set selfHarm
            flagsStatement.setBoolean(9, selfHarmInstructions.flagged());       // set selfHarmInstructions
            flagsStatement.setBoolean(10, selfHarmIntent.flagged());            // set selfHarmIntent
            flagsStatement.setBoolean(11, sexual.flagged());                    // set sexual
            flagsStatement.setBoolean(12, sexualMinors.flagged());              // set sexualMinors
            flagsStatement.setBoolean(13, violence.flagged());                  // set violence
            flagsStatement.setBoolean(14, violenceGraphic.flagged());           // set violenceGraphic
            flagsStatement.setBoolean(15, harassment.flagged());                // set harassment
            flagsStatement.setBoolean(16, harassmentThreatening.flagged());     // set harassmentThreatening
            flagsStatement.setBoolean(17, hate.flagged());                      // set hate
            flagsStatement.setBoolean(18, hateThreatening.flagged());           // set hateThreatening
            flagsStatement.setBoolean(19, illicit.flagged());                   // set illicit
            flagsStatement.setBoolean(20, illicitViolent.flagged());            // set illicitViolent
            flagsStatement.setBoolean(21, selfHarm.flagged());                  // set selfHarm
            flagsStatement.setBoolean(22, selfHarmInstructions.flagged());      // set selfHarmInstructions
            flagsStatement.setBoolean(23, selfHarmIntent.flagged());            // set selfHarmIntent
            flagsStatement.setBoolean(24, sexual.flagged());                    // set sexual
            flagsStatement.setBoolean(25, sexualMinors.flagged());              // set sexualMinors
            flagsStatement.setBoolean(26, violence.flagged());                  // set violence
            flagsStatement.setBoolean(27, violenceGraphic.flagged());           // set violenceGraphic

            // Scores
            scoresStatement.setString(1, id);                                   // set id
            scoresStatement.setDouble(2, harassment.score());                   // set harassment
            scoresStatement.setDouble(3, harassmentThreatening.score());        // set harassmentThreatening
            scoresStatement.setDouble(4, hate.score());                         // set hate
            scoresStatement.setDouble(5, hateThreatening.score());              // set hateThreatening
            scoresStatement.setDouble(6, illicit.score());                      // set illicit
            scoresStatement.setDouble(7, illicitViolent.score());               // set illicitViolent
            scoresStatement.setDouble(8, selfHarm.score());                     // set selfHarm
            scoresStatement.setDouble(9, selfHarmInstructions.score());         // set selfHarmInstructions
            scoresStatement.setDouble(10, selfHarmIntent.score());              // set selfHarmIntent
            scoresStatement.setDouble(11, sexual.score());                      // set sexual
            scoresStatement.setDouble(12, sexualMinors.score());                // set sexualMinors
            scoresStatement.setDouble(13, violence.score());                    // set violence
            scoresStatement.setDouble(14, violenceGraphic.score());             // set violenceGraphic
            scoresStatement.setDouble(15, harassment.score());                  // set harassment
            scoresStatement.setDouble(16, harassmentThreatening.score());       // set harassmentThreatening
            scoresStatement.setDouble(17, hate.score());                        // set hate
            scoresStatement.setDouble(18, hateThreatening.score());             // set hateThreatening
            scoresStatement.setDouble(19, illicit.score());                     // set illicit
            scoresStatement.setDouble(20, illicitViolent.score());              // set illicitViolent
            scoresStatement.setDouble(21, selfHarm.score());                    // set selfHarm
            scoresStatement.setDouble(22, selfHarmInstructions.score());        // set selfHarmInstructions
            scoresStatement.setDouble(23, selfHarmIntent.score());              // set selfHarmIntent
            scoresStatement.setDouble(24, sexual.score());                      // set sexual
            scoresStatement.setDouble(25, sexualMinors.score());                // set sexualMinors
            scoresStatement.setDouble(26, violence.score());                    // set violence
            scoresStatement.setDouble(27, violenceGraphic.score());             // set violenceGraphic

            // Execute statements
            ratingStatement.executeUpdate(); // execute
            flagsStatement.executeUpdate();  // execute
            scoresStatement.executeUpdate(); // execute

            // Close resources
            ratingStatement.close();
            flagsStatement.close();
            scoresStatement.close();

        } catch (SQLException | IOException e) {
            System.err.println(e.getMessage());
        }
    }
}