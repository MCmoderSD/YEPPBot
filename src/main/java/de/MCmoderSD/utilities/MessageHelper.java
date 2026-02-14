package de.MCmoderSD.utilities;

import de.MCmoderSD.helix.objects.TwitchUser;

import static java.lang.Character.isWhitespace;

@SuppressWarnings("unused")
public class MessageHelper {

    // Formatting
    public final static String BOLD = "\033[0;1m";
    public final static String UNBOLD = "\u001B[0m";
    public final static String BREAK = "\n";
    public final static String TAB = "\t";
    public final static String SPACE = " ";
    public final static String EMPTY = "";

    // Tags
    public final static String SYSTEM = "[SYS]";
    public final static String BOT = "[BOT]";
    public final static String USER = "[USR]";
    public final static String EVENT = "[EVT]";
    public final static String COMMAND = "[CMD]";
    public final static String MESSAGE = "[MSG]";
    public final static String WARNING = "[WRN]";
    public final static String ERROR = "[ERR]";
    public final static String INFO = "[INF]";
    public final static String DEBUG = "[DBG]";

    //Patterns
    public final static String TIMESTAMP_FORMAT = "dd-MM-yyyy|HH:mm:ss";
    public final static String REGEX_PATTERN = "[^\\p{L}\\p{N}\\p{S}\\p{P} ]+";

    // Icon
    public final static String ICON =
            """
                    ⠄⠄⠄⠄⠄⠄⠄⣠⣴⣶⣿⣿⡿⠶⠄⠄⠄⠄⠐⠒⠒⠲⠶⢄⠄⠄⠄⠄⠄⠄
                    ⠄⠄⠄⠄⠄⣠⣾⡿⠟⠋⠁⠄⢀⣀⡀⠤⣦⢰⣤⣶⢶⣤⣤⣈⣆⠄⠄⠄⠄⠄
                    ⠄⠄⠄⠄⢰⠟⠁⠄⢀⣤⣶⣿⡿⠿⣿⣿⣊⡘⠲⣶⣷⣶⠶⠶⠶⠦⠤⡀⠄⠄
                    ⠄⠔⠊⠁⠁⠄⠄⢾⡿⣟⡯⣖⠯⠽⠿⠛⠛⠭⠽⠊⣲⣬⠽⠟⠛⠛⠭⢵⣂⠄
                    ⡎⠄⠄⠄⠄⠄⠄⠄⢙⡷⠋⣴⡆⠄⠐⠂⢸⣿⣿⡶⢱⣶⡇⠄⠐⠂⢹⣷⣶⠆
                    ⡇⠄⠄⠄⠄⣀⣀⡀⠄⣿⡓⠮⣅⣀⣀⣐⣈⣭⠤⢖⣮⣭⣥⣀⣤⣤⣭⡵⠂⠄
                    ⣤⡀⢠⣾⣿⣿⣿⣿⣷⢻⣿⣿⣶⣶⡶⢖⣢⣴⣿⣿⣟⣛⠿⠿⠟⣛⠉⠄⠄⠄
                    ⣿⡗⣼⣿⣿⣿⣿⡿⢋⡘⠿⣿⣿⣷⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⡀⠄⠄
                    ⣿⠱⢿⣿⣿⠿⢛⠰⣞⡛⠷⣬⣙⡛⠻⠿⠿⠿⣿⣿⣿⣿⣿⣿⣿⠿⠛⣓⡀⠄
                    ⢡⣾⣷⢠⣶⣿⣿⣷⣌⡛⠷⣦⣍⣛⠻⠿⢿⣶⣶⣶⣦⣤⣴⣶⡶⠾⠿⠟⠁⠄
                    ⣿⡟⣡⣿⣿⣿⣿⣿⣿⣿⣷⣦⣭⣙⡛⠓⠒⠶⠶⠶⠶⠶⠶⠶⠶⠿⠟⠄⠄⠄
                    ⠿⡐⢬⣛⡻⠿⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⡶⠟⠃⠄⠄⠄⠄⠄⠄
                    ⣾⣿⣷⣶⣭⣝⣒⣒⠶⠬⠭⠭⠭⠭⠭⠭⠭⣐⣒⣤⣄⡀⠄⠄⠄⠄⠄⠄⠄⠄
                    ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣦⠄⠄⠄⠄⠄⠄⠄
                    """;

    // Normalize Message
    public static String normalizeMessage(String message) {

        // Check Parameters
        if (message == null) throw new IllegalArgumentException("Message cannot be null");
        if (message.isBlank()) return EMPTY;

        // Remove unwanted characters
        do {
            message = message.replaceAll(REGEX_PATTERN, SPACE);
        } while (message.matches(REGEX_PATTERN));

        // Trim Message
        while (message.contains(BREAK)) message = message.replaceAll(BREAK, SPACE);
        while (message.startsWith(BREAK)) message = message.substring(1);
        while (message.endsWith(BREAK)) message  = message.substring(0, message.length() - 1);
        while (message.contains(TAB))  message = message.replaceAll(TAB, SPACE);
        while (message.startsWith(TAB)) message = message.substring(1);
        while (message.endsWith(TAB)) message = message.substring(0, message.length() - 1);
        while (message.contains(SPACE + SPACE)) message = message.replaceAll(SPACE + SPACE, SPACE);
        while (message.startsWith(SPACE)) message = message.substring(1);
        while (message.endsWith(SPACE)) message = message.substring(0, message.length() - 1);
        if (message.isBlank()) return EMPTY;

        // Loop to remove duplicate spaces
        message = removeDuplicateSpaces(message);
        if (message.isBlank()) return EMPTY;

        // Check Message
        var firstChar = message.charAt(0);
        var lastChar = message.charAt(message.length() - 1);
        if (isWhitespace(firstChar)) throw new IllegalStateException("Message still starts with whitespace after normalization");
        if (isWhitespace(lastChar)) throw new IllegalStateException("Message still ends with whitespace after normalization");
        if (message.contains(SPACE + SPACE)) throw new IllegalStateException("Message still contains duplicate spaces after normalization");
        if (message.isBlank()) throw new IllegalStateException("Message still contains only whitespace after normalization");

        // Return
        return message;
    }

    // Remove Duplicate Spaces Helper
    private static String removeDuplicateSpaces(String message) {

        // Check Parameters
        if (message == null) throw new IllegalArgumentException("Message cannot be null");
        if (message.isEmpty()) throw new IllegalArgumentException("Message cannot be empty");

        // Get First and Last Character
        var firstChar = message.charAt(0);
        var lastChar = message.charAt(message.length() - 1);

        // Trim Message
        if (isWhitespace(firstChar)) removeDuplicateSpaces(message.substring(1));
        if (isWhitespace(lastChar)) removeDuplicateSpaces(message.substring(0, message.length() - 1));

        // Loop to remove duplicate spaces
        char[] messageChars = message.toCharArray();
        char[] result = new char[messageChars.length];
        result[0] = messageChars[0];
        for (var i = 1; i < messageChars.length; i++) {
            if (isWhitespace(messageChars[i]) && isWhitespace(messageChars[i - 1])) continue;
            result[i] = messageChars[i];
        }
        return new String(result).trim();
    }

    // Format OpenAI Response
    public static String formatOpenAI(String content) {

        // Check Parameters
        if (content == null) throw new IllegalArgumentException("Content cannot be null");
        if (content.isBlank()) throw new IllegalArgumentException("Content cannot be blank");

        // Format OpenAI Response
        return normalizeMessage(content.replaceAll("YEPPYEPP", "YEPP"));
    }

    // Tag User
    public static String tagUser(TwitchUser user) {

        // Check Parameters
        if (user == null) throw new IllegalArgumentException("TwitchUser cannot be null");

        // Tag User
        return "@" + user.getDisplayName();
    }
}