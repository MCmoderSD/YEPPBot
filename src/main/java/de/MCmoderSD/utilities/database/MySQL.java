package de.MCmoderSD.utilities.database;

import de.MCmoderSD.main.Main;
import de.MCmoderSD.utilities.database.manager.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

@SuppressWarnings("unused")
public class MySQL extends Driver {

    // Associations
    private final AssetManager assetManager;
    private final ChannelManager channelManager;
    private final CustomManager customManager;
    private final LogManager logManager;
    private final LurkManager lurkManager;

    // Cache Lists
    private final HashMap<Integer, String> channelCache;
    private final HashMap<Integer, String> userCache;

    // Constructor
    public MySQL(Main main) {

        // Initialize Driver
        super(main.getCredentials().getMySQLConfig());

        // Connect to database
        new Thread(this::connect).start();
        initTables();

        // Load Cache
        channelCache = loadCache("channels");
        userCache = loadCache("users");

        // Initialize Manager
        assetManager = new AssetManager(this);
        channelManager = new ChannelManager(this);
        customManager = new CustomManager(this);
        logManager = new LogManager(this, main.hasArg(Main.Argument.LOG));
        lurkManager = new LurkManager(this);
    }

    // Initialize Tables
    private void initTables() {
        try {
            if (!isConnected()) connect();

            // Condition for creating tables
            String condition = "CREATE TABLE IF NOT EXISTS ";

            // SQL statement for creating the users table
            connection.prepareStatement(condition +
                    """
                            users (
                            id INT PRIMARY KEY,
                            name VARCHAR(25) NOT NULL
                            )
                            """
            ).execute();

            // SQL statement for creating the channels table
            connection.prepareStatement(condition +
                    """
                            channels (
                            id INT PRIMARY KEY,
                            name VARCHAR(25) NOT NULL,
                            blacklist TEXT,
                            active BIT NOT NULL DEFAULT 1
                            )
                            """
            ).execute();


            // Copy all channel names to the users table
            connection.prepareStatement("INSERT INTO users (id, name) SELECT channels.id, channels.name FROM channels LEFT JOIN users ON channels.id = users.id WHERE users.id IS NULL").execute();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Update Channels and Users
    private void updateCache(int id, String name, String table) throws SQLException {
        if (!isConnected()) connect(); // connect

        // Check Channel
        String selectQuery = "SELECT * FROM " + table + " WHERE id = ?";
        PreparedStatement selectPreparedStatement = getConnection().prepareStatement(selectQuery);
        selectPreparedStatement.setInt(1, id);
        ResultSet resultSet = selectPreparedStatement.executeQuery();

        // Add Channel
        if (!resultSet.next()) {
            String insertQuery = "INSERT INTO channels (id, name) VALUES (?, ?)";
            PreparedStatement insertPreparedStatement = getConnection().prepareStatement(insertQuery);
            insertPreparedStatement.setInt(1, id); // set id
            insertPreparedStatement.setString(2, name); // set name
            insertPreparedStatement.executeUpdate(); // execute
            insertPreparedStatement.close(); // close the insertPreparedStatement
        }

        // Close resources
        resultSet.close();
        selectPreparedStatement.close(); // close the selectPreparedStatement
    }

    // Check Cache
    public void checkCache(int id, String name, boolean isChannel) throws SQLException {

        // Check Cache
        boolean channel = channelCache.containsKey(id) && channelCache.get(id).equals(name);
        boolean user = userCache.containsKey(id) && userCache.get(id).equals(name);

        // Return if in Cache
        if (channel && user) return;

        // Update Channel Cache
        if (!channel && isChannel) {
            updateCache(id, name, "channels");
            channelCache.put(id, name);
        }

        // Update User Cache
        if (!user) {
            updateCache(id, name, "users");
            userCache.put(id, name);
        }
    }

    // Load Cache
    private HashMap<Integer, String> loadCache(String table) {

        // Variables
        HashMap<Integer, String> cache = new HashMap<>();

        // Load Channel Cache
        try {
            if (!isConnected()) connect(); // connect

            String query = null;
            if (table.equals("channels")) query = "SELECT * FROM " + "channels";
            if (table.equals("users")) query = "SELECT * FROM " + "users";
            if (query == null) return null;
            PreparedStatement preparedStatement = getConnection().prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) cache.put(resultSet.getInt("id"), resultSet.getString("name")); // add to cache
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return cache;
    }

    // Query ID
    public int queryID(String table, String name) {
        try {
            if (!isConnected()) connect();
            String query = null;
            if (table.equals("channels")) query = "SELECT id FROM " + "channels" + " WHERE name = ?";
            if (table.equals("users")) query = "SELECT id FROM " + "users" + " WHERE name = ?";
            if (query == null) return -1;
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return resultSet.getInt("id");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return -1;
    }

    // Query Name
    public String queryName(String table, int id) {

        try {
            if (!isConnected()) connect();
            String query = null;
            if (table.equals("channels")) query = "SELECT name FROM " + "channels" + " WHERE id = ?";
            if (table.equals("users")) query = "SELECT name FROM " + "users" + " WHERE id = ?";
            if (query == null) return null;
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return resultSet.getString("name");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    // Get Cache
    public HashMap<Integer, String> getChannelCache() {
        return channelCache;
    }

    // Get Manager
    public AssetManager getAssetManager() {
        return assetManager;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public CustomManager getCustomManager() {
        return customManager;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public LurkManager getLurkManager() {
        return lurkManager;
    }
}