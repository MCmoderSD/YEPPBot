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

import static de.MCmoderSD.core.BotClient.prefixes;
import static de.MCmoderSD.utilities.other.Util.RANDOM;


public class Format {

    // Formatting
    public final static String BOLD = "\033[0;1m";
    public final static String UNBOLD = "\u001B[0m";
    public final static String BREAK = "\n";

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

    // Colors
    public final static Color DARK = new Color(0x0e0e10);
    public final static Color LIGHT = new Color(0x18181b);
    public final static Color PURPLE = new Color(0x771fe2);
    public final static Color WHITE = new Color(0xffffff);

    // Get Formatted Timestamp
    public static String getFormattedTimestamp() {
        return "[" + new java.text.SimpleDateFormat("dd-MM-yyyy|HH:mm:ss").format(new Timestamp(System.currentTimeMillis())) + "]";
    }

    // Format Unix Timestamp
    public static String formatUnixTimestamp(long unixTimestamp) {
        return new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(unixTimestamp * 1000));
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
        for (String prefix : prefixes) while (input.startsWith(prefix)) input = input.substring(prefix.length());
        return input;
    }

    // Trim Message
    public static String trimMessage(String message) {

        // Replace Invalid Characters
        message = message.replaceAll("\uDB40\uDC00", "");

        // Trim Message
        message = message.trim();
        while (message.startsWith(" ") || message.startsWith("\n")) message = message.substring(1);
        while (message.endsWith(" ") || message.endsWith("\n")) message = message.substring(0, message.length() - 1);

        // Return
        return message;
    }

    // Clean Args
    public static ArrayList<String> cleanArgs(ArrayList<String> args) {

        // Clean Args
        ArrayList<String> cleaned = new ArrayList<>();

        // Trim Args
        for (String arg : args) {
            String clean = trimMessage(arg).replaceAll(" ", "").replaceAll("\n", "");
            if (!(clean.isBlank())) cleaned.add(clean);
        }

        // Return
        return cleaned;
    }

    // Trim Args
    public static String processArgs(ArrayList<String> args) {
        return trimMessage(String.join(" ", args)).trim();
    }

    // Replace Emojis
    public static String replaceEmojis(String input, String replacement) {
        return input.replaceAll("[\\p{So}\\p{Cn}]", replacement);
    }

    // Remove repetitions
    public static String removeRepetitions(String input, String repetition) {
        return input.replaceAll("\\b(" + repetition + ")\\s+\\1\\b", "$1");
    }

    // Format OpenAI Response
    public static String formatOpenAIResponse(String response, String emote) {
        return removeRepetitions(replaceEmojis(response.replaceAll("(?i)" + emote + "[.,!?\\s]*", emote + " "), emote), emote);
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

        for (Integer id : ids) {
            if (id == null || id < 1) continue;
            String string = id.toString();
            if (string.isBlank() || strings.contains(string)) continue;
            strings.add(string);
        }

        // Return
        return strings;
    }

    // Convert to ASCII
    public static String convertToAscii(String input) {

        // Replace German Umlauts
        input = input.replaceAll("Ä", "Ae");
        input = input.replaceAll("ä", "ae");
        input = input.replaceAll("Ö", "Oe");
        input = input.replaceAll("ö", "oe");
        input = input.replaceAll("Ü", "Ue");
        input = input.replaceAll("ü", "ue");
        input = input.replaceAll("ß", "ss");

        // Normalize the input string to decompose accented characters
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // Remove diacritical marks (accents)
        String ascii = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Remove any remaining non-ASCII characters
        ascii = ascii.replaceAll("[^\\p{ASCII}]", "");

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
        if (message.indexOf(prefix) == 0) message = message.substring(1);
        else message = message.substring(message.indexOf(" " + prefix) + 2);

        // Split Command
        String[] split = trimMessage(message).split(" ");
        return new ArrayList<>(Arrays.asList(split));
    }

    // Format Command
    public static String formatCommand(TwitchMessageEvent event, ArrayList<String> args, String response) {

        // Replace Variables
        if (response.contains("%random%")) response = response.replaceAll("%random%", RANDOM.nextInt(100) + "%");
        if (response.contains("%channel%")) response = response.replaceAll("%channel%", tagChannel(event));

        // Replace Tags
        if (response.contains("%user%") || response.contains("%author%")) {
            response = response.replaceAll("%user%", tagUser(event));
            response = response.replaceAll("%author%", tagUser(event));
        }

        // Replace Tagged
        if (response.contains("%tagged%")) {
            String tagged;
            if (!args.isEmpty()) tagged = args.getFirst().startsWith("@") ? args.getFirst() : "@" + args.getFirst();
            else tagged = tagUser(event);
            response = response.replaceAll("%tagged%", tagged);
        }

        // Return
        return response;
    }

    // Format Lurk Time
    public static String formatLurkTime(Timestamp startTime) {

        // Variables
        StringBuilder response = new StringBuilder();
        long time = System.currentTimeMillis() - startTime.getTime();

        // Years
        long years = time / 31536000000L;
        time %= 31536000000L;
        if (years > 1) response.append(years).append(" Jahre, ");
        else if (years > 0) response.append(years).append(" Jahr, ");

        // Months
        long months = time / 2592000000L;
        time %= 2592000000L;
        if (months > 1) response.append(months).append(" Monate, ");
        else if (months > 0) response.append(months).append(" Monat, ");

        // Weeks
        long weeks = time / 604800000L;
        time %= 604800000L;
        if (weeks > 1) response.append(weeks).append(" Wochen, ");
        else if (weeks > 0) response.append(weeks).append(" Woche, ");

        // Days
        long days = time / 86400000L;
        time %= 86400000L;
        if (days > 1) response.append(days).append(" Tage, ");
        else if (days > 0) response.append(days).append(" Tag, ");

        // Hours
        long hours = time / 3600000L;
        time %= 3600000L;
        if (hours > 1) response.append(hours).append(" Stunden, ");
        else if (hours > 0) response.append(hours).append(" Stunde, ");

        // Minutes
        long minutes = time / 60000L;
        time %= 60000L;
        if (minutes > 1) response.append(minutes).append(" Minuten, ");
        else if (minutes > 0) response.append(minutes).append(" Minute, ");

        // Seconds
        long seconds = time / 1000L;
        if (seconds > 1) response.append(seconds).append(" Sekunden, ");
        else if (seconds > 0) response.append(seconds).append(" Sekunde, ");

        // Return
        return response.substring(0, response.length() - 2);
    }
}