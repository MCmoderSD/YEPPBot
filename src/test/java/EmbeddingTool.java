import de.MCmoderSD.json.JsonUtility;
import de.MCmoderSD.openai.core.OpenAI;
import de.MCmoderSD.openai.prompts.EmbeddingPrompt;
import de.MCmoderSD.openai.services.EmbeddingService;
import de.MCmoderSD.sql.Driver;

import tools.jackson.databind.JsonNode;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static de.MCmoderSD.sql.Driver.DatabaseType.*;
import static de.MCmoderSD.tools.GZIP.deflateObject;
import static de.MCmoderSD.utilities.Hasher.xxHash64;
import static java.math.BigDecimal.ZERO;

void main() {

    // Load Config
    JsonNode config = JsonUtility.getInstance().loadResource("/Database.json");

    // Initialize SQL
    SQL sql = new SQL(SQL.builder()
            .withType(MARIADB)
            .withHost(config.get("host").asString())
            .withPort(config.get("port").asInt())
            .withDatabase(config.get("database").asString())
            .withUsername(config.get("username").asString())
            .withPassword(config.get("password").asString())
    );

    // Initialize OpenAI
    EmbeddingService service = new OpenAI("sk-proj-").embeddings();

    // Telemetry
    var processed = 0;
    long totalTokens = 0;
    long promptTokens = 0;
    BigDecimal totalCost = ZERO;

    // Loop Through Content
    HashSet<String> unEmbeddedContent = sql.getUnEmbeddedContent();
    for (var content : unEmbeddedContent) {
        try {

            // Create Embedding
            EmbeddingPrompt prompt = service.create(content);

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
    IO.println("\nProcessed: " + processed + "/" + unEmbeddedContent.size());
    IO.println("Prompt tokens: " + promptTokens);
    IO.println("Total tokens: " + totalTokens);
    IO.println("Total cost: " + totalCost);
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
            PreparedStatement preparedStatement = getConnection().prepareStatement(
                    "SELECT content FROM MessageContent WHERE hash NOT IN (SELECT hash FROM Embedding);"
            );

            // Execute Query
            ResultSet resultSet = preparedStatement.executeQuery();

            // Collect Results
            HashSet<String> unEmbeddedContent = new HashSet<>();
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
                byte[] contentHash = xxHash64(prompt.getText());
                byte[] embedding = deflateObject(prompt.getEmbedding().getVector());

                // Insert embedding
                PreparedStatement insertEmbeddingStatement = connection.prepareStatement(
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