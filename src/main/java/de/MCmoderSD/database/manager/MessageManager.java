package de.MCmoderSD.database.manager;

import de.MCmoderSD.database.Database;
import de.MCmoderSD.openai.prompts.EmbeddingPrompt;
import de.MCmoderSD.openai.prompts.ModerationPrompt;

import java.io.IOException;
import java.sql.SQLException;

import static de.MCmoderSD.tools.GZIP.deflateObject;
import static de.MCmoderSD.utilities.Hasher.xxHash64;

public class MessageManager {

    // Associations
    private final Database database;

    // Constructor
    public MessageManager(Database database) {

        // Check Parameters
        if (database == null) throw new IllegalArgumentException("Database cannot be null");

        // Set Associations
        this.database = database;
    }

    // Add Message
    public boolean insertMessage(String content) {
        try {

            // Check Parameters
            if (content == null || content.isBlank()) throw new IllegalArgumentException("Content must not be null or blank");

            // Variables
            var hash = xxHash64(content);

            // Insert message content
            var insertContentStatement = database.getConnection().prepareStatement(
                    "INSERT IGNORE INTO MessageContent (hash, content) VALUES (?, ?);"
            );

            // Set the insert values
            insertContentStatement.setBytes(1, hash);       // Content Hash
            insertContentStatement.setString(2, content);   // Content

            // Return change
            return insertContentStatement.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert message content: " + e.getMessage(), e);
        }
    }

    // Add Embedding
    public void insertEmbedding(EmbeddingPrompt prompt) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (prompt == null) throw new IllegalArgumentException("Prompt cannot be null");

                // Variables
                var contentHash = xxHash64(prompt.getText());
                var embedding = deflateObject(prompt.getEmbedding().getVector());

                // Insert embedding
                var insertEmbeddingStatement = database.getConnection().prepareStatement(
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

    // Add Rating
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
                var insertRatingStatement = database.getConnection().prepareStatement(
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
                var insertFlagStatement = database.getConnection().prepareStatement(
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
                var insertScoreStatement = database.getConnection().prepareStatement(
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