package de.MCmoderSD.database.manager;


import de.MCmoderSD.bdsm.data.TestResult;
import de.MCmoderSD.database.Database;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.tools.GZIP;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import static de.MCmoderSD.bdsm.enums.Kink.*;
import static de.MCmoderSD.utilities.ZipUtil.*;

public class BdsmManager {

    // Associations
    private final Database database;

    // Constructor
    public BdsmManager(Database database) {

        // Check Parameters
        if (database == null) throw new IllegalArgumentException("Database cannot be null");

        // Set Associations
        this.database = database;
    }

    public void addTestResult(TestResult testResult, TwitchUser twitchUser) {

        // Check Parameters
        if (testResult == null) throw new IllegalArgumentException("TestResult cannot be null");
        if (twitchUser == null) throw new IllegalArgumentException("TwitchUser cannot be null");

        try {

            // Variables
            var scoreMap = testResult.getScoreMap();
            var data = GZIP.deflateObject(testResult);

            // Prepare the SQL statement
            var preparedStatement = database.getConnection().prepareStatement(
                    "INSERT INTO BDSM (id, user, timestamp, version, gender, ageGroup, data, ageplayer, brat, bratTamer, daddyMommy, degrader, dominant, degradee, little, masochist, masterMistress, nonMonogamist, owner, primalHunter, pet, primalPrey, rigger, ropeBunny, sadist, slave, submissive, switch, vanilla, voyeur, exhibitionist, experimentalist) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );

            // Set the parameters for the prepared statement
            preparedStatement.setString(1, testResult.getId());
            preparedStatement.setInt(2, twitchUser.getId());
            preparedStatement.setTimestamp(3, testResult.getTimestamp());
            preparedStatement.setInt(4, testResult.getVersion());
            preparedStatement.setString(5, testResult.getGender());
            preparedStatement.setInt(6, testResult.getAgeGroup().getId());
            preparedStatement.setBytes(7, data);
            preparedStatement.setDouble(8, scoreMap.get(Ageplayer) / 100d);
            preparedStatement.setDouble(9, scoreMap.get(Brat) / 100d);
            preparedStatement.setDouble(10, scoreMap.get(BratTamer) / 100d);
            preparedStatement.setDouble(11, scoreMap.get(DaddyMommy) / 100d);
            preparedStatement.setDouble(12, scoreMap.get(Degrader) / 100d);
            preparedStatement.setDouble(13, scoreMap.get(Dominant) / 100d);
            preparedStatement.setDouble(14, scoreMap.get(Degradee) / 100d);
            preparedStatement.setDouble(15, scoreMap.get(Little) / 100d);
            preparedStatement.setDouble(16, scoreMap.get(Masochist) / 100d);
            preparedStatement.setDouble(17, scoreMap.get(MasterMistress) / 100d);
            preparedStatement.setDouble(18, scoreMap.get(NonMonogamist) / 100d);
            preparedStatement.setDouble(19, scoreMap.get(Owner) / 100d);
            preparedStatement.setDouble(20, scoreMap.get(PrimalHunter) / 100d);
            preparedStatement.setDouble(21, scoreMap.get(Pet) / 100d);
            preparedStatement.setDouble(22, scoreMap.get(PrimalPrey) / 100d);
            preparedStatement.setDouble(23, scoreMap.get(Rigger) / 100d);
            preparedStatement.setDouble(24, scoreMap.get(RopeBunny) / 100d);
            preparedStatement.setDouble(25, scoreMap.get(Sadist) / 100d);
            preparedStatement.setDouble(26, scoreMap.get(Slave) / 100d);
            preparedStatement.setDouble(27, scoreMap.get(Submissive) / 100d);
            preparedStatement.setDouble(28, scoreMap.get(Switch) / 100d);
            preparedStatement.setDouble(29, scoreMap.get(Vanilla) / 100d);
            preparedStatement.setDouble(30, scoreMap.get(Voyeur) / 100d);
            preparedStatement.setDouble(31, scoreMap.get(Exhibitionist) / 100d);
            preparedStatement.setDouble(32, scoreMap.get(Experimentalist) / 100d);

            // Execute the prepared statement
            preparedStatement.executeUpdate();

            // Close resources
            preparedStatement.close();

        } catch (SQLException | IOException e) {
            throw new RuntimeException("Error occurred while adding Test Result", e);
        }
    }

    public ArrayList<TestResult> getTestResults(TwitchUser twitchUser) {

        // Check Parameters
        if (twitchUser == null) throw new IllegalArgumentException("TwitchUser cannot be null");

        try {

            // Prepare the SQL statement
            var preparedStatement = database.getConnection().prepareStatement(
                    "SELECT * FROM BDSM WHERE user = ? ORDER BY timestamp DESC"
            );

            // Set the parameter for the prepared statement
            preparedStatement.setInt(1, twitchUser.getId());

            // Execute the query and get the result set
            var resultSet = preparedStatement.executeQuery();

            // Create a list to hold the test results
            var testResults = new ArrayList<TestResult>();

            // Iterate through the result set and create TestResult objects
            while (resultSet.next()) {
                var data = resultSet.getBytes("data");
                var testResult = inflateTestResult(data);
                testResults.add(testResult);
            }

            // Close resources
            resultSet.close();
            preparedStatement.close();

            // Return
            return testResults;

        } catch (SQLException e) {
            throw new RuntimeException("Error occurred while retrieving Test Results", e);
        }
    }

    public HashMap<TwitchUser, TestResult> getLatestTestResults() {

        try {

            // Prepare the SQL statement
            var preparedStatement = database.getConnection().prepareStatement(
                    "SELECT u.user, b1.data FROM BDSM b1 JOIN User u ON b1.user = u.id WHERE timestamp = (SELECT MAX(timestamp) FROM BDSM b2 WHERE b1.user = b2.user)"
            );

            // Execute the query and get the result set
            var resultSet = preparedStatement.executeQuery();

            // Create a map to hold the latest test results
            var latestTestResults = new HashMap<TwitchUser, TestResult>();

            // Iterate through the result set and create TestResult objects
            while (resultSet.next()) {
                var twitchUser = inflateTwitchUser(resultSet.getBytes("user"));
                var testResult = inflateTestResult(resultSet.getBytes("data"));
                latestTestResults.put(twitchUser, testResult);
            }

            // Close resources
            resultSet.close();
            preparedStatement.close();

            // Return
            return latestTestResults;

        } catch (SQLException e) {
            throw new RuntimeException("Error occurred while retrieving latest Test Results", e);
        }
    }
}