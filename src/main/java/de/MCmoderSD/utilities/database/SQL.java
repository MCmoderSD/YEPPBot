package de.MCmoderSD.utilities.database;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.sql.Driver;
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

@SuppressWarnings("unused")
public class SQL extends Driver {

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
    public SQL(JsonNode config) {

        // Initialize Driver
        super(DatabaseType.MARIADB, config);

        // Initialize Tables
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
                            birthdate VARCHAR(10) CHAR SET ascii
                            ) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=1 CHARSET=utf8mb4
                            """
            ).execute();

            // SQL statement for creating the channels table
            connection.prepareStatement(condition +
                            """
                            channels (
                            id INT PRIMARY KEY,
                            name VARCHAR(25) NOT NULL,
                            blacklist TEXT,
                            active BIT NOT NULL DEFAULT TRUE,
                            auto_shoutout BIT NOT NULL DEFAULT FALSE,
                            FOREIGN KEY (id) REFERENCES users(id)
                            ) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=1 CHARSET=utf8mb4
                            """
            ).execute();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Load Cache
    @SuppressWarnings("SqlSourceToSinkFlow")
    private HashMap<Integer, String> loadCache(Table table) {

        // Variables
        HashMap<Integer, String> cache = new HashMap<>();

        // Load Channel Cache
        try {
            if (!isConnected()) connect(); // connect

            // Query
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT id, name FROM " + table.getName()
            );

            // Execute
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

    // Check User
    @SuppressWarnings("SqlSourceToSinkFlow")
    private void checkUser(int id, String name, Table table) throws SQLException {

        // Connect
        if (!isConnected()) connect();

        // Query
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO " + table.getName() + " (id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name)"
        );

        // Set Values and Execute
        preparedStatement.setInt(1, id);        // set id
        preparedStatement.setString(2, name);   // set name
        preparedStatement.executeUpdate(); // execute

        // Close resources
        preparedStatement.close();
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
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "UPDATE users SET birthdate = ? WHERE id = ?"
            );

            // Set Values and Execute
            preparedStatement.setString(1, birthdate.getSQLDate());
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
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
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT id, birthdate FROM users WHERE birthdate IS NOT NULL"
            );

            // Execute
            ResultSet resultSet = preparedStatement.executeQuery();

            // Add Birthdays
            while (resultSet.next()) {
                var id = resultSet.getInt("id");
                String[] date = resultSet.getString("birthdate").split("\\.");
                String birthday = date[2] + "." + date[1] + "." + date[0];
                birthdays.put(id, new Birthdate(birthday));
            }

            // Close resources
            resultSet.close();
            preparedStatement.close();

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