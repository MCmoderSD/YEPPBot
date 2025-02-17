package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Format.*;

public class LurkManager {

    // Associations
    private final MySQL mySQL;

    // Constructor
    public LurkManager(MySQL mySQL) {

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
                    lurkList (
                    user_id INT PRIMARY KEY NOT NULL,
                    lurkChannel_ID INT NOT NULL,
                    startTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    traitorChannel TEXT,
                    FOREIGN KEY (lurkChannel_ID) REFERENCES users(id)
                    )
                    """
            ).execute();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Save Lurk
    public HashMap<Integer, Integer> saveLurk(TwitchMessageEvent event) {

        // Set Variables
        var channelID = event.getChannelId();
        var userID = event.getUserId();

        // Log message
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Check Channel and User
            mySQL.checkCache(userID, event.getUser(), false);
            mySQL.checkCache(channelID, event.getChannel(), true);

            // Prepare statement
            String query = "INSERT INTO " + "lurkList" + " (user_id, lurkChannel_ID, startTime) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, userID); // set user
            preparedStatement.setInt(2, channelID); // set channel
            preparedStatement.setTimestamp(3, event.getTimestamp()); // set timestamp
            preparedStatement.executeUpdate(); // execute

            // Close resources
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return getLurkList(); // get lurk time
    }

    public HashMap<Integer, Integer> getLurkList() {

        // Variables
        HashMap<Integer, Integer> lurkList = new HashMap<>();

        // Get Custom Timers
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT user_id, lurkChannel_ID FROM " + "lurkList";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Add to List
            while (resultSet.next()) lurkList.put(resultSet.getInt("user_id"), resultSet.getInt("lurkChannel_ID"));

            // Close resources
            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return lurkList;
    }

    // Get Lurk Time
    public HashMap<Timestamp, ArrayList<Integer>> getLurkTime(int userID) {

        // Variables
        HashMap<Timestamp, ArrayList<Integer>> lurkTime = new HashMap<>();
        ArrayList<Integer> channels = new ArrayList<>();

        // Get Custom Timers
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT startTime, lurkChannel_ID, traitorChannel FROM " + "lurkList WHERE user_id = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Add to List
            while (resultSet.next()) {
                // Get Start Time
                Timestamp startTime = resultSet.getTimestamp("startTime");

                // Get Lurk Channel
                var channel = resultSet.getInt("lurkChannel_ID");
                channels.add(channel);
                lurkTime.put(startTime, channels);

                // Get TraitorChannel
                if (resultSet.getString("traitorChannel") == null) return lurkTime;
                String[] traitorChannel = resultSet.getString("traitorChannel").split(TAB);
                if (traitorChannel.length == 0 || traitorChannel[0].isEmpty()) return lurkTime;
                for (String name : traitorChannel) if (!name.isEmpty()) channels.add(Integer.parseInt(name));
            }

            // Close resources
            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return lurkTime;
    }

    // Remove Lurker
    public HashMap<Integer, Integer> removeLurker(int userID) {

        // Log message
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "DELETE FROM " + "lurkList" + " WHERE user_id = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, userID); // set user
            preparedStatement.executeUpdate(); // execute

            // Close resources
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return getLurkList(); // get lurk time
    }

    // Add Traitor
    public void addTraitor(int userID, String traitors) {
        new Thread(() -> {

            // Log message
            try {
                if (!mySQL.isConnected()) mySQL.connect(); // connect

                // Prepare statement
                String query = "UPDATE " + "lurkList" + " SET traitorChannel = ? WHERE user_id = ?";
                PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
                preparedStatement.setString(1, traitors); // set traitor
                preparedStatement.setInt(2, userID); // set user
                preparedStatement.executeUpdate(); // execute

                // Close resources
                preparedStatement.close();

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }
}