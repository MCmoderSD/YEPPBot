package de.MCmoderSD.database.manager;

import de.MCmoderSD.database.Database;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.openai.prompts.ChatPrompt;

import java.sql.SQLException;
import java.util.HashMap;

import static de.MCmoderSD.utilities.ZipUtil.inflateTwitchUser;

public class OpenAIManger {

    // Associations
    private final Database database;

    // Constructor
    public OpenAIManger(Database database) {

        // Check Parameters
        if (database == null) throw new IllegalArgumentException("Database cannot be null");

        // Set Associations
        this.database = database;
    }

    public void saveConversation(TwitchUser user, ChatPrompt chatPrompt) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (user == null) throw new IllegalArgumentException("TwitchUser user cannot be null");
                if (chatPrompt == null) throw new IllegalArgumentException("ChatPrompt chatPrompt cannot be null");

                // Prepare the query
                var saveConversationStatement = database.getConnection().prepareStatement(
                        "INSERT INTO Conversation (userId, response) VALUES (?, ?) ON DUPLICATE KEY UPDATE response = VALUES(response);"
                );

                // Set the query parameters
                saveConversationStatement.setInt(1, user.getId());          // User ID
                saveConversationStatement.setString(2, chatPrompt.getId()); // Response ID

                // Execute the query
                saveConversationStatement.executeUpdate();

                // Close resources
                saveConversationStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to save conversation", e);
            }
        }).start();
    }

    public void deleteConversation(TwitchUser user) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (user == null) throw new IllegalArgumentException("TwitchUser user cannot be null");

                // Prepare the query
                var deleteConversationStatement = database.getConnection().prepareStatement(
                        "DELETE FROM Conversation WHERE userId = ?;"
                );

                // Set the query parameters
                deleteConversationStatement.setInt(1, user.getId()); // User ID

                // Execute the query
                deleteConversationStatement.executeUpdate();

                // Close resources
                deleteConversationStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete conversation", e);
            }
        }).start();
    }

    public HashMap<TwitchUser, String> getConversations() {
        try {

            // Prepare the query
            var getConversationsStatement = database.getConnection().prepareStatement(
                    "SELECT user, response FROM Conversation JOIN User ON userId = id;"
            );

            // Execute the query
            var resultSet = getConversationsStatement.executeQuery();

            // Process the results
            HashMap<TwitchUser, String> conversations = new HashMap<>();
            while (resultSet.next()) conversations.put(inflateTwitchUser(resultSet.getBytes("user")), resultSet.getString("response"));

            // Close resources
            resultSet.close();
            getConversationsStatement.close();

            // Return
            return conversations;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get conversations", e);
        }
    }
}
