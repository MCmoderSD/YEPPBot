package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.utilities.database.MySQL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BlackListManager {

    // Assosiations
    private final MySQL mySQL;

    // Constructor
    public BlackListManager(MySQL mySQL) {

        // Get Associations
        this.mySQL = mySQL;
    }

    // Get Black List
    public HashMap<Integer, ArrayList<String>> getBlackList() {

        // Variables
        HashMap<Integer, ArrayList<String>> blackList = new HashMap<>();

        try {
            if (!mySQL.isConnected()) mySQL.connect();

            String query = "SELECT id, blacklist FROM " + "channels";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                var id = resultSet.getInt("id");
                String blacklist = resultSet.getString("blacklist");
                if (blacklist != null) blackList.put(id, new ArrayList<>(List.of(blacklist.toLowerCase().split("; "))));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return blackList;
    }
}