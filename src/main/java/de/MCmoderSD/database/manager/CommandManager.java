package de.MCmoderSD.database.manager;

import de.MCmoderSD.database.Database;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.commands.blueprints.CustomCommand;
import de.MCmoderSD.objects.MessageEvent;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import static de.MCmoderSD.utilities.FormatUUID.asBytes;
import static de.MCmoderSD.utilities.Hasher.xxHash64;
import static de.MCmoderSD.utilities.ZipUtil.inflateTwitchUser;
import static java.sql.Types.BINARY;

public class CommandManager {

    // Associations
    private final Database database;

    // Constructor
    public CommandManager(Database database) {

        // Check Parameters
        if (database == null) throw new IllegalArgumentException("Database cannot be null");

        // Set Associations
        this.database = database;
    }

    // Log Bot Responses
    public void logResponse(MessageEvent event, String command, String response) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (event == null) throw new IllegalArgumentException("MessageEvent cannot be null");
                if (command == null || command.isBlank()) throw new IllegalArgumentException("Invalid command");
                if (response == null || response.isBlank()) throw new IllegalArgumentException("Invalid response");

                // Variables
                var contentHash = xxHash64(response);                       // Response Content Hash
                var uuid = asBytes(UUID.randomUUID());                      // Random UUID
                var firedAt = new Timestamp(System.currentTimeMillis());    // Current Timestamp

                // Ensure the original message is logged
                database.getEventLogManager().waitTillMessageLogged(event.getId(), 10);

                // Insert message event
                var insertEventStatement = database.getConnection().prepareStatement(
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

    // Log Command executions
    public void logCommand(MessageEvent event, String command, String args) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (event == null) throw new IllegalArgumentException("MessageEvent cannot be null");
                if (command == null || command.isBlank()) throw new IllegalArgumentException("Invalid command");
                if (args == null) throw new IllegalArgumentException("Arguments cannot be null");

                // Variables
                var firedAt = new Timestamp(System.currentTimeMillis());

                // Ensure the original message is logged
                database.getEventLogManager().waitTillMessageLogged(event.getId(), 10);

                // Insert command event
                var insertEventStatement = database.getConnection().prepareStatement(
                        "INSERT INTO CommandLog (messageId, firedAt, channelId, userId, command, args) VALUES (?, ?, ?, ?, ?, ?);"
                );

                // Set the insert values
                insertEventStatement.setBytes(1, asBytes(event.getId()));       // Message Event ID
                insertEventStatement.setTimestamp(2, firedAt);                  // Fired At
                insertEventStatement.setInt(3, event.getChannel().getId());     // Channel ID
                insertEventStatement.setInt(4, event.getUser().getId());        // User ID
                insertEventStatement.setString(5, command);                     // Command
                if (args.isBlank()) insertEventStatement.setNull(6, BINARY);    // Null if no arguments
                else insertEventStatement.setBytes(6, xxHash64(args));          // else Arguments Hash

                // Execute the statement
                insertEventStatement.executeUpdate();

                // Close the statement
                insertEventStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to log Command: " + e.getMessage(), e);
            }
        }).start();
    }

    // Get Custom Commands
    public void initCustomCommands() {
        new Thread(() -> {
            try {

                // Fetch commands from database
                var fetchCommandsStatement = database.getConnection().prepareStatement(
                        "SELECT * FROM CustomCommands c JOIN User u ON c.id = u.id;"
                );

                // Execute the statement
                var resultSet = fetchCommandsStatement.executeQuery();

                 while (resultSet.next()) {
                     var twitchBot = database.getTwitchBot();
                     var channel = inflateTwitchUser(resultSet.getBytes("user"));
                     new CustomCommand(twitchBot, channel, resultSet);
                 }

                // Close the statement and result set
                resultSet.close();
                fetchCommandsStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to fetch Custom Commands: " + e.getMessage(), e);
            }
        }).start();
    }

    public void fetchCustomCommandsForChannel(TwitchUser channel) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (channel == null) throw new IllegalArgumentException("Channel cannot be null");

                // Fetch commands from database
                var fetchCommandsStatement = database.getConnection().prepareStatement(
                        "SELECT * FROM CustomCommands WHERE id = ?;"
                );

                // Set the channel ID
                fetchCommandsStatement.setInt(1, channel.getId());

                // Execute the statement
                var resultSet = fetchCommandsStatement.executeQuery();

                var twitchBot = database.getTwitchBot();
                twitchBot.getCommandHandler().resetCustomCommands(channel);
                while (resultSet.next()) new CustomCommand(twitchBot, channel, resultSet);

                // Close the statement and result set
                resultSet.close();
                fetchCommandsStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to fetch Custom Commands for channel: " + channel.getDisplayName() + " - " + e.getMessage(), e);
            }
        }).start();
    }
}