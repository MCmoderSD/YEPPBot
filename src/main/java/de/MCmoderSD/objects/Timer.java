package de.MCmoderSD.objects;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.utilities.database.MySQL;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Timer {

    // Associations
    private final TwitchChat chat;
    private final MySQL mySQL;

    // Attributes
    private final String name;
    private final String channel;
    private final long time;
    private final String response;

    // Variables
    private long counter;

    // Constructor
    public Timer(TwitchChat chat, MySQL mySQL, String channel, String name, String time, String response) {
        this.chat = chat;
        this.mySQL = mySQL;
        this.channel = channel;
        this.name = name;
        var temp = time.split("M"); // Remove the M TEMPORARY
        this.time = Long.parseLong(temp[0]);
        this.response = response;

        // Init Counter
        counter = 1;
    }

    public void trigger(ChannelMessageEvent event) {
        if (counter == time) execute(event);
        else counter++;
    }

    private void execute(ChannelMessageEvent event) {

        // Send Message
        chat.sendMessage(channel, response);

        // Reset Counter
        counter = 1;

        // Log response
        System.out.printf("%s%s %s <%s> Executed: %s%s%s", BOLD, logTimestamp(), COMMAND, channel, name + ": " + response, BREAK, UNBOLD);
        mySQL.logResponse(event, "Timer: " + name, ""   , response);
    }


    // Getter
    public String getChannel() {
        return channel;
    }

    public String getName() {
        return name;
    }

    public long getTime() {
        return time;
    }

    public String getResponse() {
        return response;
    }
}