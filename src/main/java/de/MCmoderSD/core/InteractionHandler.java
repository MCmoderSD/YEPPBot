package de.MCmoderSD.core;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.events.Event;

import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.json.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static de.MCmoderSD.utilities.Calculate.*;

public class InteractionHandler {

    // Associations
    private final MySQL mySQL;

    // Constants
    private final JsonNode whiteList;
    private final JsonNode blackList;

    // Attributes
    private final HashMap<String, Event> interactions;
    private final HashMap<String, String> aliases;
    private final HashMap<String, String> lurkChannel;
    private final HashMap<Event, ArrayList<String>> whiteListMap;
    private final HashMap<Event, ArrayList<String >> blackListMap;

    // Constructor
    public InteractionHandler(MySQL mySQL, JsonNode whiteList, JsonNode blackList, HashMap<String, String> lurkChannel) {

        // Init Constants and Attributes
        this.mySQL = mySQL;
        this.lurkChannel = lurkChannel;
        this.whiteList = whiteList;
        this.blackList = blackList;
        interactions = new HashMap<>();
        aliases = new HashMap<>();
        whiteListMap = new HashMap<>();
        blackListMap = new HashMap<>();
    }

    // Register a command
    public void registerEvent(Event event) {

        // Register command
        String name = event.getEvent().toLowerCase();
        interactions.put(name, event);

        // Register aliases
        for (String alias : event.getAlias()) aliases.put(alias.toLowerCase(), name);

        // White and Blacklist
        if (whiteList.containsKey(name)) whiteListMap.put(event, new ArrayList<>(Arrays.asList(whiteList.get(name).asText().toLowerCase().split("; "))));
        if (blackList.containsKey(name)) blackListMap.put(event, new ArrayList<>(Arrays.asList(blackList.get(name).asText().toLowerCase().split("; "))));
    }

    // Manually execute a command
    public void executeInteracton(ChannelMessageEvent event, String interaction) {
        if (interactions.containsKey(interaction) || aliases.containsKey(interaction)) {

            // Check for alias
            if (aliases.containsKey(interaction)) interaction = aliases.get(interaction);

            // Get Interaction
            Event interactionEvent = getInteraction(interaction);

            // Check for whitelist
            if (whiteListMap.containsKey(interactionEvent) && !whiteListMap.get(interactionEvent).contains(getChannel(event))) return;

            // Check for blacklist
            if (blackListMap.containsKey(interactionEvent) && blackListMap.get(interactionEvent).contains(getChannel(event))) return;

            // Execute command
            interactionEvent.execute(event);

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

    @SuppressWarnings("unused")
    public void removeInteraction(String action) {
        interactions.remove(action);
    }
}