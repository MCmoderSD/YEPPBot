package de.MCmoderSD.commands;

import de.MCmoderSD.objects.TwitchMessageEvent;
import org.apache.commons.lang.IncompleteArgumentException;

import java.util.ArrayList;

public abstract class Command {

    // Attributes
    private final String command; // Name
    private final String[] alias; // Alias
    private final String description; // Description

    // Constructor
    public Command(String description, String... command) {

        // Null Check
        if (command.length == 0)
            throw new IncompleteArgumentException("Command name missing!");

        // Set Attributes
        this.command = command[0].toLowerCase();

        // Set Alias
        String[] aliases = command.length == 1 ? new String[0] : new String[command.length - 1];
        for (var i = 1; i < command.length; i++) aliases[i - 1] = command[i].toLowerCase();
        this.alias = aliases;

        // Set Description
        this.description = description;
    }

    // Methods
    public abstract void execute(TwitchMessageEvent event, ArrayList<String> args); // Execute the command

    // Getter
    public String getCommand() {
        return command;
    } // Get the command

    public String[] getAlias() {
        return alias;
    } // Get the alias

    public String getDescription() {
        return description;
    } // Get the description
}