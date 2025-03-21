package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.SQL;

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
    private final SQL sql;

    // Constructor
    public LurkManager(SQL sql) {

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

            // SQL statement for creating the lurk list table
            connection.prepareStatement(condition +
                    """
                    LurkList (
                    userId INT PRIMARY KEY NOT NULL,
                    lurkChannelId INT NOT NULL,
                    startTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    traitorChannel TEXT,
                    FOREIGN KEY (lurkChannelId) REFERENCES Users(id)
                    ) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=1 CHARSET=utf8mb4
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
            if (!sql.isConnected()) sql.connect(); // connect

            // Check Channel and User
            sql.checkCache(userID, event.getUser(), false);
            sql.checkCache(channelID, event.getChannel(), true);

            // Prepare statement
            PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                    "INSERT INTO LurkList (userId, lurkChannelId, startTime) VALUES (?, ?, ?)"
            );

            // Set values and execute
            preparedStatement.setInt(1, userID);                        // set user
            preparedStatement.setInt(2, channelID);                     // set channel
            preparedStatement.setTimestamp(3, event.getTimestamp());    // set timestamp
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
        HashMap<Integer, Integer> LurkList = new HashMap<>();

        // Get Custom Timers
        try {
            if (!sql.isConnected()) sql.connect(); // connect

            // Prepare statement
            PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                    "SELECT userId, lurkChannelId FROM LurkList"
            );

            // Execute
            ResultSet resultSet = preparedStatement.executeQuery();

            // Add to List
            while (resultSet.next()) LurkList.put(resultSet.getInt("userId"), resultSet.getInt("lurkChannelId"));

            // Close resources
            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return LurkList;
    }

    // Get Lurk Time
    public HashMap<Timestamp, ArrayList<Integer>> getLurkTime(int userID) {

        // Variables
        HashMap<Timestamp, ArrayList<Integer>> lurkTime = new HashMap<>();
        ArrayList<Integer> channels = new ArrayList<>();

        // Get Custom Timers
        try {
            if (!sql.isConnected()) sql.connect(); // connect

            // Prepare statement
            PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                    "SELECT startTime, lurkChannelId, traitorChannel FROM LurkList WHERE userId = ?"
            );

            // Set values and execute
            preparedStatement.setInt(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Add to List
            while (resultSet.next()) {

                // Get Start Time
                Timestamp startTime = resultSet.getTimestamp("startTime");

                // Get Lurk Channel
                var channel = resultSet.getInt("lurkChannelId");
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
            if (!sql.isConnected()) sql.connect(); // connect

            // Prepare statement
            PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                    "DELETE FROM LurkList WHERE userId = ?"
            );

            // Set values and execute
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
                if (!sql.isConnected()) sql.connect(); // connect

                // Prepare statement
                PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                        "UPDATE LurkList SET traitorChannel = ? WHERE userId = ?"
                );

                // Set values and execute
                preparedStatement.setString(1, traitors);   // set traitor
                preparedStatement.setInt(2, userID);        // set user
                preparedStatement.executeUpdate(); // execute

                // Close resources
                preparedStatement.close();

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }
}