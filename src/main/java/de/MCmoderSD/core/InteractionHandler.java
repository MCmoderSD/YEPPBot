package de.MCmoderSD.core;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.events.Event;
import de.MCmoderSD.utilities.database.MySQL;

import java.util.HashMap;

import static de.MCmoderSD.utilities.Calculate.*;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class InteractionHandler {

    // Associations
    private final MySQL mySQL;

    // Attributes
    private final HashMap<String, Event> interactions;
    private final HashMap<String, String> aliases;
    private final HashMap<String, String> lurkChannel;
    private final HashMap<String, Long> lurkTime;

    // Constructor
    public InteractionHandler(MySQL mySQL, String botName, HashMap<String, String> lurkChannel, HashMap<String, Long> lurkTime) {
        this.mySQL = mySQL;
        this.lurkChannel = lurkChannel;
        this.lurkTime = lurkTime;
        interactions = new HashMap<>();
        aliases = new HashMap<>();
    }

    // Register a command
    public void registerEvent(Event event) {

        // Register command
        String name = event.getEvent().toLowerCase();
        interactions.put(name, event);

        // Register aliases
        for (String alias : event.getAlias()) aliases.put(alias.toLowerCase(), name);
    }

    // Manually execute a command
    public void executeInteracton(ChannelMessageEvent event, String interaction) {
        System.out.println(interaction);
        if (interactions.containsKey(interaction) || aliases.containsKey(interaction)) {

            // Check for alias
            if (aliases.containsKey(interaction)) interaction = aliases.get(interaction);

            // Execute command
            getInteraction(interaction).execute(event);

            // Log command execution
            System.out.printf("%s%s %s <%s> Executed: %s%s%s", BOLD, logTimestamp(), COMMAND, getChannel(event), interaction, BREAK, UNBOLD);
            mySQL.log(logDate(), logTime(), stripBrackets(COMMAND), getChannel(event), getAuthor(event), interaction);
        }
    }

    public void handleInteraction(ChannelMessageEvent event, String botName) {
        String message = getMessage(event);

        if (getAuthor(event).equals(botName)) return;

        for (String string : message.split(" ")) {
            if (interactions.containsKey(string.toLowerCase())) {
                executeInteracton(event, string.toLowerCase());
                return;
            }
        }

        if (lurkChannel.containsKey(getAuthor(event))) {
            if (lurkChannel.get(getAuthor(event)).equals(getChannel(event)))
                interactions.get("$stoppedlurk").execute(event);
        }
    }

    // Setter and Getter
    public HashMap<String, Event> getEvents() {
        return interactions;
    }

    public Event getInteraction(String action) {
        return interactions.get(action);
    }

    public void removeInteraction(String action) {
        interactions.remove(action);
    }
}
