package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.core.TwitchBot;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.MessageHelper.SPACE;

public class Say extends CommandBuilder {

    // Constructor
    public Say(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        var syntax = "Syntax: " + prefix + "Say <Nachricht>";

        // About
        var name = new String[]{ "Say", "Repeat" };
        var description = "Nur für Moderatoren und Administratoren. Sendet eine Nachricht in den Chat. " + syntax;


        // Register command
        var registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Check Permissions
                if (!twitchBot.isPermitted(event.getUser(), event.getChannel())) return false;

                // Send Message
                return twitchBot.sendMessage(event, name, String.join(SPACE, args));
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }
}