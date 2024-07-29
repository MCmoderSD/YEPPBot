package de.MCmoderSD.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.events.Event;

import de.MCmoderSD.utilities.database.MySQL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class InteractionHandler {

    // Associations
    private final MySQL mySQL;

    // Constants
    private final JsonNode whiteList;
    private final JsonNode blackList;

    // Attributes
    private final HashMap<String, Event> interactions;
    private final HashMap<String, String> aliases;
    private final HashMap<Event, ArrayList<String>> whiteListMap;
    private final HashMap<Event, ArrayList<String>> blackListMap;

    // Variables
    private HashMap<Integer, Integer> lurkList;

    // Constructor
    public InteractionHandler(MySQL mySQL, JsonNode whiteList, JsonNode blackList) {

        // Init Constants and Attributes
        this.mySQL = mySQL;
        this.whiteList = whiteList;
        this.blackList = blackList;
        interactions = new HashMap<>();
        aliases = new HashMap<>();
        whiteListMap = new HashMap<>();
        blackListMap = new HashMap<>();
        updateLurkList();
    }

    // Register a command
    public void registerEvent(Event event) {

        // Register command
        String name = event.getEvent().toLowerCase();
        interactions.put(name, event);

        // Register aliases
        for (String alias : event.getAlias()) aliases.put(alias.toLowerCase(), name);

        // White and Blacklist
        if (whiteList.has(name)) whiteListMap.put(event, new ArrayList<>(Arrays.asList(whiteList.get(name).asText().toLowerCase().split("; "))));
        if (blackList.has(name)) blackListMap.put(event, new ArrayList<>(Arrays.asList(blackList.get(name).asText().toLowerCase().split("; "))));
    }

    // Execute interaction
    public void executeInteraction(ChannelMessageEvent event, String interaction) {
        if (interactions.containsKey(interaction) || aliases.containsKey(interaction)) {

            // Check for alias
            if (aliases.containsKey(interaction)) interaction = aliases.get(interaction);

            // Get Interaction
            Event interactionEvent = getInteraction(interaction);

            // Check for whitelist
            if (whiteListMap.containsKey(interactionEvent) && !whiteListMap.get(interactionEvent).contains(getChannel(event)))
                return;

            // Check for blacklist
            if (blackListMap.containsKey(interactionEvent) && blackListMap.get(interactionEvent).contains(getChannel(event))) return;

            // Log command execution
            mySQL.logCommand(event, interactionEvent.getEvent(), "");

            // Execute command
            interactionEvent.execute(event);

            // Output to console
            System.out.printf("%s%s %s <%s> Executed: %s%s%s", BOLD, logTimestamp(), COMMAND, getChannel(event), interaction, BREAK, UNBOLD);
        }
    }

    // Handle message event and check for interactions
    public void handleInteraction(ChannelMessageEvent event, String botName) {
        new Thread(() -> {

            // Get message
            String message = getMessage(event);

            // Check for bot
            if (getAuthor(event).equals(botName)) return;

            // Process message and check for interactions
            for (String string : message.split(" ")) {
                if (interactions.containsKey(string.toLowerCase())) {
                    executeInteraction(event, string.toLowerCase());
                    return;
                }
            }

            // Check for lurk
            if (lurkList.containsKey(getUserID(event))) interactions.get("$stoppedlurk").execute(event);
        }).start();
    }

    // Setter and Getter
    public void updateLurkList() {
        lurkList = mySQL.getLurkList();
    }

    public Event getInteraction(String action) {
        return interactions.get(action);
    }
}