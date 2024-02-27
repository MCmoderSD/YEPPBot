package de.MCmoderSD.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

public abstract class Command {

    // Attributes
    private final String command; // Name
    private final String[] alias; // Alias

    // Constructor
    public Command(String... command) {
        this.command = command[0];
        this.alias = command;
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
}
