package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.SQL;
import de.MCmoderSD.utilities.database.manager.CustomManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Format.EMPTY;
import static de.MCmoderSD.utilities.other.Format.cleanArgs;

public class Help {

    // Associations
    private final BotClient botClient;
    private final MessageHandler messageHandler;
    private final CustomManager customManager;

    // Constructor
    public Help(BotClient botClient, MessageHandler messageHandler, SQL sql) {

        // Init Associations
        this.botClient = botClient;
        this.messageHandler = messageHandler;
        this.customManager = sql.getCustomManager();

        // Syntax
        String prefix = botClient.getPrefix();
        String syntax = prefix + "help commands oder " + prefix + "help <Befehl>";

        // About
        String[] name = {"help", "hilfe"};
        String description = "Um die verfügbaren Befehle zu sehen, schreibe: " + syntax;


        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                // Variables
                String arg = !args.isEmpty() ? args.getFirst().toLowerCase() : EMPTY;

                // Help Commands
                String response = null;
                if (Arrays.asList("commands", "command", "befehle", "befehl", "comand", "comands").contains(arg))
                    response = helpCommands(event); // Help Commands
                else if (getCommandDescription(arg) != null)
                    response = getCommandDescription(arg); // Command Description
                if (response == null) response = getDescription(); // Default Description

                // Send message
                botClient.respond(event, getCommand(), response);
            }
        });
    }

    // Gets all available commands
    private String helpCommands(TwitchMessageEvent event) {

        // Variables
        StringBuilder message = new StringBuilder("Available commands: ");
        String prefix = botClient.getPrefix();

        // Get commands
        HashMap<String, Command> commands = messageHandler.getCommandList();

        // Filter available commands
        for (String command : commands.keySet())
            if (!messageHandler.isBlackListed(event, command.toLowerCase()))
                message.append(prefix).append(command).append(", ");

        // Add custom commands
        for (String command : customManager.getCommands(event, false))
            message.append(prefix).append(command).append(", ");

        // Check how many commands are available
        if (message.length() < 22) message.append("none. ");

        // Return message
        return message.substring(0, message.length() - 2) + '.';
    }

    // Gets the description of a command
    private String getCommandDescription(String command) {

        // Get command
        if (messageHandler.checkAlias(command)) command = messageHandler.getAliasMap().get(command);

        // Get description if command is available
        if (messageHandler.checkCommand(command.toLowerCase()))
            return messageHandler.getCommandList().get(command).getDescription();
        else return null;
    }
}