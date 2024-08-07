package de.MCmoderSD.core;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.commands.Command;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class MessageHandler {

    // Associations
    private final BotClient botClient;
    private final MySQL mySQL;
    private final Frame frame;

    // Attributes
    private final HashMap<String, Command> commandList;
    private final HashMap<String, String> aliasList;
    private final ArrayList<Integer> lurkList;


    public MessageHandler(BotClient botClient, MySQL mySQL, Frame frame) {

        // Initialize Associations
        this.botClient = botClient;
        this.mySQL = mySQL;
        this.frame = frame;

        // Initialize Attributes
        commandList = new HashMap<>();
        aliasList = new HashMap<>();
        lurkList = new ArrayList<>();
    }

    public void handleMessage(TwitchMessageEvent event) {
        new Thread(() -> {

            // Log Message
            mySQL.logMessage(event);
            event.logToConsole();
            frame.log(event.getType(), event.getChannel(), event.getUser(), event.getMessage());

            // Handle Timers;
            handleTimers(event);

            // Check for Lurk
            if (lurkList.contains(event.getUserId())) handleLurk(event);

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

    private void handleLurk(TwitchMessageEvent event) {
        // TODO: Implement Lurk
    }

    private void handleTimers(TwitchMessageEvent event) {
        // TODO: Implement Timers
    }

    private void handleCommand(TwitchMessageEvent event) {

        // Variables
        ArrayList<String> parts = formatCommand(event);
        String trigger = parts.getFirst();

        // Check for Alias
        if (aliasList.containsKey(trigger)) {
            commandList.get(aliasList.get(trigger));
        }

        // Check for Command
        if (commandList.containsKey(trigger)) {
            commandList.get(trigger);
        }

        // Check for Custom Command
        // TODO: Implement Custom Commands

        // Check for Counter
        // TODO: Implement Counter
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
