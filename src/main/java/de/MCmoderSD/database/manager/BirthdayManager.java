package de.MCmoderSD.database.manager;

import de.MCmoderSD.data.Birthdate;
import de.MCmoderSD.database.Database;
import de.MCmoderSD.helix.objects.TwitchUser;

import java.sql.SQLException;
import java.util.HashMap;

import static de.MCmoderSD.utilities.ZipUtil.inflateTwitchUser;

public class BirthdayManager {

    // Associations
    private final Database database;

    // Constructor
    public BirthdayManager(Database database) {

        // Check Parameters
        if (database == null) throw new IllegalArgumentException("Database cannot be null");

        // Set Associations
        this.database = database;
    }

    // Add or Update Birthday
    public void addBirthday(Birthdate birthdate, TwitchUser user) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (birthdate == null) throw new IllegalArgumentException("Birthdate cannot be null");
                if (user == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

                // Delete existing birthday
                deleteBirthday(user);

                // Prepare the query
                var addBirthdayStatement = database.getConnection().prepareStatement(
                        "INSERT INTO Birthday (id, day, month, year) VALUES (?, ?, ?, ?);"
                );

                // Set the query parameters
                addBirthdayStatement.setInt(1, user.getId());       // User ID
                addBirthdayStatement.setInt(2, birthdate.day());    // Day
                addBirthdayStatement.setInt(3, birthdate.month());  // Month
                addBirthdayStatement.setInt(4, birthdate.year());   // Year

                // Execute the query
                addBirthdayStatement.executeUpdate();

                // Close resources
                addBirthdayStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to add birthday", e);
            }
        }).start();
    }

    // Delete Birthday
    public void deleteBirthday(TwitchUser user) {
        try {

            // Check Parameters
            if (user == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

            // Prepare the query
            var deleteBirthdayStatement = database.getConnection().prepareStatement(
                    "DELETE IGNORE FROM Birthday WHERE id = ?;"
            );

            // Set the query parameter
            deleteBirthdayStatement.setInt(1, user.getId()); // User ID

            // Execute the query
            deleteBirthdayStatement.executeUpdate();

            // Close resources
            deleteBirthdayStatement.close();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete birthday", e);
        }
    }

    // Get All Birthdays
    public HashMap<TwitchUser, Birthdate> getBirthdays() {
        try {

            // Prepare the query
            var getAllBirthdaysStatement = database.getConnection().prepareStatement(
                    "SELECT user, day, month, year FROM Birthday JOIN User ON Birthday.id = User.id;"
            );

            // Execute the query
            var resultSet = getAllBirthdaysStatement.executeQuery();

            // Prepare the result map
            HashMap<TwitchUser, Birthdate> birthdays = new HashMap<>();

            // Process the results
            while (resultSet.next()) {

                // Retrieve data
                var user = inflateTwitchUser(resultSet.getBytes("user"));
                var day = resultSet.getInt("day");
                var month = resultSet.getInt("month");
                var year = resultSet.getInt("year");

                // Create Birthdate
                Birthdate birthdate = new Birthdate(day, month, year);

                // Store in map
                birthdays.put(user, birthdate);
            }

            // Close resources
            resultSet.close();
            getAllBirthdaysStatement.close();

            // Return Birthdays
            return birthdays;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve birthdays", e);
        }
    }

    // Get Birthday for User
    public Birthdate getBirthday(TwitchUser user) {
        try {

            // Check Parameters
            if (user == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

            // Prepare the query
            var preparedStatement = database.getConnection().prepareStatement(
                    "SELECT day, month, year FROM Birthday WHERE id = ?;"
            );

            // Set the query parameter
            preparedStatement.setInt(1, user.getId()); // User ID

            // Execute the query
            var resultSet = preparedStatement.executeQuery();

            // Variable
            Birthdate birthdate = null;

            // Process the result
            if (resultSet.next()) {

                // Retrieve data
                var day = resultSet.getInt("day");
                var month = resultSet.getInt("month");
                var year = resultSet.getInt("year");

                // Create Birthdate
                birthdate = new Birthdate(day, month, year);
            }

            // Close resources
            resultSet.close();
            preparedStatement.close();

            // Return Birthday
            return birthdate;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve birthday", e);
        }
    }
}