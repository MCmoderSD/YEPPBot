package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.handlers.LurkHandler;
import de.MCmoderSD.objects.MessageEvent;

import java.util.ArrayList;

public class Lurk extends CommandBuilder {

    // Constructor
    public Lurk(TwitchBot twitchBot) {
        super(twitchBot);

        // About
        String[] name = new String[]{ "lurk", "lörk", "lürk", "lork", "afk" };
        String description = "Sendet den Befehl " + prefix + "lurk in den Chat, um im Lurk zu sein";

        // Get Lurk Handler
        LurkHandler lurkHandler = twitchBot.getEventHandler().getLurkHandler();

        // Register command
        boolean registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Add Lurk
                lurkHandler.addLurk(event);

                // Send Message
                //return twitchBot.sendMessage(event, "Lurk-Init", tagUser(event.getUser(), " ist jetzt im Lurk!"));
                return true;
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }
}