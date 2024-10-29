package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

import java.util.ArrayList;
import java.util.Calendar;

public class DickDestroyDecember {

    // Constructor
    public DickDestroyDecember(BotClient botClient, MessageHandler messageHandler, MySQL mySQL) {

        // About
        String[] name = {"dickdestroydecember", "ddd", "destroydickdecember", "dicdestroydecember"};
        String description = "";

        // Register command
        messageHandler.addCommand(new Command(description, name) {
            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Check if December
                if (Calendar.getInstance().get(Calendar.MONTH) != Calendar.DECEMBER) {
                    botClient.respond(event, getCommand(), "It's not December, you don't have to destroy your dick anymore!");
                    return;
                }
            }
        });
    }
}