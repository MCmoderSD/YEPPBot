package de.MCmoderSD.utilities;

import java.util.UUID;

public class FormatUUID {

    public static byte[] asBytes(UUID uuid) {
        if (uuid == null) throw new IllegalArgumentException("UUID cannot be null");
        var mostSignificantBits = uuid.getMostSignificantBits();
        var leastSignificantBits = uuid.getLeastSignificantBits();
        return new byte[] {
                (byte) (mostSignificantBits >>> 56),
                (byte) (mostSignificantBits >>> 48),
                (byte) (mostSignificantBits >>> 40),
                (byte) (mostSignificantBits >>> 32),
                (byte) (mostSignificantBits >>> 24),
                (byte) (mostSignificantBits >>> 16),
                (byte) (mostSignificantBits >>> 8),
                (byte) (mostSignificantBits),
                (byte) (leastSignificantBits >>> 56),
                (byte) (leastSignificantBits >>> 48),
                (byte) (leastSignificantBits >>> 40),
                (byte) (leastSignificantBits >>> 32),
                (byte) (leastSignificantBits >>> 24),
                (byte) (leastSignificantBits >>> 16),
                (byte) (leastSignificantBits >>> 8),
                (byte) (leastSignificantBits)
        };
    }

    public static boolean validUUID(String uuid) {
        if (uuid == null || uuid.isBlank()) return false;
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}