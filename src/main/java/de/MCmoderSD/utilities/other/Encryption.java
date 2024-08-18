package de.MCmoderSD.utilities.other;

import com.fasterxml.jackson.databind.JsonNode;

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
    public Encryption(JsonNode botConfig) {

        // Get the bot token from the configuration
        String botToken = botConfig.get("botToken").asText();

        // Generate secret key using SHA-256 hash of the bot token
        try {
            MessageDigest sha = MessageDigest.getInstance(ALGORITHM);
            byte[] key = sha.digest(botToken.getBytes(CHARSET));
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
}