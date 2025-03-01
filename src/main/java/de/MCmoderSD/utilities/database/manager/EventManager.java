package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.utilities.database.SQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.Calendar;
import java.util.HashMap;

@SuppressWarnings("SqlSourceToSinkFlow")
public class EventManager {

    // Associations
    private final SQL sql;

    // Constructor
    public EventManager(SQL sql) {

        // Get Associations
        this.sql = sql;

        // Initialize Tables
        initTables();
    }

    // Initialize Tables
    private void initTables() {
        try {

            // Variables
            Connection connection = sql.getConnection();

            // Create NNN and DDD tables
            String[] tables = {"NoNutNovember", "DickDestroyDecember"};
            for (String table : tables) {
                connection.prepareStatement(String.format(
                        """
                        CREATE TABLE IF NOT EXISTS %s (
                        year SMALLINT NOT NULL,
                        id INT NOT NULL,
                        joined DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        gaveUp DATETIME DEFAULT NULL
                        ) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=1 CHARSET=utf8mb4
                        """, table)).execute();
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public boolean joinEvent(Integer id, Event event) {
        try {
            if (!sql.isConnected()) sql.connect();

            // Check if user is already joined
            if (isJoined(id, event)) return false;

            // Add user to event
            PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                    String.format("INSERT INTO %s (year, id) VALUES (?, ?)", event.getTable())
            );

            // Set values and execute
            preparedStatement.setInt(1, Calendar.getInstance().get(Calendar.YEAR));
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();

            // Close resources
            preparedStatement.close();

            // Check if user is registered
            return isJoined(id, event);

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    public boolean leaveEvent(Integer id, Event event) {
        try {
            if (!sql.isConnected()) sql.connect();

            // Check if user is joined
            if (!isJoined(id, event)) return false;

            // Check if user has left
            if (hasLeft(id, event)) return false;

            // Add user to event
            PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                    String.format("UPDATE %s SET gaveUp = CURRENT_TIMESTAMP WHERE year = ? AND id = ?", event.getTable())
            );

            // Set values and execute
            preparedStatement.setInt(1, Calendar.getInstance().get(Calendar.YEAR));
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();

            // Close resources
            preparedStatement.close();

            // Check if user has left
            return hasLeft(id, event);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    public boolean isJoined(Integer id, Event event) {
        try {
            if (!sql.isConnected()) sql.connect();

            // Prepare statement
            PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                    String.format("SELECT * FROM %s WHERE year = ? AND id = ?", event.getTable())
            );

            // Set values and execute
            preparedStatement.setInt(1, Calendar.getInstance().get(Calendar.YEAR));
            preparedStatement.setInt(2, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    public boolean hasLeft(Integer id, Event event) {
        try {
            if (!sql.isConnected()) sql.connect();

            // Prepare statement
            PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                    String.format("SELECT * FROM %s WHERE year = ? AND id = ? AND gaveUp IS NOT NULL", event.getTable())
            );

            // Set values and execute
            preparedStatement.setInt(1, Calendar.getInstance().get(Calendar.YEAR));
            preparedStatement.setInt(2, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    public HashMap<Integer, Boolean> getParticipants(Event event) {
        try {
            if (!sql.isConnected()) sql.connect();

            // Variables
            HashMap<Integer, Boolean> participants = new HashMap<>();

            // Get participants
            PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                    String.format("SELECT id, gaveUp FROM %s WHERE year = ?", event.getTable())
            );

            // Set values and execute
            preparedStatement.setInt(1, Calendar.getInstance().get(Calendar.YEAR));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) participants.put(resultSet.getInt("id"), resultSet.getObject("gaveUp") == null);

            // Close resources
            preparedStatement.close();

            return participants;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public Timestamp[] getParticipant(Integer id, Event event) {
        try {
            if (!sql.isConnected()) sql.connect();

            // Get participants
            PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                    String.format("SELECT joined, gaveUp FROM %s WHERE year = ? AND id = ?", event.getTable())
            );

            // Set values and execute
            preparedStatement.setInt(1, Calendar.getInstance().get(Calendar.YEAR));
            preparedStatement.setInt(2, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return new Timestamp[]{resultSet.getTimestamp("joined"), resultSet.getTimestamp("gaveUp")};

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    // Events
    public enum Event {

        // Enumerations
        NNN("NoNutNovember", Calendar.NOVEMBER),
        DDD("DickDestroyDecember", Calendar.DECEMBER);

        // Variables
        private final String table;
        private final int month;

        // Constructor
        Event(String table, int month) {
            this.table = table;
            this.month = month;
        }

        // Getter
        public String getTable() {
            return table;
        }

        public int getMonth() {
            return month;
        }
    }
}