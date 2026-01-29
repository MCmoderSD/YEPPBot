import de.MCmoderSD.encryption.core.Encryption;
import de.MCmoderSD.sql.Driver;
import de.MCmoderSD.tools.GZIP;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static de.MCmoderSD.encryption.enums.Hash.SHA3_256;
import static de.MCmoderSD.encryption.enums.Transformer.AES_ECB_PKCS5;
import static de.MCmoderSD.sql.Driver.DatabaseType.MARIADB;

void main() {

    // Secrets
    String oldSecret = "old_secret_key_here"; // Old secret key
    String newSecret = "new_secret_key_here"; // New secret key

    // Create encryptors
    Encryption oldEncryptor = new Encryption(oldSecret, SHA3_256, AES_ECB_PKCS5); // Old encryptor
    Encryption newEncryptor = new Encryption(newSecret, SHA3_256, AES_ECB_PKCS5); // New encryptor

    // Database connection
    SQL.Builder builder = SQL.Builder
            .withType(MARIADB)          // Database type
            .withHost("localhost")      // Database host
            .withPort(3306)             // Database port
            .withDatabase("database")   // Database name
            .withUsername("username")   // Database username
            .withPassword("password");  // Database password

    // Initialize SQL
    SQL sql = new SQL(builder);

    // Get auth tokens
    HashMap<Integer, byte[]> authTokens = sql.getAuthTokens();

    // Loop through tokens
    for (var entry : authTokens.entrySet()) {
        try {

            // Get DB values
            var id = entry.getKey();
            var compressedToken = entry.getValue();

            // Decompress and decrypt token
            byte[] encryptedToken = GZIP.inflate(compressedToken);
            byte[] decryptedToken = oldEncryptor.decrypt(encryptedToken);

            // Re-encrypt and compress token
            byte[] reEncryptedToken = newEncryptor.encrypt(decryptedToken);
            byte[] recompressedToken = GZIP.deflate(reEncryptedToken);

            // Update token in database
            sql.updateAuthToken(id, recompressedToken);

            // Print result
            IO.println("Encrypted token: " + new String(recompressedToken));

        } catch (Exception e) {
            throw new RuntimeException("Failed to process auth token for ID: " + entry.getKey(), e);
        }
    }
}

private static class SQL extends Driver {

    public SQL(SQL.Builder builder) {

        // Call super
        super(builder);

        // Connect
        connect();
    }

    public HashMap<Integer, byte[]> getAuthTokens() {

        // Create map
        HashMap<Integer, byte[]> authTokens = new HashMap<>();

        try {

            // Prepare statement
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM AuthToken"
            );

            // Execute query
            ResultSet resultSet = preparedStatement.executeQuery();

            // Loop through results
            while (resultSet.next()) authTokens.put(resultSet.getInt("id"), resultSet.getBytes("accessToken"));

            // Return map
            return authTokens;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get auth tokens", e);
        }
    }

    public void updateAuthToken(int id, byte[] token) {
        try {

            // Prepare statement
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "UPDATE AuthToken SET token = ? WHERE id = ?"
            );

            // Set parameters
            preparedStatement.setBytes(1, token);
            preparedStatement.setInt(2, id);

            // Execute update
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update auth token for ID: " + id, e);
        }
    }
}