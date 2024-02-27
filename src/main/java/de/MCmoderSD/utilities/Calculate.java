package de.MCmoderSD.utilities;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

public class Calculate {

    public final static String BOLD = "\033[0;1m";
    public final static String UNBOLD = "\u001B[0m";
    public final static String BREAK = "\n";
    public final static String SYSTEM = "[SYS]";
    public final static String COMMAND = "[CMD]";
    public final static String MESSAGE = "[MSG]";
    public final static String FOLLOW = "[FLW]";
    public final static String SUBSCRIBE = "[SUB]";

    // Format Unix Timestamp
    public static String formatUnixTimestamp(long unixTimestamp) {
        return new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(unixTimestamp * 1000));
    }

    // Replace special characters
    public static String replaceSpecialChars(String input) {

        // Replace ö with oe
        input = input.replace("ö", "oe");

        // Replace ü with ue
        input = input.replace("ü", "ue");

        // Replace ä with ae
        input = input.replace("ä", "ae");

        return input;
    }

    // Get the author
    public static String getAuthor(ChannelMessageEvent event) {
        return event.getUser().getName();
    }

    // Tag the author
    public static String tagAuthor(ChannelMessageEvent event) {
        return "@" + getAuthor(event);
    }

    // Log timestamp
    public static String logTimestamp() {
        return "[" + new java.text.SimpleDateFormat("dd-MM-yyyy|HH:mm:ss").format(new java.util.Date()) + "]";
    }
}
