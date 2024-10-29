package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

import java.util.ArrayList;
import java.util.Calendar;

public class NoNutNovember {

    // Constructor
    public NoNutNovember(BotClient botClient, MessageHandler messageHandler, MySQL mySQL) {

        // About
        String[] name = {"nonutnovember", "nnn"};
        String description = "";

        // Register command
        messageHandler.addCommand(new Command(description, name) {
            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Check if November
                if (Calendar.getInstance().get(Calendar.MONTH) != Calendar.NOVEMBER) {
                    botClient.respond(event, getCommand(), "It's not November, you can nut all you want!");
                    return;
                }


            }
        });
    }
}