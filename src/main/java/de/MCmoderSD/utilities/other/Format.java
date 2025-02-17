package de.MCmoderSD.utilities.other;

import com.fasterxml.jackson.databind.JsonNode;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.objects.TwitchMessageEvent;

import java.awt.Color;

import java.sql.Timestamp;

import java.text.Normalizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import java.text.SimpleDateFormat;

import static de.MCmoderSD.core.BotClient.prefixes;
import static de.MCmoderSD.utilities.other.Util.*;


public class Format {

    // Formatting
    public final static String BOLD = "\033[0;1m";
    public final static String UNBOLD = "\u001B[0m";
    public final static String BREAK = "\n";
    public final static String TAB = "\t";
    public final static String SPACE = " ";
    public final static String EMPTY = "";

    // Tags
    public final static String SYSTEM = "[SYS]";
    public final static String USER = "[USR]";
    public final static String COMMAND = "[CMD]";
    public final static String RESPONSE = "[RSP]";
    public final static String EVENT = "[EVT]";
    public final static String FOLLOW = "[FLW]";
    public final static String SUBSCRIBE = "[SUB]";
    public final static String GIFT = "[GFT]";
    public final static String RAID = "[RAD]";

    // Regex
    public final static String VALID_CHARS = "[a-zA-Z0-9äöüÄÖÜß.,;:!?(){}\\\\<>@#%&*/=+~^_|\"'-]";
    public final static String INVALID_CHARS = "[^a-zA-Z0-9äöüÄÖÜß.,;:!?(){}\\\\<>@#%&*/=+~^_|\"'-]";
    public final static String INVALID_UNICODE = "\uDB40\uDC00";
    public final static String EMOJIS = "[\\p{So}\\p{Cn}]";

    //Patterns
    public final static String TIMESTAMP_FORMAT = "dd-MM-yyyy|HH:mm:ss";

    // Colors
    public final static Color DARK = new Color(0x0e0e10);
    public final static Color LIGHT = new Color(0x18181b);
    public final static Color PURPLE = new Color(0x771fe2);
    public final static Color WHITE = new Color(0xffffff);

    // Get Formatted Timestamp
    public static String getFormattedTimestamp() {
        return "[" + new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Timestamp(System.currentTimeMillis())) + "]";
    }

    // Tag the channel
    public static String tagChannel(TwitchMessageEvent event) {
        return "@" + event.getChannel();
    }

    // Tag the user
    public static String tagUser(TwitchMessageEvent event) {
        return "@" + event.getUser();
    }

    // Remove Tag
    public static String removeTag(String input) {
        while (input.startsWith("@")) input = input.substring(1);
        return input;
    }

    // Remove Prefix
    public static String removePrefix(String input) {
        if (input.substring(0, 1).matches(VALID_CHARS) && Arrays.stream(prefixes).noneMatch(input::startsWith)) return input;   // Check if valid
        while (input.substring(0, 1).matches(INVALID_CHARS)) input = input.substring(1);                                // Remove invalid characters
        for (String prefix : prefixes) while (input.startsWith(prefix)) input = input.substring(prefix.length());               // Remove prefix
        return removePrefix(input);                                                                                             // Recursion until valid
    }

    // Trim Message
    public static String trimMessage(String message) {

        // Replace Invalid Characters
        message = message.replaceAll(INVALID_UNICODE, EMPTY);

        // Trim Message
        message = message.trim();
        while (startsWith(message, SPACE, TAB, BREAK)) message = message.substring(1);
        while (endsWith(message, SPACE, TAB, BREAK)) message = message.substring(0, message.length() - 1);

        // Return
        return message;
    }

    // Clean Args
    public static ArrayList<String> cleanArgs(ArrayList<String> args) {

        // Clean Args
        ArrayList<String> cleaned = new ArrayList<>();

        // Trim Args and Remove Empty
        for (String arg : args) {
            String clean = trimMessage(arg).replaceAll(SPACE, EMPTY).replaceAll(BREAK, EMPTY).replaceAll(TAB, EMPTY);
            if (!(clean.isBlank())) cleaned.add(clean);
        }

        // Return
        return cleaned;
    }

    // Trim Args
    public static String concatArgs(ArrayList<String> args) {
        return trimMessage(String.join(SPACE, args)).trim();
    }

    // Replace Emojis
    public static String replaceEmojis(String input, String replacement) {
        return input.replaceAll(EMOJIS, replacement);
    }

    // Remove repetitions
    public static String removeRepetitions(String input, String repetition) {
        return input.replaceAll("\\b(" + repetition + ")\\s+\\1\\b", "$1");
    }

    // Format OpenAI Response
    public static String formatOpenAIResponse(String response, String emote) {
        return removePrefix(removeRepetitions(replaceEmojis(response.replaceAll("(?i)" + emote + "[.,!?\\s]*", emote + SPACE), emote), emote));
    }

    // Format Scopes
    public static String formatScopes(JsonNode scopes) {
        StringBuilder scopeString = new StringBuilder();
        for (var i = 0; i < scopes.size(); i++) scopeString.append(scopes.get(i).asText()).append("+");
        return scopeString.substring(0, scopeString.length() - 1);
    }

    // Format Ids to String
    public static ArrayList<String> formatIdsToString(HashSet<Integer> ids) {

        // Check if empty
        if (ids.isEmpty()) return new ArrayList<>();

        // Variables
        ArrayList<String> strings = new ArrayList<>();

        // Loop through ids
        for (Integer id : ids) {
            if (id == null || id < 1) continue;                         // Check if valid
            String string = id.toString();                              // Convert to string
            if (string.isBlank() || strings.contains(string)) continue; // Check if empty or duplicate
            strings.add(string);                                        // Add to list
        }

        // Return
        return strings;
    }

    // Convert to ASCII
    public static String convertToAscii(String input) {

        // Replace German Umlauts
        input = input.replaceAll("Ä", "Ae").replaceAll("ä", "ae");
        input = input.replaceAll("Ö", "Oe").replaceAll("ö", "oe");
        input = input.replaceAll("Ü", "Ue").replaceAll("ü", "ue");
        input = input.replaceAll("ẞ", "Ss").replaceAll("ß", "ss");

        // Normalize the input string to decompose accented characters
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // Remove diacritical marks (accents)
        String ascii = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", EMPTY);

        // Remove any remaining non-ASCII characters
        ascii = ascii.replaceAll("[^\\p{ASCII}]", EMPTY);

        // Return the ASCII string
        return ascii;
    }

    // Format Command
    public static ArrayList<String> formatCommand(TwitchMessageEvent event) {

        // Variables
        String message = event.getMessage();

        // Find Prefix
        String prefix = BotClient.prefix;
        for (String p : prefixes) if (message.contains(p)) prefix = p;

        // Find Start
        if (message.indexOf(prefix) == 0) message = message.substring(1);       // Remove Prefix
        else message = message.substring(message.indexOf(SPACE + prefix) + 2);    // Remove Space and Prefix

        // Split Command
        String[] split = trimMessage(message).split(SPACE);
        return new ArrayList<>(Arrays.asList(split));
    }

    // Format Command
    public static String formatCommand(TwitchMessageEvent event, ArrayList<String> args, String response) {

        // Constants
        String RANDOM = "%random%";
        String CHANNEL = "%channel%";
        String AUTHOR = "%author%";
        String USER = "%user%";
        String TAGGED = "%tagged%";

        // Replace Variables
        if (response.contains(RANDOM)) response = response.replaceAll(RANDOM, Util.RANDOM.nextInt(100) + "%");
        if (response.contains(CHANNEL)) response = response.replaceAll(CHANNEL, tagChannel(event));

        // Replace Tags
        if (response.contains(AUTHOR) || response.contains(USER)) response = response.replaceAll(AUTHOR, tagUser(event)).replaceAll(USER, tagUser(event));

        // Replace Tagged
        if (response.contains(TAGGED)) {
            String tagged;
            if (!args.isEmpty()) tagged = args.getFirst().startsWith("@") ? args.getFirst() : "@" + args.getFirst();
            else tagged = tagUser(event);
            response = response.replaceAll(TAGGED, tagged);
        }

        // Return
        return response;
    }

    // Format Lurk Time
    public static String formatLurkTime(Timestamp startTime) {

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
        long time = System.currentTimeMillis() - startTime.getTime();

        // Years
        long years = time / 31536000000L;
        time %= 31536000000L;
        if (years > 1) response.append(years).append(PATTERN.formatted(YEARS));
        else if (years > 0) response.append(years).append(PATTERN.formatted(YEAR));

        // Months
        long months = time / 2592000000L;
        time %= 2592000000L;
        if (months > 1) response.append(months).append(PATTERN.formatted(MONTHS));
        else if (months > 0) response.append(months).append(PATTERN.formatted(MONTH));

        // Weeks
        long weeks = time / 604800000L;
        time %= 604800000L;
        if (weeks > 1) response.append(weeks).append(PATTERN.formatted(WEEKS));
        else if (weeks > 0) response.append(weeks).append(PATTERN.formatted(WEEK));

        // Days
        long days = time / 86400000L;
        time %= 86400000L;
        if (days > 1) response.append(days).append(PATTERN.formatted(DAYS));
        else if (days > 0) response.append(days).append(PATTERN.formatted(DAY));

        // Hours
        long hours = time / 3600000L;
        time %= 3600000L;
        if (hours > 1) response.append(hours).append(PATTERN.formatted(HOURS));
        else if (hours > 0) response.append(hours).append(PATTERN.formatted(HOUR));

        // Minutes
        long minutes = time / 60000L;
        time %= 60000L;
        if (minutes > 1) response.append(minutes).append(PATTERN.formatted(MINUTES));
        else if (minutes > 0) response.append(minutes).append(PATTERN.formatted(MINUTE));

        // Seconds
        long seconds = time / 1000L;
        if (seconds > 1) response.append(seconds).append(PATTERN.formatted(SECONDS));
        else if (seconds > 0) response.append(seconds).append(PATTERN.formatted(SECOND));

        // Return
        return response.substring(0, response.length() - 2);
    }
}