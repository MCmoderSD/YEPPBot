package de.MCmoderSD.utilities;

import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHash64;
import net.jpountz.xxhash.XXHashFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@SuppressWarnings("unused")
public class Hasher {

    // SHA-256 Algorithm
    private static final String SHA256 = "SHA-256";

    // XXHash Instances
    private static final XXHashFactory factory = XXHashFactory.fastestInstance();
    private static final XXHash32 xxHash32 = factory.hash32();
    private static final XXHash64 xxHash64 = factory.hash64();

    // XXHash Methods
    public static byte[] xxHash32(byte[] data) {
        var hashInt = xxHash32.hash(data, 0, data.length, 0);
        return new byte[] {
                (byte) ((hashInt >> 24) & 0xFF),
                (byte) ((hashInt >> 16) & 0xFF),
                (byte) ((hashInt >> 8) & 0xFF),
                (byte) (hashInt & 0xFF)
        };
    }

    // XXHash64 Methods
    public static byte[] xxHash64(byte[] data) {
        var hashLong = xxHash64.hash(data, 0, data.length, 0);
        return new byte[] {
                (byte) ((hashLong >> 56) & 0xFF),
                (byte) ((hashLong >> 48) & 0xFF),
                (byte) ((hashLong >> 40) & 0xFF),
                (byte) ((hashLong >> 32) & 0xFF),
                (byte) ((hashLong >> 24) & 0xFF),
                (byte) ((hashLong >> 16) & 0xFF),
                (byte) ((hashLong >> 8) & 0xFF),
                (byte) (hashLong & 0xFF)
        };
    }

    public static byte[] xxHash32(String data) {
        return xxHash32(data.getBytes());
    }

    public static byte[] xxHash64(String data) {
        return xxHash64(data.getBytes());
    }

    // SHA-256 Methods
    public static byte[] sha256(byte[] data) {

        // Check Parameters
        if (data == null) throw new IllegalArgumentException("Data cannot be null");

        // Hash Data
        try {
            return MessageDigest.getInstance(SHA256).digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    public static byte[] sha256(String data) {
        return sha256(data.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha256Hex(String data) {
        return HexFormat.of().formatHex(sha256(data));
    }
}