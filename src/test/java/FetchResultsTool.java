import de.MCmoderSD.bdsm.core.BdsmTestApi;
import de.MCmoderSD.bdsm.data.TestResult;
import de.MCmoderSD.json.JsonUtility;
import de.MCmoderSD.sql.Driver;
import de.MCmoderSD.tools.GZIP;

import java.sql.SQLException;

import static de.MCmoderSD.sql.Driver.DatabaseType.MARIADB;
import static de.MCmoderSD.bdsm.enums.Kink.*;

void main() {

    // Initialize BDSM Test API
    var api = new BdsmTestApi();

    // Load Config
    var config = JsonUtility.getInstance().loadResource("/Database.json");

    // Initialize SQL
    var sql = new SQL(SQL.builder()
            .withType(MARIADB)
            .withHost(config.get("host").asString())
            .withPort(config.get("port").asInt())
            .withDatabase(config.get("database").asString())
            .withUsername(config.get("username").asString())
            .withPassword(config.get("password").asString())
    );

    // Fetch Result IDs from Database
    var resultIds = sql.getResultIds();
    var testResults = new HashSet<TestResult>();

    // Fetch Test Results from API
    for (var resultId : resultIds) {
        var testResult = api.fetchResult(resultId);
        if (testResult == null) throw new RuntimeException("Failed to fetch test result for ID: " + resultId);
        testResults.add(testResult);
    }

    // Update Database
    sql.updateResults(testResults);
}

// SQL Driver Implementation
private static class SQL extends Driver {

    // Constructor
    public SQL(Builder builder) {

        // Initialize Driver
        super(builder);

        // Connect to Database
        connect();
    }

    public HashSet<String> getResultIds() {
        try {

            // Prepare the SQL statement
            var preparedStatement = connection.prepareStatement(
                    "SELECT id FROM BDSM"
            );

            // Execute the query
            var resultSet = preparedStatement.executeQuery();

            // Collect the result IDs
            var resultIds = new HashSet<String>();
            while (resultSet.next()) resultIds.add(resultSet.getString("id"));

            // Close resources
            resultSet.close();
            preparedStatement.close();

            // Return IDs
            return resultIds;

        } catch (SQLException e) {
            throw new RuntimeException("Error occurred while fetching result IDs", e);
        }
    }

    public void updateResults(HashSet<TestResult> testResults) {
        try {

            // Prepare the SQL statement
            var preparedStatement = connection.prepareStatement(
                    "UPDATE BDSM SET timestamp = ?, version = ?, gender = ?, ageGroup = ?, data = ?, ageplayer = ?, brat = ?, bratTamer = ?, daddyMommy = ?, degrader = ?, dominant = ?, degradee = ?, little = ?, masochist = ?, masterMistress = ?, nonMonogamist = ?, owner = ?, primalHunter = ?, pet = ?, primalPrey = ?, rigger = ?, ropeBunny = ?, sadist = ?, slave = ?, submissive = ?, switch = ?, vanilla = ?, voyeur = ?, exhibitionist = ?, experimentalist = ? WHERE id = ?"
            );

            for (var  testResult : testResults) {

                // Compress the data
                var data = GZIP.deflateObject(testResult);

                // Get the score map
                var scoreMap = testResult.getScoreMap();

                // Set parameters
                preparedStatement.setTimestamp(1, testResult.getTimestamp());
                preparedStatement.setInt(2, testResult.getVersion());
                preparedStatement.setString(3, testResult.getGender());
                preparedStatement.setInt(4, testResult.getAgeGroup().getId());
                preparedStatement.setBytes(5, data);
                preparedStatement.setDouble(6, scoreMap.get(Ageplayer) / 100d);
                preparedStatement.setDouble(7, scoreMap.get(Brat) / 100d);
                preparedStatement.setDouble(8, scoreMap.get(BratTamer) / 100d);
                preparedStatement.setDouble(9, scoreMap.get(DaddyMommy) / 100d);
                preparedStatement.setDouble(10, scoreMap.get(Degrader) / 100d);
                preparedStatement.setDouble(11, scoreMap.get(Dominant) / 100d);
                preparedStatement.setDouble(12, scoreMap.get(Degradee) / 100d);
                preparedStatement.setDouble(13, scoreMap.get(Little) / 100d);
                preparedStatement.setDouble(14, scoreMap.get(Masochist) / 100d);
                preparedStatement.setDouble(15, scoreMap.get(MasterMistress) / 100d);
                preparedStatement.setDouble(16, scoreMap.get(NonMonogamist) / 100d);
                preparedStatement.setDouble(17, scoreMap.get(Owner) / 100d);
                preparedStatement.setDouble(18, scoreMap.get(PrimalHunter) / 100d);
                preparedStatement.setDouble(19, scoreMap.get(Pet) / 100d);
                preparedStatement.setDouble(20, scoreMap.get(PrimalPrey) / 100d);
                preparedStatement.setDouble(21, scoreMap.get(Rigger) / 100d);
                preparedStatement.setDouble(22, scoreMap.get(RopeBunny) / 100d);
                preparedStatement.setDouble(23, scoreMap.get(Sadist) / 100d);
                preparedStatement.setDouble(24, scoreMap.get(Slave) / 100d);
                preparedStatement.setDouble(25, scoreMap.get(Submissive) / 100d);
                preparedStatement.setDouble(26, scoreMap.get(Switch) / 100d);
                preparedStatement.setDouble(27, scoreMap.get(Vanilla) / 100d);
                preparedStatement.setDouble(28, scoreMap.get(Voyeur) / 100d);
                preparedStatement.setDouble(29, scoreMap.get(Exhibitionist) / 100d);
                preparedStatement.setDouble(30, scoreMap.get(Experimentalist) / 100d);
                preparedStatement.setString(31, testResult.getId());

                // Execute the update
                preparedStatement.addBatch();
            }

            // Execute the batch update
            preparedStatement.executeBatch();

            // Close resources
            preparedStatement.close();

        } catch (SQLException | IOException e) {
            throw new RuntimeException("Error occurred while adding Test Result", e);
        }
    }
}