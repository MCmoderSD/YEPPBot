package de.MCmoderSD.database.manager;

import de.MCmoderSD.database.Database;
import de.MCmoderSD.objects.MessageEvent;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;

import static de.MCmoderSD.utilities.FormatUUID.asBytes;
import static de.MCmoderSD.utilities.Hasher.xxHash64;

public class CommandManager {

    // Associations
    private final Database database;

    // Attributes
    private final Connection connection;

    // Constructor
    public CommandManager(Database database) {

        // Check Parameters
        if (database == null) throw new IllegalArgumentException("Database cannot be null");

        // Set Associations
        this.database = database;

        // Set Attributes
        connection = database.getConnection();
    }

    public void logResponse(MessageEvent event, String command, String response) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (event == null) throw new IllegalArgumentException("MessageEvent cannot be null");
                if (command == null || command.isBlank()) throw new IllegalArgumentException("Invalid command");
                if (response == null || response.isBlank()) throw new IllegalArgumentException("Invalid response");

                // Variables
                byte[] contentHash = xxHash64(response);                            // Response Content Hash
                byte[] uuid = asBytes(UUID.randomUUID());                           // Random UUID
                Timestamp firedAt = new Timestamp(System.currentTimeMillis());      // Current Timestamp

                // Insert message content
                PreparedStatement insertContentStatement = connection.prepareStatement(
                        "INSERT IGNORE INTO MessageContent (hash, content) VALUES (?, ?);"
                );

                // Set the insert values
                insertContentStatement.setBytes(1, contentHash);    // Content Hash
                insertContentStatement.setString(2, response);      // Response Content

                // Execute the statement
                insertContentStatement.executeUpdate();

                // Close the statement
                insertContentStatement.close();

                // Ensure the original message is logged
                database.getEventLogManager().waitTillMessageLogged(event.getId(), 10);

                // Insert message event
                PreparedStatement insertEventStatement = connection.prepareStatement(
                        "INSERT INTO ResponseMessage (id, firedAt, channelId, userId, command, content, messageId) VALUES (?, ?, ?, ?, ?, ?, ?);"
                );

                // Set the insert values
                insertEventStatement.setBytes(1, uuid);                     // Response ID
                insertEventStatement.setTimestamp(2, firedAt);              // Fired At
                insertEventStatement.setInt(3, event.getChannel().getId()); // Channel ID
                insertEventStatement.setInt(4, event.getUser().getId());    // User ID
                insertEventStatement.setString(5, command);                 // Command
                insertEventStatement.setBytes(6, contentHash);              // Response Content Hash
                insertEventStatement.setBytes(7, asBytes(event.getId()));   // Message Event ID

                // Execute the statement
                insertEventStatement.executeUpdate();

                // Close the statement
                insertEventStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to log Response Message: " + e.getMessage(), e);
            }
        }).start();
    }

    public void logCommand(MessageEvent event, String command, ArrayList<String> args) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (event == null) throw new IllegalArgumentException("MessageEvent cannot be null");
                if (command == null || command.isBlank()) throw new IllegalArgumentException("Invalid command");
                if (args == null) throw new IllegalArgumentException("Arguments cannot be null");
                for (var arg : args) if (arg == null || arg.isBlank()) throw new IllegalArgumentException("Arguments cannot be null");

                // Variables
                String argsJoined = String.join(" ", args);
                byte[] argsHash = xxHash64(argsJoined);
                Timestamp firedAt = new Timestamp(System.currentTimeMillis());

                // Insert command arguments
                PreparedStatement insertArgsStatement = connection.prepareStatement(
                        "INSERT IGNORE INTO MessageContent (hash, content) VALUES (?, ?);"
                );

                // Set the insert values
                insertArgsStatement.setBytes(1, argsHash);        // Content Hash
                insertArgsStatement.setString(2, argsJoined);     // Args Content

                // Execute the statement
                insertArgsStatement.executeUpdate();

                // Close the statement
                insertArgsStatement.close();

                // Ensure the original message is logged
                database.getEventLogManager().waitTillMessageLogged(event.getId(), 10);

                // Insert command event
                PreparedStatement insertEventStatement = connection.prepareStatement(
                        "INSERT INTO CommandLog (messageId, firedAt, channelId, userId, command, args) VALUES (?, ?, ?, ?, ?, ?);"
                );

                // Set the insert values
                insertEventStatement.setBytes(1, asBytes(event.getId()));       // Message Event ID
                insertEventStatement.setTimestamp(2, firedAt);                  // Fired At
                insertEventStatement.setInt(3, event.getChannel().getId());     // Channel ID
                insertEventStatement.setInt(4, event.getUser().getId());        // User ID
                insertEventStatement.setString(5, command);                     // Command
                insertEventStatement.setBytes(6, argsHash);                     // Arguments Hash

                // Execute the statement
                insertEventStatement.executeUpdate();

                // Close the statement
                insertEventStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to log Command: " + e.getMessage(), e);
            }
        }).start();
    }
}