package de.MCmoderSD.utilities.database;

import de.MCmoderSD.main.Main;
import de.MCmoderSD.utilities.database.manager.BlackListManager;
import de.MCmoderSD.utilities.database.manager.ChannelManager;
import de.MCmoderSD.utilities.database.manager.LogManager;
import de.MCmoderSD.utilities.database.manager.LurkManager;

import java.sql.*;

@SuppressWarnings("unused")
public class MySQL extends Driver {

    // Associations
    private final BlackListManager blackListManager;
    private final ChannelManager channelManager;
    private final LogManager logManager;
    private final LurkManager lurkManager;

    // Constructor
    public MySQL(Main main) {

        // Initialize Driver
        super(main.getCredentials().getMySQLConfig());

        // Connect to database
        new Thread(this::connect).start();
        initTables();

        // Initialize Managers
        blackListManager = new BlackListManager(this);
        channelManager = new ChannelManager(this);
        logManager = new LogManager(this, main.hasArg("log"));
        lurkManager = new LurkManager(this);
    }

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

            // SQL statement for creating the message log table
            connection.prepareStatement(condition +
                            """
                            MessageLog (
                            timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            type VARCHAR(5) NOT NULL,
                            channel_id INT NOT NULL,
                            user_id INT NOT NULL,
                            message VARCHAR(500),
                            bits INT NOT NULL DEFAULT 0,
                            subMonths INT NOT NULL DEFAULT 0,
                            subStreak INT NOT NULL DEFAULT 0,
                            subPlan VARCHAR(5) NOT NULL DEFAULT 'NONE',
                            FOREIGN KEY (channel_id) REFERENCES channels(id),
                            FOREIGN KEY (user_id) REFERENCES users(id)
                            )
                            """
            ).execute();

            // SQL statement for creating the command log table
            connection.prepareStatement(condition +
                            """
                            CommandLog (
                            timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            channel_id INT NOT NULL,
                            user_id INT NOT NULL,
                            command TEXT NOT NULL,
                            args VARCHAR(500),
                            bits INT NOT NULL DEFAULT 0,
                            subMonths INT NOT NULL DEFAULT 0,
                            subStreak INT NOT NULL DEFAULT 0,
                            subPlan VARCHAR(5) NOT NULL DEFAULT 'NONE',
                            FOREIGN KEY (channel_id) REFERENCES channels(id),
                            FOREIGN KEY (user_id) REFERENCES users(id)
                            )
                            """
            ).execute();

            // SQL statement for creating the response log table
            connection.prepareStatement(condition +
                            """
                            ResponseLog (
                            timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            channel_id INT NOT NULL,
                            user_id INT NOT NULL,
                            command TEXT NOT NULL,
                            args VARCHAR(500),
                            response VARCHAR(500),
                            bits INT NOT NULL DEFAULT 0,
                            subMonths INT NOT NULL DEFAULT 0,
                            subStreak INT NOT NULL DEFAULT 0,
                            subPlan VARCHAR(5) NOT NULL DEFAULT 'NONE',
                            FOREIGN KEY (channel_id) REFERENCES channels(id),
                            FOREIGN KEY (user_id) REFERENCES users(id)
                            )
                            """
            ).execute();

            // SQL statement for creating the lurk list table
            connection.prepareStatement(condition +
                            """
                            lurkList (
                            user_id INT PRIMARY KEY,
                            lurkChannel_ID INT NOT NULL,
                            startTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            traitorChannel TEXT,
                            FOREIGN KEY (lurkChannel_ID) REFERENCES channels(id)
                            )
                            """
            ).execute();

            // SQL statement for creating the custom timers table
            connection.prepareStatement(condition +
                            """
                            CustomTimers (
                            channel_id INT NOT NULL,
                            name TEXT NOT NULL,
                            time TEXT NOT NULL,
                            response VARCHAR(500) NOT NULL,
                            isEnabled BIT NOT NULL DEFAULT 1,
                            FOREIGN KEY (channel_id) REFERENCES channels(id)
                            )
                            """
            ).execute();

            // SQL statement for creating the custom commands table
            connection.prepareStatement(condition +
                            """
                            CustomCommands (
                            channel_id INT NOT NULL,
                            command_name TEXT NOT NULL,
                            command_alias TEXT,
                            command_response VARCHAR(500) NOT NULL,
                            isEnabled BIT NOT NULL DEFAULT 1,
                            FOREIGN KEY (channel_id) REFERENCES channels(id)
                            )
                            """
            ).execute();

            // SQL statement for creating the counters table
            connection.prepareStatement(condition +
                            """
                            Counters (
                            channel_id INT NOT NULL,
                            name TEXT NOT NULL,
                            value INT NOT NULL,
                            FOREIGN KEY (channel_id) REFERENCES channels(id)
                            )
                            """
            ).execute();

            // SQL statement for creating the fact list table
            connection.prepareStatement(condition +
                            """
                            factList (
                            fact_id INT PRIMARY KEY AUTO_INCREMENT,
                            en_percent varchar(500),
                            en_people varchar(500),
                            en_verb varchar(500),
                            en_frequency varchar(500),
                            en_adjective varchar(500),
                            en_object varchar(500),
                            de_percent varchar(500),
                            de_people varchar(500),
                            de_verb varchar(500),
                            de_frequency varchar(500),
                            de_adjective varchar(500),
                            de_object varchar(500)
                            )
                            """
            ).execute();

            // SQL statement for creating the joke list table
            connection.prepareStatement(condition +
                            """
                            jokeList (
                            joke_id INT PRIMARY KEY AUTO_INCREMENT,
                            en varchar(500),
                            de varchar(500)
                            )
                            """
            ).execute();

            // SQL statement for creating the insult list table
            connection.prepareStatement(condition +
                            """
                            insultList (
                            insult_id INT PRIMARY KEY AUTO_INCREMENT,
                            en varchar(500) NOT NULL,
                            de varchar(500) NOT NULL
                            )
                            """
            ).execute();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
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

    // Get Manager
    public BlackListManager getBlackListManager() {
        return blackListManager;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public LurkManager getLurkManager() {
        return lurkManager;
    }
}