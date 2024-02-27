package de.MCmoderSD.events;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

public abstract class Event {

    // Attributes
    private final String event; // Name
    private final String[] alias; // Alias

    // Constructor
    public Event(String... event) {
        this.event = event[0];
        this.alias = event;
    }

    // Methods
    public abstract void execute(ChannelMessageEvent event); // Execute the event

    // Getter
    public String getEvent() {
        return event;
    } // Get the event

    public String[] getAlias() {
        return alias;
    } // Get the alias
}
