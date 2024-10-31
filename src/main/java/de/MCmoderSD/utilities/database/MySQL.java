package de.MCmoderSD.utilities.database;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.objects.Birthdate;
import de.MCmoderSD.objects.TwitchMessageEvent;

import de.MCmoderSD.utilities.database.manager.AssetManager;
import de.MCmoderSD.utilities.database.manager.ChannelManager;
import de.MCmoderSD.utilities.database.manager.CustomManager;
import de.MCmoderSD.utilities.database.manager.EventManager;
import de.MCmoderSD.utilities.database.manager.LogManager;
import de.MCmoderSD.utilities.database.manager.LurkManager;
import de.MCmoderSD.utilities.database.manager.QuoteManager;
import de.MCmoderSD.utilities.database.manager.TokenManager;
import de.MCmoderSD.utilities.database.manager.YEPPConnect;

import javax.management.InvalidAttributeValueException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.LinkedHashMap;

@SuppressWarnings({"SqlSourceToSinkFlow", "unused"})
public class MySQL extends Driver {

    // Managers
    private final AssetManager assetManager;
    private final ChannelManager channelManager;
    private final CustomManager customManager;
    private final EventManager eventManager;
    private final LogManager logManager;
    private final LurkManager lurkManager;
    private final QuoteManager quoteManager;
    private final TokenManager tokenManager;
    private final YEPPConnect yeppConnect;

    // Cache Lists
    private final HashMap<Integer, String> userCache;
    private final HashMap<Integer, String> channelCache;

    // Constructor
    public MySQL(JsonNode config) {

        // Initialize Driver
        super(config);

        // Connect to database
        new Thread(this::connect).start();
        initTables();

        // Load Cache
        userCache = loadCache(Table.USERS);
        channelCache = loadCache(Table.CHANNELS);

        // Initialize Manager
        assetManager = new AssetManager(this);
        channelManager = new ChannelManager(this);
        customManager = new CustomManager(this);
        eventManager = new EventManager(this);
        logManager = new LogManager(this);
        lurkManager = new LurkManager(this);
        quoteManager = new QuoteManager(this);
        tokenManager = new TokenManager(this);
        yeppConnect = new YEPPConnect(this);
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
                            name VARCHAR(25) NOT NULL,
                            birthdate VARCHAR(10)
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
                            active BIT NOT NULL DEFAULT 1,
                            FOREIGN KEY (id) REFERENCES users(id)
                            )
                            """
            ).execute();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Load Cache
    private HashMap<Integer, String> loadCache(Table table) {

        // Variables
        HashMap<Integer, String> cache = new HashMap<>();

        // Load Channel Cache
        try {
            if (!isConnected()) connect(); // connect

            // Query
            String query = "SELECT id, name FROM " + table.getName();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Add to Cache
            while (resultSet.next()) {
                var id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                cache.put(id, name);
            }

            // Close resources
            resultSet.close();
            preparedStatement.close(); // close the preparedStatement
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        // Return
        return cache;
    }

    // Checks Channels
    private void checkUser(int id, String name, Table table) throws SQLException {
        if (!isConnected()) connect(); // connect

        // Query
        String query = "SELECT id, name FROM " + table.getName() + " WHERE id = ?";
        PreparedStatement selectPreparedStatement = connection.prepareStatement(query);
        selectPreparedStatement.setInt(1, id);
        ResultSet resultSet = selectPreparedStatement.executeQuery();

        // Check if user exists
        if (!resultSet.next()) {
            String insertQuery = "INSERT INTO " + table.getName() + " (id, name) VALUES (?, ?)";
            PreparedStatement insertPreparedStatement = connection.prepareStatement(insertQuery);
            insertPreparedStatement.setInt(1, id);
            insertPreparedStatement.setString(2, name);
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
        boolean user = userCache.containsKey(id) && userCache.get(id).equals(name);
        boolean channel = channelCache.containsKey(id) && channelCache.get(id).equals(name);

        // Return if in Cache
        if (user && channel) return;

        // Update User Cache
        if (!user) {
            checkUser(id, name, Table.USERS);
            userCache.put(id, name);
        }

        // Update Channel Cache
        if (!channel && isChannel) {
            checkUser(id, name, Table.CHANNELS);
            channelCache.put(id, name);
        }
    }

    // Set Birthday
    public void setBirthday(TwitchMessageEvent event, Birthdate birthdate) {
        try {
            if (!isConnected()) connect(); // connect

            // Variables
            var id = event.getUserId();

            // Check Cache
            checkCache(id, event.getUser(), false);
            checkCache(event.getChannelId(), event.getChannel(), true);

            // Update Birthday
            String updateQuery = "UPDATE users SET birthdate = ? WHERE id = ?";
            PreparedStatement updatePreparedStatement = connection.prepareStatement(updateQuery);
            updatePreparedStatement.setString(1, birthdate.getMySQLDate());
            updatePreparedStatement.setInt(2, id);
            updatePreparedStatement.executeUpdate(); // execute
            updatePreparedStatement.close(); // close the updatePreparedStatement
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Get Birthdays
    public LinkedHashMap<Integer, Birthdate> getBirthdays() {
        try {
            if (!isConnected()) connect(); // connect

            // Variables
            LinkedHashMap<Integer, Birthdate> birthdays = new LinkedHashMap<>();

            // Query
            String query = "SELECT id, birthdate FROM users WHERE birthdate IS NOT NULL";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Add Birthdays
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String[] date = resultSet.getString("birthdate").split("\\.");
                String birthday = date[2] + "." + date[1] + "." + date[0];
                birthdays.put(id, new Birthdate(birthday));
            }

            // Close resources
            resultSet.close();
            preparedStatement.close(); // close the preparedStatement

            return birthdays;
        } catch (SQLException | InvalidAttributeValueException | NumberFormatException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    // Get Cache
    public HashMap<Integer, String> getChannelCache() {
        return channelCache;
    }

    public HashMap<Integer, String> getUserCache() {
        return userCache;
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

    public EventManager getEventManager() {
        return eventManager;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public LurkManager getLurkManager() {
        return lurkManager;
    }

    public QuoteManager getQuoteManager() {
        return quoteManager;
    }

    public TokenManager getTokenManager() {
        return tokenManager;
    }

    public YEPPConnect getYEPPConnect() {
        return yeppConnect;
    }

    public enum Table {

        // Tables
        USERS("users"),
        CHANNELS("channels");

        // Variables
        private final String name;

        // Constructor
        Table(String name) {
            this.name = name;
        }

        // Getter
        public String getName() {
            return name;
        }
    }
}