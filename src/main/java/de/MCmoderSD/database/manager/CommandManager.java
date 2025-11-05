package de.MCmoderSD.database.manager;

import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.database.Database;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
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

    private boolean waitTillMessageLogged(UUID messageId, int attemptsLeft) {
        try {

            // Prepare the query
            PreparedStatement checkStatement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM MessageEvent WHERE id = ?;"
            );

            // Set the query parameter
            checkStatement.setBytes(1, asBytes(messageId));

            // Execute the query
            var resultSet = checkStatement.executeQuery();
            resultSet.next();
            var count = resultSet.getInt(1);

            // Close resources
            resultSet.close();
            checkStatement.close();

            // Return whether the message is already logged
            if (count == 0 && attemptsLeft > 0) return waitTillMessageLogged(messageId, attemptsLeft - 1);
            return count > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check if message is already logged: " + e.getMessage(), e);
        }
    }

    public void logResponse(MessageEvent event, String command, String response) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (event == null) throw new IllegalArgumentException("MessageEvent cannot be null");
                if (command == null || command.isBlank()) throw new IllegalArgumentException("Invalid command");
                if (response == null || response.isBlank()) throw new IllegalArgumentException("Invalid response");

                // Variables
                byte[] contentHash = xxHash64(response);

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
                waitTillMessageLogged(event.getId(), 10);

                // Insert message event
                PreparedStatement insertEventStatement = connection.prepareStatement(
                        "INSERT INTO ResponseMessage (id, firedAt, channelId, userId, command, content, messageId) VALUES (?, ?, ?, ?, ?, ?, ?);"
                );

                // Set the insert values
                insertEventStatement.setBytes(1, asBytes(UUID.randomUUID()));                       // Response ID
                insertEventStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));    // Fired At
                insertEventStatement.setInt(3, event.getChannel().getId());                         // Channel ID
                insertEventStatement.setInt(4, event.getUser().getId());                            // User ID
                insertEventStatement.setString(5, command);                                         // Command
                insertEventStatement.setBytes(6, contentHash);                                      // Response Content Hash
                insertEventStatement.setBytes(7, asBytes(event.getId()));                           // Message Event ID

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
                waitTillMessageLogged(event.getId(), 10);

                // Insert command event
                PreparedStatement insertEventStatement = connection.prepareStatement(
                        "INSERT INTO CommandLog (messageId, firedAt, channelId, userId, command, args) VALUES (?, ?, ?, ?, ?, ?);"
                );

                // Set the insert values
                insertEventStatement.setBytes(1, asBytes(event.getId()));                           // Message Event ID
                insertEventStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));    // Fired At
                insertEventStatement.setInt(3, event.getChannel().getId());                         // Channel ID
                insertEventStatement.setInt(4, event.getUser().getId());                            // User ID
                insertEventStatement.setString(5, command);                                         // Command
                insertEventStatement.setBytes(6, argsHash);                                         // Arguments Hash

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