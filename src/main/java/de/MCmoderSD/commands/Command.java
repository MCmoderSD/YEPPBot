package de.MCmoderSD.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

public abstract class Command {

    // Attributes
    private final String command; // Name
    private final String[] alias; // Alias
    private final boolean whitelist; // Whitelist
    private final boolean blacklist; // Blacklist

    // Constructor
    public Command(String... command) {

        // Null Check
        if (command.length == 0)
            throw new IllegalArgumentException("Command name missing!");

        // Set attributes
        this.command = command[0]; // Name
        this.alias = command; // Alias

        // Set List
        this.whitelist = false; // Whitelist
        this.blacklist = false; // Blacklist
    }

    public Command(boolean whitelist, boolean blacklist, String... command) {

        // Null Check
        if (command.length == 0)
            throw new IllegalArgumentException("Command name missing!");

        // Set attributes
        this.command = command[0]; // Name
        this.alias = command; // Alias

        // Set List
        this.whitelist = whitelist;
        this.blacklist = blacklist;
    }


    // Methods
    public abstract void execute(ChannelMessageEvent event, String... args); // Execute the command

    // Getter
    public String getCommand() {
        return command;
    } // Get the command

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
