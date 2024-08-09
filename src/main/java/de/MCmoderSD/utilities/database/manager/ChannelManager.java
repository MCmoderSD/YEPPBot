package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.utilities.database.MySQL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ChannelManager {

    // Assosiations
    private final MySQL mySQL;

    // Constructor
    public ChannelManager(MySQL mySQL) {
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
}
