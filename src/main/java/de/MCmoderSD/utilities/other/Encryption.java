package de.MCmoderSD.utilities.other;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;

public class Encryption {

    // Constants
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String ALGORITHM = "SHA-256";

    // Attributes
    private final SecretKey secretKey;
    private final HashMap<String, String> cache;

    // Constructor
    public Encryption(String token) {

        // Generate secret key using SHA-256 hash of the bot token
        try {
            MessageDigest sha = MessageDigest.getInstance(ALGORITHM);
            byte[] key = sha.digest(token.getBytes(CHARSET));
            secretKey = new SecretKeySpec(key, "AES"); // Key for AES encryption
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // Initialize the cache
        cache = new HashMap<>();
    }

    // Encrypt token
    public String encrypt(String token) {

        // Check cache for encrypted token
        for (HashMap.Entry<String, String> entry : cache.entrySet()) if (entry.getValue().equals(token)) return entry.getKey();
        try {

            // Create a new Cipher instance and initialize it to ENCRYPT mode
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // Perform encryption
            byte[] encryptedToken = cipher.doFinal(token.getBytes(CHARSET));
            String encodedToken = Base64.getEncoder().encodeToString(encryptedToken);

            // Store the encrypted token in the cache
            cache.put(encodedToken, token);
            return encodedToken; // Return the encrypted token
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    // Decrypt token
    public String decrypt(String encryptedToken) {

        // Check cache for decrypted token
        if (cache.containsKey(encryptedToken)) return cache.get(encryptedToken);
        try {
            // Create a new Cipher instance and initialize it to DECRYPT mode
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Perform decryption
            byte[] decodedToken = Base64.getDecoder().decode(encryptedToken);
            byte[] originalToken = cipher.doFinal(decodedToken);
            String token = new String(originalToken, CHARSET);

            // Store the decrypted token in the cache
            cache.put(encryptedToken, token);
            return token; // Return the decrypted token
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    // Change Encryption Token
    public static void main(String[] args) throws SQLException {

        // Old and new token
        String oldToken = "oauth:"; // Old token
        String newToken = "oauth:"; // New token

        // Create encryptors
        Encryption oldEncryptor = new Encryption(oldToken); // Old token
        Encryption newEncryptor = new Encryption(newToken); // New token

        // Database connection
        String host = "localhost";
        int port = 3306; // MySQL Default Port
        String database = "your_database";
        String username = "your_username";
        String password = "your_password";

        // Connect to database

        Connection connection = DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/%s", host, port, database), username, password); // connect

        // Replace old token with new token
        ResultSet resultSet = connection.prepareStatement("SELECT * FROM AuthTokens").executeQuery();
        while (resultSet.next()) {
            try {

                // Get the token
                String accessToken = resultSet.getString("accessToken");
                String refreshToken = resultSet.getString("refreshToken");

                // Decrypt the token
                String oldAccessToken = oldEncryptor.decrypt(accessToken);
                String oldRefreshToken = oldEncryptor.decrypt(refreshToken);

                // Encrypt the token
                String newAccessToken = newEncryptor.encrypt(oldAccessToken);
                String newRefreshToken = newEncryptor.encrypt(oldRefreshToken);

                //noinspection SqlSourceToSinkFlow
                connection.prepareStatement("UPDATE AuthTokens SET accessToken = '" + newAccessToken + "', refreshToken = '" + newRefreshToken + "' WHERE accessToken = '" + accessToken + "' AND refreshToken = '" + refreshToken + "'").execute();

                // Log the change
                System.out.println("Updated token from Client ID: " + resultSet.getInt("id"));

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}