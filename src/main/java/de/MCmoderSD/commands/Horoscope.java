package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.openAI.OpenAI;

import java.util.ArrayList;

public class Horoscope {

    // Constructor
    public Horoscope(BotClient botClient, MessageHandler messageHandler, MySQL mySQL, OpenAI openAI) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "horoscope <Sternzeichen>";

        // About
        String[] name = {"horoscope", "horoskop", "horoskope"};
        String description = "Zeigt dein Horoskop an. " + syntax;


        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Check Arguments
                if (args.isEmpty()) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                // Send Message

            }
        });
    }
}