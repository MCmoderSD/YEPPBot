package de.MCmoderSD.database;

import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.database.manager.*;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.sql.Driver;

import tools.jackson.databind.JsonNode;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.*;

import static de.MCmoderSD.tools.GZIP.*;
import static de.MCmoderSD.utilities.FormatUUID.*;
import static de.MCmoderSD.utilities.ImageDownloader.downloadImage;

public class Database extends Driver {

    // Associations
    private final TwitchBot twitchBot;

    // Managers
    private final ChannelManager channelManager;
    private final CommandManager commandManager;
    private final EventLogManager eventLogManager;
    private final BirthdayManager birthdayManager;
    private final LurkManager lurkManager;

    // Constructor
    public Database(DatabaseType databaseType, JsonNode config, TwitchBot twitchBot) {

        // Initialize Driver
        super(databaseType, config);

        // Check Parameters
        if (twitchBot == null) throw new IllegalArgumentException("TwitchBot cannot be null");

        // Set Associations
        this.twitchBot = twitchBot;

        // Load Tables
        var users = loadTables("database/Users.sql");
        var channels = loadTables("database/Channels.sql");
        var messages = loadTables("database/Messages.sql");
        var events = loadTables("database/Events.sql");
        var lurker = loadTables("database/Lurker.sql");

        // Initialize Tables
        initTables(users);
        initTables(channels);
        initTables(messages);
        initTables(events);
        initTables(lurker);

        // Initialize Managers
        channelManager = new ChannelManager(this);
        commandManager = new CommandManager(this);
        eventLogManager = new EventLogManager(this);
        birthdayManager = new BirthdayManager(this);
        lurkManager = new LurkManager(this);
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

    private void checkProfileImage(TwitchUser user, String profileImageUrl) {
        new Thread(() -> {
            try {

                // Extract UUID from URL
                var uuid = asBytes(UUID.fromString(profileImageUrl.substring(47, 83)));

                // Check if image is already downloaded
                PreparedStatement checkStatement = connection.prepareStatement(
                        "SELECT COUNT(uuid) AS count FROM ProfileImage WHERE uuid = ?;"
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

                // Download Image
                byte[] imageData = downloadImage(profileImageUrl);
                byte[] compressedData = deflateObject(imageData);

                // Variables
                var size = compressedData.length;
                var uncompressed = imageData.length;

                // Insert into database
                PreparedStatement insertStatement = connection.prepareStatement(
                        "INSERT INTO ProfileImage (uuid, id, url, size, uncompressed, image) VALUES (?, ?, ?, ?, ?, ?);"
                );

                // Set the insert values
                insertStatement.setBytes(1, uuid);              // Image UUID
                insertStatement.setInt(2, user.getId());        // User ID
                insertStatement.setString(3, profileImageUrl);  // Image URL
                insertStatement.setInt(4, size);                // Compressed Size
                insertStatement.setInt(5, uncompressed);        // Uncompressed Size
                insertStatement.setBytes(6, compressedData);    // Image Data (compressed)

                // Execute the statement
                insertStatement.executeUpdate();

                // Close the statement
                insertStatement.close();

            } catch (SQLException | IOException e) {
                throw new RuntimeException("Failed to check profile image for TwitchUser with id " + user.getId() + ": " + e.getMessage(), e);
            }
        }).start();
    }

    private void checkOfflineImage(TwitchUser user, String offlineImageUrl) {
        new Thread(() -> {
            try {

                // Extract UUID from URL
                var uuid = asBytes(UUID.fromString(offlineImageUrl.substring(47, 83)));

                // Check if image is already downloaded
                PreparedStatement checkStatement = connection.prepareStatement(
                        "SELECT COUNT(uuid) AS count FROM OfflineImage WHERE uuid = ?;"
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

                // Download Image
                byte[] imageData = downloadImage(offlineImageUrl);
                byte[] compressedData = deflateObject(imageData);

                // Variables
                var size = compressedData.length;
                var uncompressed = imageData.length;

                // Insert into database
                PreparedStatement insertStatement = connection.prepareStatement(
                        "INSERT INTO OfflineImage (uuid, id, url, size, uncompressed, image) VALUES (?, ?, ?, ?, ?, ?);"
                );

                // Set the insert values
                insertStatement.setBytes(1, uuid);              // Image UUID
                insertStatement.setInt(2, user.getId());        // User ID
                insertStatement.setString(3, offlineImageUrl);  // Image URL
                insertStatement.setInt(4, size);                // Compressed Size
                insertStatement.setInt(5, uncompressed);        // Uncompressed Size
                insertStatement.setBytes(6, compressedData);    // Image Data (compressed)

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
                if (profileImageUrl != null && validUUID(profileImageUrl.substring(47, 83))) checkProfileImage(user, profileImageUrl);
                if (offlineImageUrl != null && validUUID(offlineImageUrl.substring(47, 83))) checkOfflineImage(user, offlineImageUrl);

            } catch (SQLException | IOException e) {
                throw new RuntimeException("Failed to add or update TwitchUser with id " + user.getId() + ": " + e.getMessage(), e);
            }
        }).start();
    }

    // Getter
    public ChannelManager getChannelManager() {
        return channelManager;
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
}