package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.utilities.database.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;

public class QuoteManager {

    // Assosiations
    private final MySQL mySQL;

    // Constructor
    public QuoteManager(MySQL mySQL) {

        // Set associations
        this.mySQL = mySQL;

        // Initialize
        initTables();
    }

    // Initialize Tables
    private void initTables() {
        try {

            // Variables
            Connection connection = mySQL.getConnection();

            // Condition for creating tables
            String condition = "CREATE TABLE IF NOT EXISTS ";

            // SQL statement for creating the lurk list table
            connection.prepareStatement(condition +
                    """
                    Quotes (
                    channel_id INT NOT NULL,
                    quote_id INT NOT NULL,
                    quote TEXT NOT NULL,
                    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (channel_id) REFERENCES users(id)
                    )
                    """
            ).execute();

            // Close resources
            connection.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Get Quotes
    public ArrayList<String> getQuotes(int channel_id) {
        try {

            // Variables
            Connection connection = mySQL.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT quote FROM Quotes WHERE channel_id = ?");
            statement.setInt(1, channel_id);
            ResultSet resultSet = statement.executeQuery();

            // Create List
            ArrayList<String> quotes = new ArrayList<>();

            // Add Quotes
            while (resultSet.next()) quotes.add(resultSet.getString("quote"));

            // Close resources
            resultSet.close();
            statement.close();

            // Return
            return quotes;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    // Add Quote
    public String addQuote(int channel_id, String quote) {
        try {

            // Variables
            var quoteIndex = getQuotes(channel_id).size();

            // Add Quote
            Connection connection = mySQL.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO Quotes (channel_id, quote_id, quote) VALUES (?, ?, ?)");
            statement.setInt(1, channel_id);
            statement.setInt(2, quoteIndex);
            statement.setString(3, quote);
            statement.execute();

            // Close resources
            statement.close();

            // Return
            return String.format("Added quote #%d: %s", quoteIndex + 1, quote);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error while adding quote.";
        }
    }

    // Remove Quote
    public String removeQuote(int channel_id, int quote_id) {
        try {

            // Variables
            Connection connection = mySQL.getConnection();
            PreparedStatement statement = connection.prepareStatement("DELETE FROM Quotes WHERE channel_id = ? AND quote_id = ?");
            statement.setInt(1, channel_id);
            statement.setInt(2, quote_id);
            statement.execute();

            // Close resources
            statement.close();

            // Reorder
            reorderQuotes(channel_id);

            // Return
            return String.format("Removed quote #%d", quote_id + 1);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error while removing quote.";
        }
    }

    // Edit Quote
    public String editQuote(int channel_id, int quote_id, String quote) {
        try {

            // Variables
            Connection connection = mySQL.getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE Quotes SET quote = ? WHERE channel_id = ? AND quote_id = ?");
            statement.setString(1, quote);
            statement.setInt(2, channel_id);
            statement.setInt(3, quote_id);
            statement.execute();

            // Close resources
            statement.close();

            // Return
            return String.format("Edited quote #%d to %s", quote_id + 1, quote);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error while editing quote.";
        }
    }

    // Reorder Quotes
    private void reorderQuotes(int channel_id) {
        try {

            // Variables
            Connection connection = mySQL.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT quote_id FROM Quotes WHERE channel_id = ?");
            statement.setInt(1, channel_id);
            ResultSet resultSet = statement.executeQuery();

            // Create List
            ArrayList<Integer> quoteIds = new ArrayList<>();

            // Add Quotes
            while (resultSet.next()) quoteIds.add(resultSet.getInt("quote_id"));

            // Reorder
            for (var i = 0; i < quoteIds.size(); i++) {
                PreparedStatement reorderStatement = connection.prepareStatement("UPDATE Quotes SET quote_id = ? WHERE channel_id = ? AND quote_id = ?");
                reorderStatement.setInt(1, i);
                reorderStatement.setInt(2, channel_id);
                reorderStatement.setInt(3, quoteIds.get(i));
                reorderStatement.execute();
            }

            // Close resources
            resultSet.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}