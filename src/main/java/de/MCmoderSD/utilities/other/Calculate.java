package de.MCmoderSD.utilities.other;

import de.MCmoderSD.objects.TwitchMessageEvent;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Color;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Calculate {

    // Constants
    public final static String BOLD = "\033[0;1m";
    public final static String UNBOLD = "\u001B[0m";
    public final static String BREAK = "\n";
    public final static String SYSTEM = "[SYS]";
    public final static String USER = "[USR]";
    public final static String COMMAND = "[CMD]";
    public final static String RESPONSE = "[RSP]";
    public final static String MESSAGE = "[MSG]";
    public final static String CHEER = "[CHR]";
    public final static String SUBSCRIBE = "[SUB]";
    public final static Color DARK = new Color(0x0e0e10);
    public final static Color LIGHT = new Color(0x18181b);
    public final static Color PURPLE = new Color(0x771fe2);


    // Methods

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

    // Tag the author
    public static String tagUser(TwitchMessageEvent event) {
        return "@" + event.getUser();
    }

    // Tag the channel
    public static String tagChannel(TwitchMessageEvent event) {
        return "@" + event.getChannel();
    }

    // Log timestamp
    public static String logTimestamp() {
        return "[" + new java.text.SimpleDateFormat("dd-MM-yyyy|HH:mm:ss").format(new java.util.Date()) + "]";
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

    // Get Timestamp
    public static Timestamp getTimestamp() {
        return new Timestamp(new java.util.Date().getTime());
    }

    public String getSubTier(int tier) {
        if (tier != 0) return "TIER" + tier;
        else return "NONE";
    }
}