package de.MCmoderSD.other;

import java.awt.*;

import static de.MCmoderSD.other.Util.endsWith;
import static de.MCmoderSD.other.Util.startsWith;

public class Format {

    // Formatting
    public final static String BOLD = "\033[0;1m";
    public final static String UNBOLD = "\u001B[0m";
    public final static String BREAK = "\n";
    public final static String TAB = "\t";
    public final static String SPACE = " ";
    public final static String EMPTY = "";
    public final static String CLEAR = "\033[H\033\\[2J";

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

        // Replace Invalid Characters
        while (message.contains(INVALID_CHARS)) message = message.replaceAll(INVALID_CHARS, SPACE);
        while (message.contains(BREAK)) message = message.replaceAll(BREAK, SPACE);
        while (message.contains(TAB)) message = message.replaceAll(TAB, SPACE);
        while (message.contains(SPACE + SPACE)) message = message.replaceAll(SPACE + SPACE, SPACE);

        // Return
        return message;
    }

    // Trim Message
    public static String trimMessage(String message) {

        // Normalize Message
        message = normalizeMessage(message);

        // Replace Invalid Characters
        message = message.replaceAll(INVALID_UNICODE, EMPTY);

        // Trim Message
        message = message.trim();
        while (startsWith(message, SPACE, TAB, BREAK)) message = message.substring(1);
        while (endsWith(message, SPACE, TAB, BREAK)) message = message.substring(0, message.length() - 1);

        // Return
        return message;
    }


}
