import de.MCmoderSD.openai.core.OpenAI;
import de.MCmoderSD.openai.models.ModerationModel;
import de.MCmoderSD.openai.objects.ModerationPrompt;
import de.MCmoderSD.openai.objects.Rating;
import de.MCmoderSD.openai.objects.Rating.Flag;
import de.MCmoderSD.sql.Driver;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;

import static de.MCmoderSD.openai.models.ModerationModel.OMNI_MODERATION_LATEST;
import static de.MCmoderSD.openai.models.ModerationModel.TEXT_MODERATION_LATEST;

@SuppressWarnings("BusyWait")
public class GenerateRatings {

    // Main
    public static void main(String[] args) {

        // Models
        ModerationModel[] models = {
                OMNI_MODERATION_LATEST,
                OMNI_MODERATION_LATEST,
                TEXT_MODERATION_LATEST
        };

        // Variables
        var i = 0;
        ModerationModel model = models[i];

        // Loop until success
        while (!loop(model)) {
            try {
                System.err.println("Retrying...");

                // Increment model index
                i++;
                if (i >= models.length) i = 0;
                model = models[i];

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // Loop
    private static boolean loop(ModerationModel model) {
        try {
            generate(model);
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        } finally {
            System.out.println("Finished!");
        }
        return true;
    }

    // Generate Ratings
    private static void generate(ModerationModel model) throws SQLException, IOException {

        // Variables
        String host = "localhost";
        var port = 3306; // SQL Default Port
        String database = "your_database";
        String username = "your_username";
        String password = "your_password";

        // OpenAI API Key
        String openAIKey = "your_openai_key";
        OpenAI openAI = new OpenAI(openAIKey);

        // Connect to the database
        System.out.println("Connecting to database...");
        Connection connection = DriverManager.getConnection(Driver.DatabaseType.MARIADB.getUrl(host, port, database), username, password);

        System.out.println("Connected to database!\n");
        System.out.println("Counting rows...");

        // SQL query to count rows with null ratingId
        ResultSet countResultSet = connection.createStatement().executeQuery("SELECT COUNT(*) FROM EventLog WHERE ratingId IS NULL");

        // Get the count
        var fetchSize = 0;
        if (countResultSet.next()) fetchSize = countResultSet.getInt(1);
        System.out.println("Rows: " + fetchSize + "\n");

        // Fetch rows with null ratingId
        System.out.println("Fetching rows...");
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT m.id, m.message FROM MessageLog m JOIN EventLog e ON m.id = e.id WHERE e.ratingId IS NULL;");
        System.out.println("Fetched rows!\n");
        var done = 0;

        System.out.println("Processing rows...");

        // Iterate through the result set
        while (resultSet.next()) {
            var time = System.nanoTime(); // Start time

            // Prompt
            ModerationPrompt prompt = openAI.moderate(model, resultSet.getString("message"));
            String ratingId = prompt.getId().startsWith("modr-") ? prompt.getId().substring(5) : prompt.getId();
            Rating rating = prompt.getRating();
            var ratingBytes = rating.getBytes();

            // Rating Flags
            Flag harassment = rating.getHarassment();
            Flag harassmentThreatening = rating.getHarassmentThreatening();
            Flag hate = rating.getHate();
            Flag hateThreatening = rating.getHateThreatening();
            Flag illicit = rating.getIllicit();
            Flag illicitViolent = rating.getIllicitViolent();
            Flag selfHarm = rating.getSelfHarm();
            Flag selfHarmInstructions = rating.getSelfHarmInstructions();
            Flag selfHarmIntent = rating.getSelfHarmIntent();
            Flag sexual = rating.getSexual();
            Flag sexualMinors = rating.getSexualMinors();
            Flag violence = rating.getViolence();
            Flag violenceGraphic = rating.getViolenceGraphic();

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
            eventStatement.setString(1, ratingId);                              // set ratingId
            eventStatement.setString(2, resultSet.getString("id")); // set id

            // Execute statements
            ratingStatement.executeUpdate();
            flagsStatement.executeUpdate();
            scoresStatement.executeUpdate();
            eventStatement.executeUpdate();

            // Close resources
            ratingStatement.close();
            flagsStatement.close();
            scoresStatement.close();
            eventStatement.close();

            // Print time taken
            done++;
            var delta = System.nanoTime() - time;
            System.out.println("Done: " + done + " / " + fetchSize + " | Time: " + delta / 1000000L + "ms" + " | " + new BigDecimal(done * 100d / fetchSize).setScale(2, RoundingMode.HALF_UP) + "%");
        }
    }
}