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
        String syntax = "Syntax: " + prefix + "Help [command]";

        // About
        String[] name = { "Help", "Hilfe" };
        String description = "Zeigt eine Liste aller Befehle oder Informationen zu einem bestimmten Befehl an. " + syntax;


        // Register command
        boolean registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Get Command List
                ArrayList<Command> commands = new ArrayList<>(commandHandler.getCommands(event.getChannel()));
                commands.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));

                // Check Arguments
                if (!args.isEmpty()) {

                    // Get Command
                    String commandName = args.getFirst().toLowerCase();

                    // Find Command
                    for (var command : commands) {

                        // Check Name and Aliases
                        boolean matches = commandName.equalsIgnoreCase(command.getName());
                        for (var alias : command.getAliases()) if (commandName.equalsIgnoreCase(alias)) {
                            matches = true;
                            break;
                        }

                        // If Command matches, send Description
                        if (matches) return twitchBot.sendMessage(event, name, "Befehl: " + prefix + command.getName() + ", Beschreibung: " + command.getDescription());
                    }
                }

                // Build Response
                StringBuilder response = new StringBuilder("Verfügbare Befehle: ");
                for (var command : commands) response.append(prefix).append(command.getName()).append(SPACE);

                // Send Message
                return twitchBot.sendMessage(event, name, response.toString());
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }
}