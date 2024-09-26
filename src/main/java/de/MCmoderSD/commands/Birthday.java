package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.openAI.OpenAI;

import java.util.ArrayList;

public class Birthday {

    // Constructor
    public Birthday(BotClient botClient, MessageHandler messageHandler, MySQL mySQL, OpenAI openAI) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "birthday <set> <date>";

        // About
        String[] name = {"birthday", "bday", "geburtstag"};
        String description = "Setzt deinen Geburtstag. " + syntax;


        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {


            }
        });
    }
}