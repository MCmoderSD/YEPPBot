package de.MCmoderSD.handlers;

import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.database.Database;
import de.MCmoderSD.database.manager.LurkManager;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import static de.MCmoderSD.utilities.MessageHelper.tagUser;

public class LurkHandler {

    // Associations
    private final TwitchBot twitchBot;

    // Database
    private final Database database;
    private final LurkManager lurkManager;

    // Attributes
    private final HashSet<String> lurkCommands;
    private final HashSet<TwitchUser> traitorList;
    private final ConcurrentHashMap<TwitchUser, TwitchUser> lurkList;

    // Constructor
    public LurkHandler(TwitchBot twitchBot) {

        // Check Parameters
        if (twitchBot == null) throw new IllegalArgumentException("TwitchBot cannot be null");

        // Set Associations
        this.twitchBot = twitchBot;

        // Set Database
        database = twitchBot.getDatabase();
        lurkManager = database.getLurkManager();

        // Initialize Lurk Commands
        lurkCommands = new HashSet<>();
        var commands = new String[]{ "lurk", "lörk", "lürk", "lork", "afk" };
        for (var command : commands) for (var prefix : twitchBot.getPrefixes()) {
            lurkCommands.add(prefix + command);
            lurkCommands.add(" " + prefix + command);
        }

        // Initialize Attributes
        traitorList = lurkManager.getTraitors();
        lurkList = new ConcurrentHashMap<>(lurkManager.getLurks());
    }

    public void handleLurk(MessageEvent event) {
        new Thread(() -> {

            // Check Parameters
            if (event == null) throw new IllegalArgumentException("MessageEvent cannot be null");

            // Get user and channel
            var user = event.getUser();
            var channel = event.getChannel();

            // Check for lurk command
            String message = event.getMessage().toLowerCase();
            for (var command : lurkCommands) if (command.contains(message)) {
                lurkManager.removeLurk(user);
                traitorList.remove(user);
                lurkList.remove(user);
                return;
            }

            // Skip if user not in lurk list
            if (!lurkList.containsKey(user)) return;

            // Check if user is a traitor
            boolean isTraitor = !lurkList.get(user).equals(channel);

            // If user is a traitor but already marked as one, do nothing
            if (isTraitor && traitorList.contains(user)) return;

            if (isTraitor) {

                // Add to traitor list
                traitorList.add(user);
                lurkManager.addTraitor(user);

                // Get Message Event
                MessageEvent lurkEvent = lurkManager.getLurkEvent(user);

                // Show traitor message
                twitchBot.sendMessage(lurkEvent, "Lurk-Traitor", tagUser(user, "ist ein dreckiger Verräter, hab den Kek gerade im Chat von " + tagUser(channel, "gesehen! YEPP")));

            } else {

                // Get start time
                Timestamp startTime = lurkManager.getLurkTime(user);

                // Remove from lurk list
                lurkList.remove(user);
                traitorList.remove(user);
                lurkManager.removeLurk(user);

                // Show lurk message
                twitchBot.sendMessage(event, "Lurk-End", tagUser(user, "war " + formatLurkTime(startTime) + " im Lurk! YEPP"));
            }
        }, "Handle-Lurk-" + event.getId().toString()).start();
    }

    public void addLurk(MessageEvent event) {

        // Check Parameters
        if (event == null) throw new IllegalArgumentException("MessageEvent cannot be null");

        // Variables
        var user = event.getUser();

        // Reset traitor list
        traitorList.remove(user);

        // Remove from lurk list if already present
        if (lurkList.contains(user)) lurkManager.removeLurk(user);

        // Add to lurk list
        lurkManager.addLurk(event);             // Database entry
        lurkList.put(user, event.getChannel()); // Local entry
    }

    private static String formatLurkTime(Timestamp startTime) {

        // Constants
        String PATTERN = " %s, ";
        String YEARS = "Jahre";
        String YEAR = "Jahr";
        String MONTHS = "Monate";
        String MONTH = "Monat";
        String WEEKS = "Wochen";
        String WEEK = "Woche";
        String DAYS = "Tage";
        String DAY = "Tag";
        String HOURS = "Stunden";
        String HOUR = "Stunde";
        String MINUTES = "Minuten";
        String MINUTE = "Minute";
        String SECONDS = "Sekunden";
        String SECOND = "Sekunde";

        // Variables
        StringBuilder response = new StringBuilder();
        var time = System.currentTimeMillis() - startTime.getTime();

        // Years
        var years = time / 31536000000L;
        time %= 31536000000L;
        if (years > 1) response.append(years).append(PATTERN.formatted(YEARS));
        else if (years > 0) response.append(years).append(PATTERN.formatted(YEAR));

        // Months
        var months = time / 2592000000L;
        time %= 2592000000L;
        if (months > 1) response.append(months).append(PATTERN.formatted(MONTHS));
        else if (months > 0) response.append(months).append(PATTERN.formatted(MONTH));

        // Weeks
        var weeks = time / 604800000L;
        time %= 604800000L;
        if (weeks > 1) response.append(weeks).append(PATTERN.formatted(WEEKS));
        else if (weeks > 0) response.append(weeks).append(PATTERN.formatted(WEEK));

        // Days
        var days = time / 86400000L;
        time %= 86400000L;
        if (days > 1) response.append(days).append(PATTERN.formatted(DAYS));
        else if (days > 0) response.append(days).append(PATTERN.formatted(DAY));

        // Hours
        var hours = time / 3600000L;
        time %= 3600000L;
        if (hours > 1) response.append(hours).append(PATTERN.formatted(HOURS));
        else if (hours > 0) response.append(hours).append(PATTERN.formatted(HOUR));

        // Minutes
        var minutes = time / 60000L;
        time %= 60000L;
        if (minutes > 1) response.append(minutes).append(PATTERN.formatted(MINUTES));
        else if (minutes > 0) response.append(minutes).append(PATTERN.formatted(MINUTE));

        // Seconds
        var seconds = time / 1000L;
        if (seconds > 1) response.append(seconds).append(PATTERN.formatted(SECONDS));
        else if (seconds > 0) response.append(seconds).append(PATTERN.formatted(SECOND));

        // Return
        return response.substring(0, response.length() - 2);
    }
}