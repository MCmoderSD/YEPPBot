package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static de.MCmoderSD.utilities.other.Calculate.USER;
import static de.MCmoderSD.utilities.other.Calculate.getTimestamp;

public class LogManager {
    
    // Assosiations
    private final MySQL mySQL;
    
    // Variables
    private boolean log;
    
    // Constructor
    public LogManager(MySQL mySQL, boolean log) {

        // Set associations
        this.mySQL = mySQL;
        this.log = log;

        // Initialize
        initTables();
    }

    // Initialize Tables
    private void initTables() {
        try {

            // Variables
            Connection connection = mySQL.getConnection();

            // Condition for creating tables
            String condition = "CREATE TABLE IF NOT EXISTS ";

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
        } catch (SQLException e) {
                System.err.println(e.getMessage());
            } 
    }

    // Log Message
    public void logMessage(TwitchMessageEvent event) {
        if (log) new Thread(() -> {
            event.logToMySQL(this); // log to MySQL
        }).start();
    }

    // Log Command
    public void logCommand(TwitchMessageEvent event, String trigger, String args) {
        if (log) new Thread(() -> {

            // Variables
            var channelID = event.getChannelId();
            var userID = event.getUserId();
            var channel = event.getChannel();
            var user = event.getUser();

            try {
                if (!mySQL.isConnected()) mySQL.connect(); // connect

                // Check Channel and User
                mySQL.checkCache(channelID, channel);
                mySQL.checkCache(userID, user);

                // Prepare statement
                String query = "INSERT INTO " + "CommandLog" + " (timestamp, channel_id, user_id, command, args, bits, subMonths, subStreak, subPlan) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
                preparedStatement.setTimestamp(1, event.getTimestamp()); // set timestamp
                preparedStatement.setInt(2, channelID); // set channel
                preparedStatement.setInt(3, userID); // set user
                preparedStatement.setString(4, trigger); // set command
                preparedStatement.setString(5, args); // set args
                preparedStatement.setInt(6, event.getLogBits()); // set bits
                preparedStatement.setInt(7, event.getLogSubMonths()); // set subMonths
                preparedStatement.setInt(8, event.getLogSubStreak()); // set subStreak
                preparedStatement.setString(9, event.getSubTier()); // set subPlan
                preparedStatement.executeUpdate(); // execute
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Log Command Response
    public void logResponse(TwitchMessageEvent event, String command, String response) {
        if (log) new Thread(() -> {

            // Variables
            var channelID = event.getChannelId();
            var userID = event.getUserId();
            var channel = event.getChannel();
            var user = event.getUser();

            try {
                if (!mySQL.isConnected()) mySQL.connect(); // connect

                // Check Channel and User
                mySQL.checkCache(channelID, channel);
                mySQL.checkCache(userID, user);

                // Prepare statement
                String query = "INSERT INTO " + "ResponseLog" + " (timestamp, channel_id, user_id, command, args, response, bits, subMonths, subStreak, subPlan) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
                preparedStatement.setTimestamp(1, event.getTimestamp()); // set timestamp
                preparedStatement.setInt(2, channelID); // set channel
                preparedStatement.setInt(3, userID); // set user
                preparedStatement.setString(4, command); // set command
                preparedStatement.setString(5, event.getMessage()); // set args
                preparedStatement.setString(6, response); // set response
                preparedStatement.setInt(7, event.getLogBits()); // set bits
                preparedStatement.setInt(8, event.getLogSubMonths()); // set subMonths
                preparedStatement.setInt(9, event.getLogSubStreak()); // set subStreak
                preparedStatement.setString(10, event.getSubTier()); // set subPlan
                preparedStatement.executeUpdate(); // execute
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Log Bot Response
    public void logResponse(String channel, String user, String message) {
        if (log) new Thread(() -> {

            // Variables
            var channelID = mySQL.queryID("channels", channel);
            var userID = mySQL.queryID("users", user);

            try {
                if (!mySQL.isConnected()) mySQL.connect(); // connect

                // Check Channel and User
                mySQL.checkCache(channelID, channel);
                mySQL.checkCache(userID, user);

                // Prepare statement
                String query = "INSERT INTO " + "ResponseLog" + " (timestamp, channel_id, user_id, command, args, response) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
                preparedStatement.setTimestamp(1, getTimestamp()); // set timestamp
                preparedStatement.setInt(2, channelID); // set channel
                preparedStatement.setInt(3, userID); // set user
                preparedStatement.setString(4, USER); // set command
                preparedStatement.setString(5, USER); // set args
                preparedStatement.setString(6, message); // set response
                preparedStatement.executeUpdate(); // execute
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Getter
    public MySQL getMySQL() {
        return mySQL;
    }

    public boolean isLog() {
        return log;
    }

    // Setter
    public void setLog(boolean log) {
        this.log = log;
    }
}