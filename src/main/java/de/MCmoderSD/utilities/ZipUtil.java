package de.MCmoderSD.utilities;

import de.MCmoderSD.bdsm.data.MatchResult;
import de.MCmoderSD.bdsm.data.TestResult;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.FollowEvent;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.objects.RaidEvent;

import java.io.IOException;

import static de.MCmoderSD.tools.GZIP.inflateObject;

@SuppressWarnings("unused")
public class ZipUtil {

    public static TwitchUser inflateTwitchUser(byte[] data) {
        try {
            return (TwitchUser) inflateObject(data);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to inflate TwitchUser object: " + e.getMessage(), e);
        }
    }

    public static MessageEvent inflateMessageEvent(byte[] data) {
        try {
            return (MessageEvent) inflateObject(data);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to inflate MessageEvent object: " + e.getMessage(), e);
        }
    }

    public static RaidEvent inflateRaidEvent(byte[] data) {
        try {
            return (RaidEvent) inflateObject(data);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to inflate RaidEvent object: " + e.getMessage(), e);
        }
    }

    public static FollowEvent inflateFollowEvent(byte[] data) {
        try {
            return (FollowEvent) inflateObject(data);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to inflate FollowEvent object: " + e.getMessage(), e);
        }
    }

    public static TestResult inflateTestResult(byte[] data) {
        try {
            return (TestResult) inflateObject(data);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to inflate TestResult object: " + e.getMessage(), e);
        }
    }

    public static MatchResult inflateMatchResult(byte[] data) {
        try {
            return (MatchResult) inflateObject(data);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to inflate MatchResult object: " + e.getMessage(), e);
        }
    }
}