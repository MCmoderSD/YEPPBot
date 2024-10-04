package de.MCmoderSD.utilities.database;

import com.sun.jdi.request.StepRequest;
import de.MCmoderSD.main.Main;
import de.MCmoderSD.objects.Birthdate;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.manager.AssetManager;
import de.MCmoderSD.utilities.database.manager.ChannelManager;
import de.MCmoderSD.utilities.database.manager.CustomManager;
import de.MCmoderSD.utilities.database.manager.LogManager;
import de.MCmoderSD.utilities.database.manager.LurkManager;
import de.MCmoderSD.utilities.database.manager.TokenManager;
import de.MCmoderSD.utilities.database.manager.YEPPConnect;

import javax.management.InvalidAttributeValueException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

@SuppressWarnings("unused")
public class MySQL extends Driver {

    // Managers
    private final AssetManager assetManager;
    private final ChannelManager channelManager;
    private final CustomManager customManager;
    private final LogManager logManager;
    private final LurkManager lurkManager;
    private final TokenManager tokenManager;
    private final YEPPConnect yeppConnect;

    // Cache Lists
    private final HashMap<Integer, String> userCache;
    private final HashMap<Integer, String> channelCache;

    // Constructor
    public MySQL(Main main) {

        // Initialize Driver
        super(main.getCredentials().getMySQLConfig());

        // Connect to database
        new Thread(this::connect).start();
        initTables();

        // Load Cache
        userCache = loadCache(false);
        channelCache = loadCache(true);

        // Initialize Manager
        assetManager = new AssetManager(this);
        channelManager = new ChannelManager(this);
        customManager = new CustomManager(this);
        logManager = new LogManager(this, !main.hasArg(Main.Argument.LOG));
        lurkManager = new LurkManager(this);
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
                            id INT PRIMARY KEY NOT NULL,
                            name VARCHAR(25) NOT NULL,
                            birthdate VARCHAR(10)
                            )
                            """
            ).execute();

            // SQL statement for creating the channels table
            connection.prepareStatement(condition +
                            """
                            channels (
                            id INT PRIMARY KEY NOT NULL,
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
    private HashMap<Integer, String> loadCache(boolean isChannel) {

        // Variables
        HashMap<Integer, String> cache = new HashMap<>();

        // Load Channel Cache
        try {
            if (!isConnected()) connect(); // connect

            // Query
            String query;
            if (isChannel) query = "SELECT * FROM " + "channels";
            else query = "SELECT * FROM " + "users";

            // Execute Query
            PreparedStatement preparedStatement = getConnection().prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) cache.put(resultSet.getInt("id"), resultSet.getString("name")); // add to cache
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return cache;
    }

    // Checks Channels
    private void checkUser(int id, String name, boolean isChannel) throws SQLException {
        if (!isConnected()) connect(); // connect

        // Check Channel
        String selectQuery;
        if (isChannel) selectQuery = "SELECT * FROM channels WHERE id = ?";
        else selectQuery = "SELECT * FROM users WHERE id = ?";

        PreparedStatement selectPreparedStatement = connection.prepareStatement(selectQuery);
        selectPreparedStatement.setInt(1, id);
        ResultSet resultSet = selectPreparedStatement.executeQuery();

        // Add Channel
        if (!resultSet.next()) {
            String insertQuery;
            if (isChannel) insertQuery = "INSERT INTO channels (id, name) VALUES (?, ?)";
            else insertQuery = "INSERT INTO users (id, name) VALUES (?, ?)";
            PreparedStatement insertPreparedStatement = connection.prepareStatement(insertQuery);
            insertPreparedStatement.setInt(1, id); // set id
            insertPreparedStatement.setString(2, name); // set name
            insertPreparedStatement.executeUpdate(); // execute
            insertPreparedStatement.close(); // close the insertPreparedStatement
        }

        // Add to Cache
        if (isChannel) channelCache.put(id, name);
        else userCache.put(id, name);

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
            checkUser(id, name, false);
            userCache.put(id, name);
        }

        // Update Channel Cache
        if (!channel && isChannel) {
            checkUser(id, name, true);
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

            // Get Birthday
            String[] date = birthdate.getDate().split("\\.");
            String birthday = date[2] + "." + date[1] + "." + date[1];

            // Update Birthday
            String updateQuery = "UPDATE users SET birthdate = ? WHERE id = ?";
            PreparedStatement updatePreparedStatement = connection.prepareStatement(updateQuery);
            updatePreparedStatement.setString(1, birthday);
            updatePreparedStatement.setInt(2, id);
            updatePreparedStatement.executeUpdate(); // execute
            updatePreparedStatement.close(); // close the updatePreparedStatement
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Get Birthdays
    public HashMap<Integer, Birthdate> getBirthdays() {
        try {
            if (!isConnected()) connect(); // connect

            // Variables
            HashMap<Integer, Birthdate> birthdays = new HashMap<>();

            // Query
            String query = "SELECT * FROM users WHERE birthdate IS NOT NULL";
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

    public LogManager getLogManager() {
        return logManager;
    }

    public LurkManager getLurkManager() {
        return lurkManager;
    }

    public TokenManager getTokenManager() {
        return tokenManager;
    }

    public YEPPConnect getYEPPConnect() {
        return yeppConnect;
    }
}