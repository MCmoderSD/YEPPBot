package de.MCmoderSD.utilities;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import javax.swing.*;
import java.awt.*;
import java.sql.Time;

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

    // Log timestamp
    public static String logTimestamp() {
        return "[" + new java.text.SimpleDateFormat("dd-MM-yyyy|HH:mm:ss").format(new java.util.Date()) + "]";
    }

    // Log date
    public static java.sql.Date logDate() {
        return new java.sql.Date(new java.util.Date().getTime());
    }

    // Log time
    public static Time logTime() {
        return new Time(new java.util.Date().getTime());
    }

    // Strip Brackets
    public static String stripBrackets(String string) {
        return string.replaceAll("[\\[\\]]", "");
    }
}