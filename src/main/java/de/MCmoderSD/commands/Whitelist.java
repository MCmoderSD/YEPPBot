package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Whitelist {

    // Constructor
    public Whitelist(BotClient botClient, MessageHandler messageHandler, MySQL mySQL) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "whitelist join/kick <McUsername>";

        // About
        String[] name = {"whitelist"};
        String description = "Adde dich selbst auf die Minecraft Server Whitelist" + syntax;

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Check args
                if (args.isEmpty()) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                String verb = args.getFirst().toLowerCase();
                if (!Arrays.asList("join", "kick").contains(verb) && args.size() < 2) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                if (Objects.equals("kick", verb) && !(botClient.isPermitted(event) || botClient.isAdmin(event))) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                if (Objects.equals("kick", verb)) botClient.respond(event, getCommand(), mySQL.getYEPPConnect().editWhitelist(event, args.get(1).toLowerCase(), false));
                else botClient.respond(event, getCommand(), mySQL.getYEPPConnect().editWhitelist(event, args.get(1).toLowerCase(), true));
            }
        });
    }
}