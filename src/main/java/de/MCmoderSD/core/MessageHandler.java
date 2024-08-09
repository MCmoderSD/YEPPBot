package de.MCmoderSD.core;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.objects.Command;
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
    private final HashMap<Integer, ArrayList<String>> blackList;
    private final HashMap<String, Command> commandList;
    private final HashMap<String, String> aliasList;
    private final ArrayList<Integer> lurkList;


    public MessageHandler(BotClient botClient, MySQL mySQL, Frame frame) {

        // Initialize Associations
        this.botClient = botClient;
        this.mySQL = mySQL;
        this.frame = frame;

        // Initialize Attributes
        blackList = new HashMap<>();
        commandList = new HashMap<>();
        aliasList = new HashMap<>();
        lurkList = new ArrayList<>();

        // Update Black List
        updateBlackList();
    }

    public void addCommand(Command command) {

        // Register command
        String name = command.getCommand().toLowerCase();
        commandList.put(name, command);

        // Register aliases
        for (String alias : command.getAlias()) aliasList.put(alias.toLowerCase(), name);
    }

    public void updateBlackList() {
        blackList.clear();
        blackList.putAll(mySQL.getBlackList());
    }

    public void handleMessage(TwitchMessageEvent event) {
        new Thread(() -> {

            // Log Message
            mySQL.logMessage(event);
            event.logToConsole();
            if (!botClient.hasArg("cli")) frame.log(event.getType(), event.getChannel(), event.getUser(), event.getMessage());

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
                botClient.respond(event, "@" + botClient.getBotName(), tagUser(event) + " YEPP");
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
            trigger = aliasList.get(trigger);
            parts.set(0, trigger);
        }

        // Check for Command
        if (commandList.containsKey(trigger)) {
            if (isBlackListed(event, trigger)) return;
            Command command = commandList.get(trigger);
            parts.removeFirst();

            // Log Command
            mySQL.logCommand(event, trigger, processArgs(parts));

            // Execute Command
            command.execute(event, parts);
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
        if (message.indexOf(botClient.getPrefix()) == 0) message = message.substring(1);
        else message = message.substring(message.indexOf(" " + botClient.getPrefix()) + 2);

        // Split Command
        String[] split = trimMessage(message).split(" ");
        return new ArrayList<>(Arrays.asList(split));
    }

    private boolean isBlackListed(TwitchMessageEvent event, String command) {
        if (!blackList.containsKey(event.getChannelId())) return false;
        else return blackList.get(event.getChannelId()).contains(command.toLowerCase());
    }
}