import de.MCmoderSD.encryption.core.Encryption;
import de.MCmoderSD.json.JsonUtility;
import de.MCmoderSD.tools.GZIP;
import de.MCmoderSD.sql.Driver;
import tools.jackson.databind.JsonNode;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

import static de.MCmoderSD.encryption.enums.Hash.SHA3_256;
import static de.MCmoderSD.encryption.enums.Transformer.AES_ECB_PKCS5;

void main() throws IOException, URISyntaxException {

    // Secrets
    String oldSecret = "old_secret_key_here"; // Old secret key
    String newSecret = "new_secret_key_here"; // New secret key

    // Create encryptors
    Encryption oldEncryptor = new Encryption(oldSecret, SHA3_256, AES_ECB_PKCS5); // Old encryptor
    Encryption newEncryptor = new Encryption(newSecret, SHA3_256, AES_ECB_PKCS5); // New encryptor

    // Load Config
    JsonNode config = JsonUtility.getInstance().load("/database.json");

    // Initialize SQL
    SQL sql = new SQL(Driver.Builder
            .withType(Driver.DatabaseType.MARIADB)
            .withHost(config.get("host").asString())
            .withPort(config.get("port").asInt())
            .withDatabase(config.get("database").asString())
            .withUsername(config.get("username").asString())
            .withPassword(config.get("password").asString())
    );

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