import com.openai.models.moderations.ModerationModel;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.openai.core.OpenAI;
import de.MCmoderSD.openai.objects.ModerationPrompt;
import de.MCmoderSD.openai.objects.Rating;
import de.MCmoderSD.sql.Driver;

import java.io.IOException;
import java.sql.*;

public class GenerateRatings {

    // Change Encryption Token
    public static void main(String[] args) throws SQLException {

        // Database connection
        String host = "localhost";
        int port = 3306; // SQL Default Port
        String database = "your_database";
        String username = "your_username";
        String password = "your_password";

        // OpenAI API Key
        String openAIKey = "your_openai_key";
        OpenAI openAI = new OpenAI(openAIKey);

        try (Connection connection = DriverManager.getConnection(Driver.DatabaseType.MARIADB.getUrl(host, port, database), username, password)) {

            // SQL query to select all rows from the table
            String sql = "SELECT id, event FROM EventLog WHERE ratingId IS NULL";
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            var fetchSize = resultSet.getFetchSize();
            var done = 0;

            // Iterate through the result set
            while (resultSet.next()) {
                var time = System.nanoTime();

                // Variables
                var id = resultSet.getInt("id");
                TwitchMessageEvent event = TwitchMessageEvent.fromBytes(resultSet.getBytes("event"));

                // Prompt
                ModerationPrompt prompt = openAI.moderate(ModerationModel.OMNI_MODERATION_LATEST, event.getMessage());
                String ratingId = prompt.getId().startsWith("modr-") ? prompt.getId().substring(5) : prompt.getId();
                Rating rating = prompt.getRatings().getFirst();
                byte[] ratingBytes = rating.getBytes();

                // Rating Flags
                Rating.Flag harassment = rating.getHarassment();
                Rating.Flag harassmentThreatening = rating.getHarassmentThreatening();
                Rating.Flag hate = rating.getHate();
                Rating.Flag hateThreatening = rating.getHateThreatening();
                Rating.Flag illicit = rating.getIllicit();
                Rating.Flag illicitViolent = rating.getIllicitViolent();
                Rating.Flag selfHarm = rating.getSelfHarm();
                Rating.Flag selfHarmInstructions = rating.getSelfHarmInstructions();
                Rating.Flag selfHarmIntent = rating.getSelfHarmIntent();
                Rating.Flag sexual = rating.getSexual();
                Rating.Flag sexualMinors = rating.getSexualMinors();
                Rating.Flag violence = rating.getViolence();
                Rating.Flag violenceGraphic = rating.getViolenceGraphic();

                // Prepared Statements
                PreparedStatement ratingStatement;
                PreparedStatement flagsStatement;
                PreparedStatement scoresStatement;

                // Insert Rating or Update
                ratingStatement = connection.prepareStatement(
                        "INSERT INTO Rating (id, flagged, rating) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE flagged = ?, rating = ?"
                );

                // Insert Rating Flags or Update
                flagsStatement = connection.prepareStatement(
                        """
                                INSERT INTO RatingFlags (id, harassment, harassmentThreatening, hate, hateThreatening, illicit, illicitViolent, selfHarm, selfHarmInstructions, selfHarmIntent, sexual, sexualMinors, violence, violenceGraphic)
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                                ON DUPLICATE KEY UPDATE harassment = ?, harassmentThreatening = ?, hate = ?, hateThreatening = ?, illicit = ?, illicitViolent = ?, selfHarm = ?, selfHarmInstructions = ?, selfHarmIntent = ?, sexual = ?, sexualMinors = ?, violence = ?, violenceGraphic = ?
                                """
                );

                // Insert Rating Flags or Update
                scoresStatement = connection.prepareStatement(
                        """
                                INSERT INTO RatingScores (id, harassment, harassmentThreatening, hate, hateThreatening, illicit, illicitViolent, selfHarm, selfHarmInstructions, selfHarmIntent, sexual, sexualMinors, violence, violenceGraphic)
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                                ON DUPLICATE KEY UPDATE harassment = ?, harassmentThreatening = ?, hate = ?, hateThreatening = ?, illicit = ?, illicitViolent = ?, selfHarm = ?, selfHarmInstructions = ?, selfHarmIntent = ?, sexual = ?, sexualMinors = ?, violence = ?, violenceGraphic = ?
                                """
                );

                // Rating
                ratingStatement.setString(1, ratingId);                             // set id
                ratingStatement.setBoolean(2, rating.isFlagged());                  // set flagged
                ratingStatement.setBytes(3, ratingBytes);                           // set rating
                ratingStatement.setBoolean(4, rating.isFlagged());                  // set flagged
                ratingStatement.setBytes(5, ratingBytes);                           // set rating

                // Flags
                flagsStatement.setString(1, ratingId);                              // set id
                flagsStatement.setBoolean(2, harassment.flagged());                 // set harassment
                flagsStatement.setBoolean(3, harassmentThreatening.flagged());      // set harassmentThreatening
                flagsStatement.setBoolean(4, hate.flagged());                       // set hate
                flagsStatement.setBoolean(5, hateThreatening.flagged());            // set hateThreatening
                flagsStatement.setBoolean(6, illicit.flagged());                    // set illicit
                flagsStatement.setBoolean(7, illicitViolent.flagged());             // set illicitViolent
                flagsStatement.setBoolean(8, selfHarm.flagged());                   // set selfHarm
                flagsStatement.setBoolean(9, selfHarmInstructions.flagged());       // set selfHarmInstructions
                flagsStatement.setBoolean(10, selfHarmIntent.flagged());            // set selfHarmIntent
                flagsStatement.setBoolean(11, sexual.flagged());                    // set sexual
                flagsStatement.setBoolean(12, sexualMinors.flagged());              // set sexualMinors
                flagsStatement.setBoolean(13, violence.flagged());                  // set violence
                flagsStatement.setBoolean(14, violenceGraphic.flagged());           // set violenceGraphic
                flagsStatement.setBoolean(15, harassment.flagged());                // set harassment
                flagsStatement.setBoolean(16, harassmentThreatening.flagged());     // set harassmentThreatening
                flagsStatement.setBoolean(17, hate.flagged());                      // set hate
                flagsStatement.setBoolean(18, hateThreatening.flagged());           // set hateThreatening
                flagsStatement.setBoolean(19, illicit.flagged());                   // set illicit
                flagsStatement.setBoolean(20, illicitViolent.flagged());            // set illicitViolent
                flagsStatement.setBoolean(21, selfHarm.flagged());                  // set selfHarm
                flagsStatement.setBoolean(22, selfHarmInstructions.flagged());      // set selfHarmInstructions
                flagsStatement.setBoolean(23, selfHarmIntent.flagged());            // set selfHarmIntent
                flagsStatement.setBoolean(24, sexual.flagged());                    // set sexual
                flagsStatement.setBoolean(25, sexualMinors.flagged());              // set sexualMinors
                flagsStatement.setBoolean(26, violence.flagged());                  // set violence
                flagsStatement.setBoolean(27, violenceGraphic.flagged());           // set violenceGraphic

                // Scores
                scoresStatement.setString(1, ratingId);                             // set id
                scoresStatement.setDouble(2, harassment.score());                   // set harassment
                scoresStatement.setDouble(3, harassmentThreatening.score());        // set harassmentThreatening
                scoresStatement.setDouble(4, hate.score());                         // set hate
                scoresStatement.setDouble(5, hateThreatening.score());              // set hateThreatening
                scoresStatement.setDouble(6, illicit.score());                      // set illicit
                scoresStatement.setDouble(7, illicitViolent.score());               // set illicitViolent
                scoresStatement.setDouble(8, selfHarm.score());                     // set selfHarm
                scoresStatement.setDouble(9, selfHarmInstructions.score());         // set selfHarmInstructions
                scoresStatement.setDouble(10, selfHarmIntent.score());              // set selfHarmIntent
                scoresStatement.setDouble(11, sexual.score());                      // set sexual
                scoresStatement.setDouble(12, sexualMinors.score());                // set sexualMinors
                scoresStatement.setDouble(13, violence.score());                    // set violence
                scoresStatement.setDouble(14, violenceGraphic.score());             // set violenceGraphic
                scoresStatement.setDouble(15, harassment.score());                  // set harassment
                scoresStatement.setDouble(16, harassmentThreatening.score());       // set harassmentThreatening
                scoresStatement.setDouble(17, hate.score());                        // set hate
                scoresStatement.setDouble(18, hateThreatening.score());             // set hateThreatening
                scoresStatement.setDouble(19, illicit.score());                     // set illicit
                scoresStatement.setDouble(20, illicitViolent.score());              // set illicitViolent
                scoresStatement.setDouble(21, selfHarm.score());                    // set selfHarm
                scoresStatement.setDouble(22, selfHarmInstructions.score());        // set selfHarmInstructions
                scoresStatement.setDouble(23, selfHarmIntent.score());              // set selfHarmIntent
                scoresStatement.setDouble(24, sexual.score());                      // set sexual
                scoresStatement.setDouble(25, sexualMinors.score());                // set sexualMinors
                scoresStatement.setDouble(26, violence.score());                    // set violence
                scoresStatement.setDouble(27, violenceGraphic.score());             // set violenceGraphic

                // Link Event to Rating
                PreparedStatement eventStatement = connection.prepareStatement(
                        "UPDATE EventLog SET ratingId = ? WHERE id = ?"
                );

                // Set values
                eventStatement.setString(1, ratingId);  // set ratingId
                eventStatement.setInt(2, id);           // set id

                // Execute statements
                ratingStatement.executeUpdate(); // execute
                flagsStatement.executeUpdate();  // execute
                scoresStatement.executeUpdate(); // execute
                eventStatement.executeUpdate(); // execute

                // Close resources
                ratingStatement.close();
                flagsStatement.close();
                scoresStatement.close();
                eventStatement.close();

                // Print time taken
                done++;
                var delta = System.nanoTime() - time;
                System.out.println("Done: " + done + "/" + fetchSize + " | Time: " + delta / 1_000_000 + "ms | ETA:" + delta / 1_000_000 * (fetchSize - done) / 1_000_000 + "s");
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}