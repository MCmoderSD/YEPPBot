package de.MCmoderSD.core;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

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
            System.out.println(event.getLog());
            frame.log(event.getType(), event.getChannel(), event.getUser(), event.getMessage());

            // Check for Command
            if (event.hasCommand()) {
                handleCommand(event);
                return;
            }

            // Reply YEPP
            if (event.hasBotName()) {
                botClient.sendMessage(event.getChannel(), tagUser(event) + "YEPP");
                return;
            }

            // Say YEPP
            if (event.hasYEPP()) botClient.sendMessage(event.getMessage(), "YEPP");
        }).start();
    }

    private void handleCommand(TwitchMessageEvent event) {

    }
}
