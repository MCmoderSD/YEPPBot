package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

import java.util.ArrayList;

public class Lurk {

    // Constructor
    public Lurk(BotClient botClient, MessageHandler messageHandler, MySQL mySQL) {

        // About
        String[] name = {"lurk", "lürk", "afk", "lörk"}; // Command name and aliases
        String description = "Sendet den Befehl " + botClient.getPrefix() + "lurk in den Chat, um im Lurk zu sein";


        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Save data
                messageHandler.updateLurkList(mySQL.getLurkManager().saveLurk(event));

                // Send message
                botClient.respond(event, getCommand(), "");
            }
        });
    }
}