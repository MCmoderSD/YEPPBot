package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.core.TwitchBot;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.MessageHelper.SPACE;

public class Help extends CommandBuilder {

    // Constructor
    public Help(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        var syntax = "Syntax: " + prefix + "Help [command]";

        // About
        var name = new String[]{ "Help", "Hilfe" };
        var description = "Zeigt eine Liste aller Befehle oder Informationen zu einem bestimmten Befehl an. " + syntax;


        // Register command
        var registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Get Command List
                var commands = new ArrayList<>(commandHandler.getCommands(event.getChannel()));
                commands.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));

                // Check Arguments
                if (!args.isEmpty()) {

                    // Get Command
                    var commandName = args.getFirst().toLowerCase();

                    // Find Command
                    for (var command : commands) {

                        // Check Name and Aliases
                        var matches = commandName.equalsIgnoreCase(command.getName());
                        for (var alias : command.getAliases()) if (commandName.equalsIgnoreCase(alias)) {
                            matches = true;
                            break;
                        }

                        // If Command matches, send Description
                        if (matches) return twitchBot.sendMessage(event, name, "Befehl: " + prefix + command.getName() + ", Beschreibung: " + command.getDescription());
                    }
                }

                // Build Response
                var response = new StringBuilder("Verfügbare Befehle: ");
                for (var command : commands) response.append(prefix).append(command.getName()).append(SPACE);

                // Send Message
                return twitchBot.sendMessage(event, name, response.toString());
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }
}