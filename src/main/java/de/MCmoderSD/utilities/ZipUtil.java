package de.MCmoderSD.utilities;

import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;

import java.io.IOException;

import static de.MCmoderSD.tools.GZIP.inflateObject;

public class ZipUtil {

    public static TwitchUser inflateTwitchUser(byte[] data) {
        try {
            return (TwitchUser) inflateObject(data);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to inflate TwitchUser object: " + e.getMessage(), e);
        }
    }

    public static MessageEvent inflateEvent(byte[] data) {
        try {
            return (MessageEvent) inflateObject(data);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to inflate TwitchUser object: " + e.getMessage(), e);
        }
    }
}
