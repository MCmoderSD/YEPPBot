import de.MCmoderSD.json.JsonUtility;
import de.MCmoderSD.openai.core.OpenAI;
import de.MCmoderSD.openai.prompts.EmbeddingPrompt;
import de.MCmoderSD.sql.Driver;

import java.sql.SQLException;

import static de.MCmoderSD.sql.Driver.DatabaseType.*;
import static de.MCmoderSD.tools.GZIP.deflateObject;
import static de.MCmoderSD.utilities.Hasher.xxHash64;
import static java.lang.IO.println;
import static java.math.BigDecimal.ZERO;

void main() {

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

    // Initialize OpenAI
    var service = new OpenAI("sk-proj-").embeddings();

    // Telemetry
    var processed = 0;
    var totalTokens = 0L;
    var promptTokens = 0L;
    var totalCost = ZERO;

    // Loop Through Content
    var unEmbeddedContent = sql.getUnEmbeddedContent();
    for (var content : unEmbeddedContent) {
        try {

            // Create Embedding
            var prompt = service.create(content);

            // Add Telemetry
            totalTokens += prompt.getTotalTokens();
            promptTokens += prompt.getPromptTokens();
            totalCost = totalCost.add(prompt.getTotalCost());
            processed++;

            // Update Database
            sql.insertEmbedding(prompt);

            // Console Log
            System.out.printf("[%d/%d] %s%n", processed, unEmbeddedContent.size(), content);

        } catch (Exception e) {
            System.err.println("Failed to process content: " + e.getMessage());
            break;
        }
    }

    // Print Telemetry
    println("\nProcessed: " + processed + "/" + unEmbeddedContent.size());
    println("Prompt tokens: " + promptTokens);
    println("Total tokens: " + totalTokens);
    println("Total cost: " + totalCost);
}

// SQL Driver Implementation
private static class SQL extends Driver {

    // Constructor
    public SQL(SQL.Builder builder) {

        // Call super
        super(builder);

        // Connect
        connect();
    }

    // Get Unembedded Content Method
    public HashSet<String> getUnEmbeddedContent() {
        try {

            // Prepare Statement
            var preparedStatement = getConnection().prepareStatement(
                    "SELECT content FROM MessageContent WHERE hash NOT IN (SELECT hash FROM Embedding);"
            );

            // Execute Query
            var resultSet = preparedStatement.executeQuery();

            // Collect Results
            var unEmbeddedContent = new HashSet<String>();
            while (resultSet.next()) unEmbeddedContent.add(resultSet.getString("content"));

            // Close Resources
            resultSet.close();
            preparedStatement.close();

            // Return Results
            return unEmbeddedContent;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve unembedded content: " + e.getMessage(), e);
        }
    }

    // Insert Embedding Method
    public void insertEmbedding(EmbeddingPrompt prompt) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (prompt == null) throw new IllegalArgumentException("Prompt cannot be null");

                // Variables
                var contentHash = xxHash64(prompt.getText());
                var embedding = deflateObject(prompt.getEmbedding().getVector());

                // Insert embedding
                var insertEmbeddingStatement = connection.prepareStatement(
                        "INSERT INTO Embedding (hash, dimension, embedding) VALUES (?, ?, ?);"
                );

                // Set the insert values
                insertEmbeddingStatement.setBytes(1, contentHash);          // Content Hash
                insertEmbeddingStatement.setInt(2, prompt.getDimension());  // Dimension
                insertEmbeddingStatement.setBytes(3, embedding);            // Embedding (compressed)

                // Execute the insert
                insertEmbeddingStatement.executeUpdate();

            } catch (SQLException | IOException e) {
                throw new RuntimeException("Failed to insert embedding: " + e.getMessage(), e);
            }
        }).start();
    }
}