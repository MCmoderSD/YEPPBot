package de.MCmoderSD.objects;

import de.MCmoderSD.core.BotClient;

import static de.MCmoderSD.utilities.other.Format.*;

public class Timer {

    // Associations
    private final BotClient botClient;

    // Attributes
    private final String channel;
    private final String name;
    private final char type;
    private final long time;
    private final String response;

    // Variables
    private long counter;

    // Constructor
    public Timer(BotClient botClient, String channel, String name, String time, String response) {

        // Set Associations
        this.botClient = botClient;

        // Set Attributes
        this.channel = channel;
        this.name = name;
        this.type = time.charAt(time.length() - 1);
        this.time = parseTime(time.trim());
        this.response = response;

        // Init Counter
        counter = 1;
    }

    // Methods
    private long parseTime(String timeString) {

            // Variables
            long time = 0;
            String number = timeString.substring(0, timeString.length() - 1);

            // Parse Time
            switch (type) {
                case 's', 'M' -> time = Long.parseLong(number);
                case 'm' -> time = Long.parseLong(number) * 60;
                case 'h' -> time = Long.parseLong(number) * 60 * 60;
            }

            return time;
    }

    // Execute
    private void execute() {

        // Reset Counter
        counter = 1;

        // Log
        System.out.printf("%s%s %s <%s> Executed: %s%s%s", BOLD, getFormattedTimestamp(), COMMAND, channel, "Timer: " + name + ": " + response, BREAK, UNBOLD);

        // Send Message
        botClient.respond(new TwitchMessageEvent(
                botClient.getHelixHandler().getUser(channel).getId(),
                botClient.getHelixHandler().getUser(channel).getId(),
                channel,
                channel,
                response,
                null, null, null), "Timer: " + name, response);
    }

    // Trigger
    public void trigger() {
        if (type == 'M' && counter >= time) execute();
        else counter++;
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