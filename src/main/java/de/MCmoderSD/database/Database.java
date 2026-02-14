package de.MCmoderSD.database;

import de.MCmoderSD.database.manager.*;
import de.MCmoderSD.enums.ImageFormat;
import de.MCmoderSD.enums.UserImageType;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.sql.Driver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import static de.MCmoderSD.enums.ImageFormat.getFormat;
import static de.MCmoderSD.utilities.FormatUUID.*;
import static de.MCmoderSD.enums.UserImageType.*;
import static de.MCmoderSD.tools.GZIP.*;
import static java.util.UUID.fromString;

public class Database extends Driver {

    // Managers
    private final ChannelManager channelManager;
    private final MessageManager messageManager;
    private final CommandManager commandManager;
    private final EventLogManager eventLogManager;
    private final BirthdayManager birthdayManager;
    private final LurkManager lurkManager;
    private final QueueManager queueManager;
    private final QuoteManager quoteManager;
    private final OpenAIManger openAIManger;

    // Constructor
    public Database(Builder builder) {

        // Initialize Driver
        super(builder);

        // Connect to Database
        setAutoReconnect(true);
        connect();

        // Load Table Statements
        ArrayList<String> userTable = loadTables("database/UserTable.sql");
        ArrayList<String> channelTable = loadTables("database/ChannelTable.sql");
        ArrayList<String> messages = loadTables("database/Messages.sql");
        ArrayList<String> ratingTable = loadTables("database/RatingTable.sql");
        ArrayList<String> events = loadTables("database/Events.sql");
        ArrayList<String> birthday = loadTables("database/BirthdayTable.sql");
        ArrayList<String> lurker = loadTables("database/Lurker.sql");
        ArrayList<String> openAI = loadTables("database/OpenAI.sql");
        ArrayList<String> queue = loadTables("database/QueueTable.sql");
        ArrayList<String> quotes = loadTables("database/QuoteTable.sql");

        // Initialize Tables
        initTables(userTable);      // User & UserImage Tables
        initTables(channelTable);   // Channel & Blacklist Tables               | needs UserTable
        initTables(messages);       // Message, Response & Command Log Tables   | needs UserTable
        initTables(ratingTable);    // Rating, Flag & Score Tables              | needs Messages
        initTables(events);         // Raid & Follow Table                      | needs UserTable
        initTables(birthday);       // Birthday Table                           | needs UserTable
        initTables(lurker);         // Lurker Table                             | needs UserTable
        initTables(openAI);         // Conversation Table                       | needs UserTable
        initTables(queue);          // Queue Table                              | needs ChannelTable
        initTables(quotes);         // Quote Table                              | needs ChannelTable

        // Initialize Managers
        channelManager = new ChannelManager(this);
        messageManager = new MessageManager(this);
        commandManager = new CommandManager(this);
        eventLogManager = new EventLogManager(this);
        birthdayManager = new BirthdayManager(this);
        lurkManager = new LurkManager(this);
        queueManager = new QueueManager(this);
        quoteManager = new QuoteManager(this);
        openAIManger = new OpenAIManger(this);
    }

    private static ArrayList<String> loadTables(String path) {

        // Check Parameters
        if (path == null || path.isBlank()) throw new IllegalArgumentException("Path cannot be null or blank");
        var resource = Database.class.getClassLoader().getResourceAsStream(path);
        if (resource == null) throw new IllegalArgumentException("Resource not found at path: " + path);

        try (var bis = new BufferedInputStream(resource)) {

            // Load Data
            byte[] data = bis.readAllBytes();
            String content = new String(data);
            String[] statements = content.split(";");

            // Prepare Table Names
            ArrayList<String> tables = new ArrayList<>();
            for (var statement : statements) {
                if (statement == null || statement.isBlank()) continue;
                tables.add(statement.trim() + ";");
            }

            // Return Tables
            return tables;

        } catch (IOException e) {
            throw new RuntimeException("Failed to load tables from path: " + path + ": " + e.getMessage(), e);
        }
    }

    private void initTables(ArrayList<String> tables) {
        if (tables == null || tables.isEmpty()) throw new IllegalArgumentException("Tables cannot be null or empty");
        for (var table : tables) if (table == null || table.isBlank()) throw new IllegalArgumentException("Table statement cannot be null or blank");
        try {
            for (var table : tables) {
                PreparedStatement preparedStatement = connection.prepareStatement(table);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize tables: " + e.getMessage(), e);
        }
    }

    private void checkImage(TwitchUser user, String imageUrl, UserImageType imageType) {
        new Thread(() -> {
            try {

                // Image UUID
                var uuid = asBytes(fromString(imageUrl.substring(47, 83)));

                // Check if image is already downloaded
                PreparedStatement checkStatement = connection.prepareStatement(
                        "SELECT COUNT(uuid) AS count FROM UserImage WHERE uuid = ?;"
                );

                // Set the query value
                checkStatement.setBytes(1, uuid);   // Image UUID

                // Execute the query
                var resultSet = checkStatement.executeQuery();

                // If image exists, return
                if (resultSet.next() && resultSet.getInt(1) == 1) return;

                // Close resources
                checkStatement.close();
                resultSet.close();

                // Variables
                byte[] imageData;
                byte[] compressedData;
                ImageFormat imageFormat = getFormat(imageUrl);

                // Download Image
                try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new URI(imageUrl).toURL().openStream())) {
                    imageData = bufferedInputStream.readAllBytes();
                } catch (IOException | URISyntaxException e) {
                    throw new IOException("Failed to download image from URL: " + imageUrl, e);
                }

                // Compress Image
                compressedData = deflate(imageData);

                // Image Sizes
                var uncompressed = imageData.length;
                var compressed = compressedData.length;

                // Insert into database
                PreparedStatement insertStatement = connection.prepareStatement(
                        "INSERT IGNORE INTO UserImage (uuid, id, url, size, totalSize, type, format, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?);"
                );

                // Set the insert values
                insertStatement.setBytes(1, uuid);                  // Image UUID
                insertStatement.setInt(2, user.getId());            // User ID
                insertStatement.setString(3, imageUrl);             // Image URL
                insertStatement.setInt(4, compressed);              // Compressed Size
                insertStatement.setInt(5, uncompressed);            // Uncompressed Size
                insertStatement.setString(6, imageType.name());     // Image Type (Profile or Offline)
                insertStatement.setString(7, imageFormat.name());   // Image Format (JPEG, PNG, GIF)
                insertStatement.setBytes(8, compressedData);        // Image Data (compressed)

                // Execute the statement
                insertStatement.executeUpdate();

                // Close the statement
                insertStatement.close();

            } catch (SQLException | IOException e) {
                throw new RuntimeException("Failed to check profile image for TwitchUser with id " + user.getId() + ": " + e.getMessage(), e);
            }
        }).start();
    }

    public void addTwitchUser(TwitchUser user) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (user == null) throw new IllegalArgumentException("TwitchUser cannot be null");

                // Variables
                byte[] data = deflateObject(user);
                String profileImageUrl = user.getProfileImageUrl();
                String offlineImageUrl = user.getOfflineImageUrl();

                // Prepare the SQL statement
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO User (id, username, displayName, type, broadcasterType, user) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE username = ?, displayName = ?, type = ?, broadcasterType = ?, user = ?;"
                );

                // Set the insert values
                preparedStatement.setInt(1, user.getId());                          // Twitch User ID
                preparedStatement.setString(2, user.getUsername());                 // Twitch Username
                preparedStatement.setString(3, user.getDisplayName());              // Twitch Display Name
                preparedStatement.setString(4, user.getType().name());              // Type
                preparedStatement.setString(5, user.getBroadcasterType().name());   // Broadcaster Type
                preparedStatement.setBytes(6, data);                                // TwitchUser Object (compressed)

                // Set the update values
                preparedStatement.setString(7, user.getUsername());                 // Twitch Username
                preparedStatement.setString(8, user.getDisplayName());              // Twitch Display Name
                preparedStatement.setString(9, user.getType().name());              // Type
                preparedStatement.setString(10, user.getBroadcasterType().name());  // Broadcaster Type
                preparedStatement.setBytes(11, data);                               // TwitchUser Object (compressed)

                // Execute the statement
                preparedStatement.executeUpdate();

                // Close the statement
                preparedStatement.close();

                // Check profile image
                if (profileImageUrl != null && validUUID(profileImageUrl.substring(47, 83))) checkImage(user, profileImageUrl, PROFILE);
                if (offlineImageUrl != null && validUUID(offlineImageUrl.substring(47, 83))) checkImage(user, offlineImageUrl, OFFLINE);

            } catch (SQLException | IOException e) {
                throw new RuntimeException("Failed to add or update TwitchUser with id " + user.getId() + ": " + e.getMessage(), e);
            }
        }).start();
    }

    // Getter
    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public EventLogManager getEventLogManager() {
        return eventLogManager;
    }

    public BirthdayManager getBirthdayManager() {
        return birthdayManager;
    }

    public LurkManager getLurkManager() {
        return lurkManager;
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public QuoteManager getQuoteManager() {
        return quoteManager;
    }

    public OpenAIManger getOpenAIManger() {
        return openAIManger;
    }
}