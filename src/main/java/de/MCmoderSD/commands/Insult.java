package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Insult {

    // Constructor
    public Insult(BotClient botClient, MessageHandler messageHandler, MySQL mySQL) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "insult <Nutzer> <en/de>";

        // About
        String[] name = {"insult", "beleidige", "fronte", "mobbe", "mobbing"};
        String description = "Beleidigt einen Nutzer. " + syntax;


        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Determine language
                boolean isEnglish = false;
                if (args.size() > 1) isEnglish = trimMessage(args.get(1)).toLowerCase().startsWith("en");
                String insult = mySQL.getAssetManager().getInsult(isEnglish ? "en" : "de");

                // Gets target, insults the author if no target is provided
                String target = tagUser(event);
                if (!args.isEmpty() && args.getFirst().length() > 2) target = trimMessage(args.getFirst());
                if (target.startsWith("@")) target = target.substring(1);
                String message = insult.replace("%member%", '@' + target);

                // Send message
                botClient.respond(event, getCommand(), trimMessage(message));
            }
        });
    }
}