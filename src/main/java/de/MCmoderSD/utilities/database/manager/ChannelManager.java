package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.enums.Account;
import de.MCmoderSD.utilities.database.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Format.*;

public class ChannelManager {

    // Associations
    private final MySQL mySQL;

    // Constructor
    public ChannelManager(MySQL mySQL) {

        // Get Associations
        this.mySQL = mySQL;

        // Initialize Tables
        initTables();
    }

    // Initialize Tables
    private void initTables() {
        try {
            if (!mySQL.isConnected()) mySQL.connect();

            // Variables
            Connection connection = mySQL.getConnection();

            // Condition for creating tables
            String condition = "CREATE TABLE IF NOT EXISTS ";

            // SQL statement for creating the Account table
            connection.prepareStatement(condition +
                    """
                    AccountValues (
                    id INT PRIMARY KEY,
                    apex VARCHAR(500),
                    league VARCHAR(500),
                    rainbow VARCHAR(500),
                    valorant VARCHAR(500),
                    instagram VARCHAR(500),
                    tiktok VARCHAR(500),
                    twitter VARCHAR(500),
                    youtube VARCHAR(500)
                    )
                    """
            ).execute();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Get Active Channels
    public ArrayList<String> getActiveChannels() {

        // Variables
        ArrayList<String> channels = new ArrayList<>();

        try {
            if (!mySQL.isConnected()) mySQL.connect();

            String query = "SELECT name FROM " + "channels" + " WHERE active = 1";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) channels.add(resultSet.getString("name"));

            // Close resources
            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return channels;
    }

    // Edit Channel
    public String editChannel(String channel, boolean isActive) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "UPDATE " + "channels" + " SET active = ? WHERE name = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, isActive ? 1 : 0); // set active
            preparedStatement.setString(2, channel); // set channel
            preparedStatement.executeUpdate(); // execute

            // Close resources
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }

        return (isActive ? "Joining " : "Leaving ") + channel;
    }

    public String autoShoutout(Integer channelId, boolean isAutoShoutout) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "UPDATE " + "channels" + " SET auto_shoutout = ? WHERE id = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, isAutoShoutout ? 1 : 0); // set auto shoutout
            preparedStatement.setInt(2, channelId); // set channel
            preparedStatement.executeUpdate(); // execute

            // Close resources
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }

        return "Auto shoutout " + (isAutoShoutout ? "enabled" : "disabled");
    }

    public boolean hasAutoShoutout(Integer channelId) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT auto_shoutout FROM " + "channels" + " WHERE id = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, channelId); // set channel
            ResultSet resultSet = preparedStatement.executeQuery(); // execute

            // Close resources
            boolean hasAutoShoutout = resultSet.next() && resultSet.getInt("auto_shoutout") == 1;
            resultSet.close();
            preparedStatement.close();
            return hasAutoShoutout;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    // Get Black List
    public HashMap<Integer, HashSet<String>> getBlackList() {

        // Variables
        HashMap<Integer, HashSet<String>> blackList = new HashMap<>();

        try {
            if (!mySQL.isConnected()) mySQL.connect();

            String query = "SELECT id, blacklist FROM " + "channels";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                var id = resultSet.getInt("id");
                String blacklist = resultSet.getString("blacklist");
                if (blacklist != null) blackList.put(id, new HashSet<>(List.of(blacklist.toLowerCase().split("; "))));
            }

            // Close resources
            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return blackList;
    }

    // Edit Blacklist
    public String editBlacklist(String channel, String command, boolean isBlocked, HelixHandler helixHandler) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Check Channel
            if (!mySQL.getChannelCache().containsValue(channel)) mySQL.checkCache(helixHandler.getUser(channel).getId(), channel, true);

            // Variables
            Connection connection = mySQL.getConnection();

            // Prepare select statement
            String selectQuery = "SELECT blacklist FROM channels WHERE name = ?";
            PreparedStatement selectPreparedStatement = connection.prepareStatement(selectQuery);
            selectPreparedStatement.setString(1, channel);
            ResultSet resultSet = selectPreparedStatement.executeQuery();
            if (!resultSet.next()) {
                resultSet.close();
                selectPreparedStatement.close();
                return "Error: Channel not found";
            }
            String blacklist = resultSet.getString("blacklist");
            if (blacklist == null) blacklist = EMPTY;
            ArrayList<String> list = new ArrayList<>(List.of(blacklist.split("; ")));
            if (isBlocked && !list.contains(command)) list.add(command);
            else if (!isBlocked) list.remove(command);
            list.remove(EMPTY);

            // Close select resources
            resultSet.close();
            selectPreparedStatement.close();

            // Prepare update statement
            String updateQuery = "UPDATE channels SET blacklist = ? WHERE name = ?";
            PreparedStatement updatePreparedStatement = connection.prepareStatement(updateQuery);
            updatePreparedStatement.setString(1, String.join("; ", list));
            updatePreparedStatement.setString(2, channel);
            updatePreparedStatement.executeUpdate();

            // Close update statement
            updatePreparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }

        return (isBlocked ? "Blocking " : "Unblocking ") + command + " in " + channel;
    }

    public boolean setAccountValue(Integer id, Account account, String value) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT id FROM AccountValues WHERE id = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, id); // set id
            ResultSet resultSet = preparedStatement.executeQuery(); // execute

            // Check if account exists
            if (!resultSet.next()) {

                // Close resources
                resultSet.close();
                preparedStatement.close();

                // Insert new account if it does not exist
                query = "INSERT INTO AccountValues (id) VALUES (?)";
                preparedStatement = mySQL.getConnection().prepareStatement(query);
                preparedStatement.setInt(1, id); // set id
                preparedStatement.executeUpdate(); // execute
            } else {
                resultSet.close();
                preparedStatement.close();
            }

            // Prepare statement
            query = "UPDATE AccountValues SET " + account.getTable() + " = ? WHERE id = ?";
            preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setString(1, value); // set value
            //noinspection JpaQueryApiInspection
            preparedStatement.setInt(2, id); // set id
            preparedStatement.executeUpdate(); // execute

            // Close resources
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    // Get Account Value
    public String getAccountValue(Integer id, Account account) {
        try {
            if (!mySQL.isConnected()) mySQL.connect();

            // Prepare statement
            String query = "SELECT " + account.getTable() + " FROM " + "AccountValues" + " WHERE id = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, id); // set id
            ResultSet resultSet = preparedStatement.executeQuery(); // execute
            return resultSet.next() ? resultSet.getString(account.getTable()) : null;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}