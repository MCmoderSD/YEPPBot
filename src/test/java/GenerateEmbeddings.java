import de.MCmoderSD.openai.core.OpenAI;
import de.MCmoderSD.openai.objects.EmbeddingPrompt;
import de.MCmoderSD.sql.Driver;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;

import static de.MCmoderSD.openai.model.EmbeddingModel.TEXT_EMBEDDING_3_LARGE;

@SuppressWarnings("BusyWait")
public class GenerateEmbeddings {

    // Main
    public static void main(String[] args) {

        // Loop until success
        while (!loop()) {
            try {
                System.err.println("Retrying...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // Loop
    private static boolean loop() {
        try {
            generate();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        } finally {
            System.out.println("Finished!");
        }
        return true;
    }

    // Main
    private static void generate() throws SQLException {

        // Variables
        String host = "localhost";
        var port = 3306; // SQL Default Port
        String database = "your_database";
        String username = "your_username";
        String password = "your_password";

        // OpenAI API Key
        String openAIKey = "your_openai_key";
        OpenAI openAI = new OpenAI(openAIKey);

        String user = "your_user";

        // Connect to the database
        System.out.println("Connecting to database...");
        Connection connection = DriverManager.getConnection(Driver.DatabaseType.MARIADB.getUrl(host, port, database), username, password);

        System.out.println("Connected to database!\n");
        System.out.println("Counting rows...");

        // SQL query to count rows with null ratingId
        ResultSet countResultSet = connection.createStatement().executeQuery("SELECT COUNT(*) FROM MessageLog m LEFT JOIN EventLog e ON m.id = e.id LEFT JOIN EmbeddingLog el ON e.id = el.id WHERE el.id IS NULL;");

        // Get the count
        var fetchSize = 0;
        if (countResultSet.next()) fetchSize = countResultSet.getInt(1);
        System.out.println("Rows: " + fetchSize + "\n");

        // Fetch rows with null ratingId
        System.out.println("Fetching rows...");
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT m.id, m.message FROM MessageLog m LEFT JOIN EventLog e ON m.id = e.id LEFT JOIN EmbeddingLog el ON e.id = el.id WHERE el.id IS NULL;");
        System.out.println("Fetched rows!\n");
        var done = 0;

        System.out.println("Processing rows...");

        // Iterate through the result set
        while (resultSet.next()) {
            var time = System.nanoTime(); // Start time

            // Prompt
            EmbeddingPrompt prompt = openAI.embedding(
                    TEXT_EMBEDDING_3_LARGE,
                    user,
                    3072L,
                    resultSet.getString("message")
            );

            // Insert into the database
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO EmbeddingLog (id, token, type, embedding) VALUES (?,?,?,?);"
            );

            // Set parameters
            preparedStatement.setString(1, resultSet.getString("id"));  // ID
            preparedStatement.setLong(2, prompt.getTotalTokens());              // Token
            preparedStatement.setString(3, prompt.getModel().toString());       // Type
            preparedStatement.setBytes(4, prompt.getEmbedding().getBytes());    // Embedding
            preparedStatement.executeUpdate(); // Execute the update

            // Print time taken
            done++;
            var delta = System.nanoTime() - time;
            System.out.println("Done: " + done + " / " + fetchSize + " | Time: " + delta / 1000000L + "ms" + " | " + new BigDecimal(done * 100d / fetchSize).setScale(2, RoundingMode.HALF_UP) + "%");
        }
    }
}