package de.MCmoderSD.utilities.other;

import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.enums.Argument;
import de.MCmoderSD.enums.Scope;
import de.MCmoderSD.main.Main;
import de.MCmoderSD.objects.Birthdate;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.objects.TwitchUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class Util {

    // Instances
    public final static Random RANDOM = new Random();

    public static ArrayList<String> readAllLines(String path) {
        return readAllLines(path, false);
    }

    public static ArrayList<String> readAllLines(String path, boolean isAbsolute) {
        try {
            // Variables
            if (isAbsolute) return new ArrayList<>(Files.readAllLines(Path.of(path)));

            // Get Input Stream
            InputStream inputStream = Util.class.getResourceAsStream(path);

            // Check
            if (inputStream == null) throw new RuntimeException("File not found: " + path);

            // Initialize Util
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            // Variables
            ArrayList<String> lines = new ArrayList<>();
            String line;

            // Read
            while ((line = reader.readLine()) != null) lines.add(line);

            // Close
            return lines;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static LinkedHashMap<Integer, Birthdate> removeNonFollower(TwitchMessageEvent event, LinkedHashMap<Integer, Birthdate> birthdays, HelixHandler helixHandler) {

        // Return if dev mode
        if (Main.terminal.hasArg(Argument.DEV)) return birthdays;

        // Check Scope
        if (!helixHandler.checkScope(event.getChannelId(), Scope.MODERATOR_READ_FOLLOWERS)) return null;

        // Remove all non followers
        HashSet<TwitchUser> followers = new HashSet<>(helixHandler.getFollowers(event.getChannelId(), null));
        followers.add(new TwitchUser(event));                                       // Add User
        followers.add(new TwitchUser(event.getChannelId(), event.getChannel()));    // Add Broadcaster
        birthdays.entrySet().removeIf(entry -> !TwitchUser.containsTwitchUser(followers, entry.getKey()));

        // Return
        return birthdays;
    }

    public static HashSet<Integer> removeNonFollower(TwitchMessageEvent event, HashSet<Integer> ids, HelixHandler helixHandler) {

        // Return if dev mode
        if (Main.terminal.hasArg(Argument.DEV)) return ids;

        // Check Scope
        if (!helixHandler.checkScope(event.getChannelId(), Scope.MODERATOR_READ_FOLLOWERS)) return null;

        // Remove all non followers>
        HashSet<TwitchUser> followers = helixHandler.getFollowers(event.getChannelId(), null);
        followers.add(new TwitchUser(event));                                       // Add User
        followers.add(new TwitchUser(event.getChannelId(), event.getChannel()));    // Add Broadcaster
        ids.removeIf(id -> !TwitchUser.containsTwitchUser(followers, id));

        // Return
        return ids;
    }

    public static LinkedHashMap<Integer, Birthdate> sortBirthdays(LinkedHashMap<Integer, Birthdate> birthdays) {
        if (birthdays == null) return null;
        return birthdays.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.comparing(Birthdate::getDaysUntilBirthday))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}