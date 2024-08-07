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

        // Set attributes
        this.command = command[0]; // Name
        this.alias = command; // Alias
        this.description = description; // Description
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