package de.MCmoderSD.events;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

public abstract class Event {

    // Attributes
    private final String event; // Name
    private final String[] alias; // Alias
    private final boolean whitelist; // Whitelist
    private final boolean blacklist; // Blacklist

    // Constructor
    public Event(String... event) {

        // Null Check
        if (event.length == 0)
            throw new IllegalArgumentException("Event name missing!");

        // Set attributes
        this.event = event[0]; // Name
        this.alias = event; // Alias

        // Set List
        this.whitelist = false; // Whitelist
        this.blacklist = false; // Blacklist
    }

    public Event(boolean whitelist, boolean blacklist, String... event) {

        // Null Check
        if (event.length == 0)
            throw new IllegalArgumentException("Event name missing!");

        // Set attributes
        this.event = event[0]; // Name
        this.alias = event; // Alias

        // Set List
        this.whitelist = whitelist; // Whitelist
        this.blacklist = blacklist; // Blacklist
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

    public boolean hasWhitelist() {
        return whitelist;
    } // Get the whitelist

    public boolean hasBlacklist() {
        return blacklist;
    } // Get the blacklist
}