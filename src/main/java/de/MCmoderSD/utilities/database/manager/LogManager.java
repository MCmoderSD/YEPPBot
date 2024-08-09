package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

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
        this.mySQL = mySQL;
        this.log = log;
    }

    // Log Message
    public void logMessage(TwitchMessageEvent event) {
        if (log) new Thread(() -> {
            event.logToMySQL(this); // log to MySQL
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