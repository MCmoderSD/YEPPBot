package de.MCmoderSD.utilities.other;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Color;
import java.sql.Timestamp;

public class Calculate {

    // Constants
    public final static String BOLD = "\033[0;1m";
    public final static String UNBOLD = "\u001B[0m";
    public final static String BREAK = "\n";
    public final static String SYSTEM = "[SYS]";
    public final static String COMMAND = "[CMD]";
    public final static String MESSAGE = "[MSG]";
    public final static String FOLLOW = "[FLW]";
    public final static String SUBSCRIBE = "[SUB]";
    public final static String USER = "[USR]";
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

    // Get the author
    public static String getAuthor(ChannelMessageEvent event) {
        return event.getUser().getName();
    }

    // Tag the author
    public static String tagAuthor(ChannelMessageEvent event) {
        return "@" + getAuthor(event);
    }

    // Get the message
    public static String getMessage(ChannelMessageEvent event) {
        return event.getMessage();
    }

    // Get Channel
    public static String getChannel(ChannelMessageEvent event) {
        return event.getChannel().getName();
    }

    // Tag the channel
    public static String tagChannel(ChannelMessageEvent event) {
        return "@" + getChannel(event);
    }

    // Get Channel ID
    public static int getChannelID(ChannelMessageEvent event) {
        return Integer.parseInt(event.getChannel().getId());
    }

    // Get User ID
    public static int getUserID(ChannelMessageEvent event) {
        return Integer.parseInt(event.getUser().getId());
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
    public static String processArgs(String... args) {
        return trimMessage(String.join(" ", args)).trim();
    }

    // Get Timestamp
    public static Timestamp getTimestamp() {
        return new Timestamp(new java.util.Date().getTime());
    }
}