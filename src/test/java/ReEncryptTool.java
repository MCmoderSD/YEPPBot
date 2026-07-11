import de.MCmoderSD.encryption.core.Encryption;
import de.MCmoderSD.json.JsonUtility;
import de.MCmoderSD.sql.Driver;

import java.sql.SQLException;

import static de.MCmoderSD.encryption.enums.Hash.*;
import static de.MCmoderSD.encryption.enums.Transformer.*;
import static de.MCmoderSD.sql.Driver.DatabaseType.*;
import static java.lang.IO.println;

void main() {

    // Secrets
    var oldSecret = "old_secret_key_here"; // Old secret key
    var newSecret = "new_secret_key_here"; // New secret key

    // Create encryptors
    var oldEncryptor = new Encryption(oldSecret, SHA3_256, AES_ECB_PKCS5); // Old encryptor
    var newEncryptor = new Encryption(newSecret, SHA3_256, AES_ECB_PKCS5); // New encryptor

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

    // Get refresh tokens from database
    var refreshTokens = sql.getRefreshTokens();

    // Loop through tokens
    for (var entry : refreshTokens.entrySet()) {

        // Get ID
        var id = entry.getKey();

        // Re-encrypt token
        var decryptedToken = oldEncryptor.decrypt(entry.getValue()); // Decrypt with old key
        var encryptedToken = newEncryptor.encrypt(decryptedToken);   // Encrypt with new key

        // Update token in database
        sql.updateRefreshToken(id, encryptedToken);

        // Print result
        println("Encrypted token for ID " + id + " has been updated.");
    }
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

    // Retrieve all refresh Tokens
    public HashMap<Integer, String> getRefreshTokens() {
        try {

            // SQL statement to select all tokens
            var preparedStatement = connection.prepareStatement(
                    "SELECT * FROM RefreshToken"
            );

            // Execute the query
            var resultSet = preparedStatement.executeQuery();

            // Process the result set
            var refreshTokens = new HashMap<Integer, String>();
            while (resultSet.next()) refreshTokens.put(resultSet.getInt("id"), resultSet.getString("token"));

            // Close the result set and statement
            resultSet.close();
            preparedStatement.close();

            // Return the refresh tokens
            return refreshTokens;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve refresh tokens: " + e.getMessage(), e);
        }
    }

    // Update auth token method
    public void updateRefreshToken(int id, String token) {
        try {

            // Prepare statement
            var preparedStatement = connection.prepareStatement(
                    "UPDATE RefreshToken SET token = ? WHERE id = ?"
            );

            // Set parameters
            preparedStatement.setString(1, token);  // Set the new token
            preparedStatement.setInt(2, id);        // Set ID

            // Execute update
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update refresh token for ID: " + id + ": " + e.getMessage(), e);
        }
    }
}