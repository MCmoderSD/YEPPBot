import de.MCmoderSD.json.JsonUtility;
import de.MCmoderSD.openai.core.OpenAI;
import de.MCmoderSD.openai.prompts.ModerationPrompt;
import de.MCmoderSD.sql.Driver;

import java.sql.SQLException;

import static de.MCmoderSD.sql.Driver.DatabaseType.*;
import static de.MCmoderSD.tools.GZIP.deflateObject;
import static de.MCmoderSD.utilities.Hasher.xxHash64;
import static java.lang.IO.println;

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
    var service = new OpenAI("sk-proj-").moderations();

    // Telemetry
    var processed = 0;
    var flagged = 0;

    // Loop Through Content
    var unRatedContent = sql.getUnRatedContent();
    for (var content : unRatedContent) {
        try {

            // Create Rating
            var prompt = service.create(content);

            // Add Telemetry
            processed++;
            flagged += prompt.getRating().isFlagged() ? 1 : 0;

            // Update Database
            sql.insertRating(prompt);

            // Console Log
            System.out.printf("[%d/%d] %s%n", processed, unRatedContent.size(), content);

        } catch (Exception e) {
            System.err.println("Failed to process content: " + e.getMessage());
            break;
        }
    }

    // Print Telemetry
    println("\nProcessed: " + processed + "/" + unRatedContent.size());
    println("Flagged: " + flagged + " of " + processed);
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

    // Get Unrated Content Method
    public HashSet<String> getUnRatedContent() {
        try {

            // Prepare Statement
            var preparedStatement = getConnection().prepareStatement(
                    "SELECT content FROM MessageContent WHERE hash NOT IN (SELECT hash FROM Rating) OR hash NOT IN (SELECT hash FROM RatingFlag) OR hash NOT IN (SELECT hash FROM RatingScore);"
            );

            // Execute Query
            var resultSet = preparedStatement.executeQuery();

            // Collect Results
            var unRatedContent = new HashSet<String>();
            while (resultSet.next()) unRatedContent.add(resultSet.getString("content"));

            // Close Resources
            resultSet.close();
            preparedStatement.close();

            // Return Results
            return unRatedContent;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve unrated content: " + e.getMessage(), e);
        }
    }

    // Insert Rating Method
    public void insertRating(ModerationPrompt prompt) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (prompt == null) throw new IllegalArgumentException("Prompt cannot be null");

                // Variables
                var rating = prompt.getRating();
                var contentHash = xxHash64(prompt.getText());
                var ratingData = deflateObject(rating);

                // Insert rating
                var insertRatingStatement = connection.prepareStatement(
                        "INSERT INTO Rating (hash, flagged, rating) VALUES (?, ?, ?);"
                );

                insertRatingStatement.setBytes(1, contentHash);             // Content Hash
                insertRatingStatement.setBoolean(2, rating.isFlagged());    // Flagged
                insertRatingStatement.setBytes(3, ratingData);              // Rating (compressed)
                insertRatingStatement.executeUpdate();

                // Flags
                var harassment              = rating.getHarassment();               // Harassment
                var harassmentThreatening   = rating.getHarassmentThreatening();    // Harassment Threatening
                var hate                    = rating.getHate();                     // Hate
                var hateThreatening         = rating.getHateThreatening();          // Hate Threatening
                var illicit                 = rating.getIllicit();                  // Illicit
                var illicitViolent          = rating.getIllicitViolent();           // Illicit Violent
                var selfHarm                = rating.getSelfHarm();                 // Self-Harm
                var selfHarmInstructions    = rating.getSelfHarmInstructions();     // Self-Harm Instructions
                var selfHarmIntent          = rating.getSelfHarmIntent();           // Self-Harm Intent
                var sexual                  = rating.getSexual();                   // Sexual
                var sexualMinors            = rating.getSexualMinors();             // Sexual Minors
                var violence                = rating.getViolence();                 // Violence
                var violenceGraphic         = rating.getViolenceGraphic();          // Violence Graphic

                // Insert Rating Flags
                var insertFlagStatement = connection.prepareStatement(
                        "INSERT INTO RatingFlag (hash, harassment, harassmentThreatening, hate, hateThreatening, illicit, illicitViolent, selfHarm, selfHarmInstructions, selfHarmIntent, sexual, sexualMinors, violence, violenceGraphic) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                );

                insertFlagStatement.setBytes(1, contentHash);                       // Content Hash
                insertFlagStatement.setBoolean(2, harassment.flagged());            // Harassment
                insertFlagStatement.setBoolean(3, harassmentThreatening.flagged()); // Harassment Threatening
                insertFlagStatement.setBoolean(4, hate.flagged());                  // Hate
                insertFlagStatement.setBoolean(5, hateThreatening.flagged());       // Hate Threatening
                insertFlagStatement.setBoolean(6, illicit.flagged());               // Illicit
                insertFlagStatement.setBoolean(7, illicitViolent.flagged());        // Illicit Violent
                insertFlagStatement.setBoolean(8, selfHarm.flagged());              // Self-Harm
                insertFlagStatement.setBoolean(9, selfHarmInstructions.flagged());  // Self-Harm Instructions
                insertFlagStatement.setBoolean(10, selfHarmIntent.flagged());       // Self-Harm Intent
                insertFlagStatement.setBoolean(11, sexual.flagged());               // Sexual
                insertFlagStatement.setBoolean(12, sexualMinors.flagged());         // Sexual Minors
                insertFlagStatement.setBoolean(13, violence.flagged());             // Violence
                insertFlagStatement.setBoolean(14, violenceGraphic.flagged());      // Violence Graphic
                insertFlagStatement.executeUpdate();

                // Insert Rating Flags
                var insertScoreStatement = connection.prepareStatement(
                        "INSERT INTO RatingScore (hash, harassment, harassmentThreatening, hate, hateThreatening, illicit, illicitViolent, selfHarm, selfHarmInstructions, selfHarmIntent, sexual, sexualMinors, violence, violenceGraphic) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                );

                insertScoreStatement.setBytes(1, contentHash);                      // Content Hash
                insertScoreStatement.setDouble(2, harassment.score());              // Harassment
                insertScoreStatement.setDouble(3, harassmentThreatening.score());   // Harassment Threatening
                insertScoreStatement.setDouble(4, hate.score());                    // Hate
                insertScoreStatement.setDouble(5, hateThreatening.score());         // Hate Threatening
                insertScoreStatement.setDouble(6, illicit.score());                 // Illicit
                insertScoreStatement.setDouble(7, illicitViolent.score());          // Illicit Violent
                insertScoreStatement.setDouble(8, selfHarm.score());                // Self-Harm
                insertScoreStatement.setDouble(9, selfHarmInstructions.score());    // Self-Harm Instructions
                insertScoreStatement.setDouble(10, selfHarmIntent.score());         // Self-Harm Intent
                insertScoreStatement.setDouble(11, sexual.score());                 // Sexual
                insertScoreStatement.setDouble(12, sexualMinors.score());           // Sexual Minors
                insertScoreStatement.setDouble(13, violence.score());               // Violence
                insertScoreStatement.setDouble(14, violenceGraphic.score());        // Violence Graphic
                insertScoreStatement.executeUpdate();

            } catch (SQLException | IOException e) {
                throw new RuntimeException("Failed to insert rating: " + e.getMessage(), e);
            }
        }).start();
    }
}