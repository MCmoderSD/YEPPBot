package de.MCmoderSD.core;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

import java.util.ArrayList;
import java.util.Arrays;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class MessageHandler {

    // Associations
    private final BotClient botClient;
    private final MySQL mySQL;
    private final Frame frame;


    public MessageHandler(BotClient botClient, MySQL mySQL, Frame frame) {
        this.botClient = botClient;
        this.mySQL = mySQL;
        this.frame = frame;
    }

    public void handleMessage(TwitchMessageEvent event) {
        new Thread(() -> {

            // Log Message
            mySQL.logMessage(event);
            event.logToConsole();
            frame.log(event.getType(), event.getChannel(), event.getUser(), event.getMessage());

            // Check for Command
            if (event.hasCommand()) {
                handleCommand(event);
                return;
            }

            // Reply YEPP
            if (event.hasBotName()) {
                botClient.respond(event, "@" + BotClient.botName, tagUser(event) + " YEPP");
                return;
            }

            // Say YEPP
            if (event.hasYEPP()) botClient.respond(event, "YEPP","YEPP");
        }).start();
    }

    private void handleCommand(TwitchMessageEvent event) {

        // Variables
        ArrayList<String> parts = formatCommand(event);
        String command = parts.getFirst();

        // Check for Command
    }

    private ArrayList<String> formatCommand(TwitchMessageEvent event) {

        // Variables
        String message = event.getMessage();

        // Find Start
        if (message.indexOf(BotClient.prefix) == 0) message = message.substring(1);
        else message = message.substring(message.indexOf(" " + BotClient.prefix) + 2);

        // Split Command
        String[] split = message.split(" ");
        return new ArrayList<>(Arrays.asList(split));
    }
}
