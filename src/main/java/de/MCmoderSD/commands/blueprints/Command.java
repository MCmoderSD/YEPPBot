package de.MCmoderSD.commands.blueprints;

import de.MCmoderSD.objects.MessageEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public abstract class Command {

    // Attributes
    protected final String name;              // Name
    protected final HashSet<String> aliases;  // Alias
    protected final String description;       // Description

    // Constructor
    public Command(String description, String... name) {

        // Check Parameters
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Description cannot be null or empty");
        if (name == null || name.length == 0) throw new IllegalArgumentException("Name cannot be null or empty");
        for (var alias : name) if (alias == null || alias.isBlank()) throw new IllegalArgumentException("Name and aliases cannot be null or empty");

        // Check Length
        for (var alias : name) if (alias.length() > 500) throw new IllegalArgumentException("Name or aliases cannot be longer than 500 characters");

        // Set Name
        this.name = name[0];

        // Set Aliases
        aliases = new HashSet<>();
        aliases.addAll(Arrays.asList(name).subList(1, name.length));
        aliases.remove(this.name);

        // Set Description
        this.description = description;
    }

    // Methods
    public abstract boolean execute(MessageEvent event, ArrayList<String> args);

    // Getter
    public String getName() {
        return name;
    }

    public HashSet<String> getAliases() {
        return aliases;
    }

    public String getDescription() {
        return description;
    }
}