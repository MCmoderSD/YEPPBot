package de.MCmoderSD.database.manager;

import de.MCmoderSD.database.Database;
import de.MCmoderSD.encryption.enums.Hash;
import de.MCmoderSD.helix.objects.TwitchUser;

import java.nio.file.LinkOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class QuoteManager {

    // Associations
    private final Database database;

    // Attributes
    private final Connection connection;

    // Constructor
    public QuoteManager(Database database) {

        // Check Parameters
        if (database == null) throw new IllegalArgumentException("Database cannot be null");

        // Set Associations
        this.database = database;

        // Set Attributes
        connection = database.getConnection();
    }

    public LinkedHashMap<Integer, String> getQuotes(TwitchUser channel) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

            // Prepare the query
            var getQuotesStatement = connection.prepareStatement(
                    "SELECT id, quote FROM Quote WHERE channelId = ? ORDER BY id;"
            );

            // Set the query parameter
            getQuotesStatement.setInt(1, channel.getId());

            // Execute the query
            var resultSet = getQuotesStatement.executeQuery();

            // Process the results
            var quotes = new LinkedHashMap<Integer, String>();
            while (resultSet.next()) quotes.put(resultSet.getInt("id"), resultSet.getString("quote"));

            // Close resources
            resultSet.close();
            getQuotesStatement.close();

            // Return quotes
            return quotes;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get quotes", e);
        }
    }

    public void reorderQuotes(TwitchUser channel) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

                // Variables
                var channelId = channel.getId();
                var quotes = getQuotes(channel);
                var quoteIds = new ArrayList<>(quotes.keySet());

                // Get all quotes
                PreparedStatement updateQuoteStatement = null;
                for (var i = 0; i < quoteIds.size(); i++) {

                    // Prepare statement
                    updateQuoteStatement = connection.prepareStatement(
                            "UPDATE Quote SET id = ? WHERE channelId = ? AND id = ?"
                    );

                    // Set values and execute
                    updateQuoteStatement.setInt(1, i);
                    updateQuoteStatement.setInt(2, channelId);
                    updateQuoteStatement.setInt(3, quoteIds.get(i));
                    updateQuoteStatement.addBatch();
                }

                // Check statement
                if (updateQuoteStatement == null) return;

                // Execute batch
                updateQuoteStatement.executeBatch();

                // Close resources
                updateQuoteStatement.close();

            } catch (Exception e) {
                throw new RuntimeException("Failed to reorder quotes", e);
            }
        }).start();
    }

    // Add Quote
    public void addQuote(String quote, TwitchUser channel) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (quote == null || quote.isBlank() || quote.length() > 500) throw new IllegalArgumentException("Quote text is invalid");
                if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

                // Get Quote Index
                var index = getQuotes(channel).size();

                // Insert quote entry
                var insertQuoteStatement = connection.prepareStatement(
                        "INSERT INTO Quote (channelId, id, quote) VALUES (?, ?, ?);"
                );

                // Set the insert values
                insertQuoteStatement.setInt(1, channel.getId());    // Channel
                insertQuoteStatement.setInt(2, index);              // Quote ID
                insertQuoteStatement.setString(3, quote);           // Quote Text

                // Execute the statement
                insertQuoteStatement.executeUpdate();

                // Close the statement
                insertQuoteStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to add quote entry", e);
            }
        }).start();
    }

    public void removeQuote(int quoteId, TwitchUser channel) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (quoteId < 0) throw new IllegalArgumentException("Quote ID must be non-negative");
                if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

                // Delete quote entry
                var deleteQuoteStatement = connection.prepareStatement(
                        "DELETE FROM Quote WHERE channelId = ? AND id = ?;"
                );

                // Set the delete values
                deleteQuoteStatement.setInt(1, channel.getId());    // Channel
                deleteQuoteStatement.setInt(2, quoteId);            // Quote ID

                // Execute the statement
                deleteQuoteStatement.executeUpdate();

                // Close the statement
                deleteQuoteStatement.close();

                // Reorder quotes
                reorderQuotes(channel);

            } catch (Exception e) {
                throw new RuntimeException("Failed to remove quote entry", e);
            }
        }).start();
    }

    public void editQuote(int id, String quote, TwitchUser channel) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (id < 0) throw new IllegalArgumentException("Quote ID must be non-negative");
                if (quote == null || quote.isBlank() || quote.length() > 500) throw new IllegalArgumentException("Quote text is invalid");
                if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

                // Update quote entry
                var updateQuoteStatement = connection.prepareStatement(
                        "UPDATE Quote SET quote = ? WHERE channelId = ? AND id = ?;"
                );

                // Set the update values
                updateQuoteStatement.setString(1, quote);           // Quote Text
                updateQuoteStatement.setInt(2, channel.getId());    // Channel
                updateQuoteStatement.setInt(3, id);                 // Quote ID

                // Execute the statement
                updateQuoteStatement.executeUpdate();

                // Close the statement
                updateQuoteStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to edit quote entry", e);
            }
        }).start();
    }
}