package de.MCmoderSD.utilities.other;

import de.MCmoderSD.objects.TwitchMessageEvent;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Color;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.ArrayList;

public class Calculate {

    // Constants
    public final static String BOLD = "\033[0;1m";
    public final static String UNBOLD = "\u001B[0m";
    public final static String BREAK = "\n";

    // Tags
    public final static String SYSTEM = "[SYS]";
    public final static String USER = "[USR]";
    public final static String COMMAND = "[CMD]";
    public final static String RESPONSE = "[RSP]";
    public final static String EVENT = "[EVT]";
    public final static String MESSAGE = "[MSG]";
    public final static String CHEER = "[CHR]";
    public final static String FOLLOW = "[FLW]";
    public final static String SUBSCRIBE = "[SUB]";
    public final static String GIFT = "[GFT]";
    public final static String RAID = "[RAD]";

    // Colors
    public final static Color DARK = new Color(0x0e0e10);
    public final static Color LIGHT = new Color(0x18181b);
    public final static Color PURPLE = new Color(0x771fe2);
    public final static Color WHITE = new Color(0xffffff);

    // Center JFrame
    public static Point centerJFrame(JFrame frame) {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (dim.width - frame.getWidth()) / 2;
        int y = (dim.height - frame.getHeight()) / 2;
        return new Point(x, y);
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

    // Log timestamp
    public static String logTimestamp() {
        return "[" + new java.text.SimpleDateFormat("dd-MM-yyyy|HH:mm:ss").format(new java.util.Date()) + "]";
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

        return ascii;
    }

    // Trim Message
    public static String trimMessage(String message) {
        while (message.startsWith(" ") || message.startsWith("\n")) message = message.substring(1);
        while (message.endsWith(" ") || message.endsWith("\n")) message = message.trim();
        return message;
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
    public static String formatOpenAiResponse(String response, String emote) {
        return removeRepetitions(replaceEmojis(response.replaceAll("(?i)" + emote + "[.,!?\\s]*", emote + " "), emote), emote);
    }

    // Get Timestamp
    public static Timestamp getTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static String getFormattedTimestamp() {
        return "[" + new java.text.SimpleDateFormat("dd-MM-yyyy|HH:mm:ss").format(getTimestamp()) + "]";
    }
}