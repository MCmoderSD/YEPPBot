package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.utilities.database.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ChannelManager {

    // Associations
    private final MySQL mySQL;

    // Constructor
    public ChannelManager(MySQL mySQL) {

        // Get Associations
        this.mySQL = mySQL;
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
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }

        return (isActive ? "Joining " : "Leaving ") + channel;
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
            if (blacklist == null) blacklist = "";
            ArrayList<String> list = new ArrayList<>(List.of(blacklist.split("; ")));
            if (isBlocked && !list.contains(command)) list.add(command);
            else if (!isBlocked) list.remove(command);
            list.remove("");

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
}