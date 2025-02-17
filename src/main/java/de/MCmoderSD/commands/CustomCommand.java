package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.database.manager.CustomManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import static de.MCmoderSD.utilities.other.Format.*;

public class CustomCommand {

    // Constructor
    public CustomCommand(BotClient botClient, MessageHandler messageHandler, MySQL mySQL) {

        // Variables
        CustomManager customManager = mySQL.getCustomManager();
        String prefix = botClient.getPrefix();
        String syntax = prefix + "CustomCommand create/enable/disable/delete/list name/alias : response";

        // About
        String[] name = {"customcommand", "cc"};
        String description = "Kann benutzt werden um eigene Commands zu erstellen. Syntax: " + syntax;


        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Check if user is permitted
                if (!(botClient.isPermitted(event) || botClient.isAdmin(event))) return;

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                // Check for list
                if (!args.isEmpty() && args.getFirst().equalsIgnoreCase("list") || args.getFirst().equalsIgnoreCase("show")) {
                    botClient.respond(event, getCommand(), getCommandNames(event, customManager));
                    return;
                }

                // Get command syntax
                HashMap<String, Integer> message = new HashMap<>();
                for (var i = 1; i < args.size(); i++) message.put(args.get(i), i);

                String response = null;
                if (args.size() < 2) response = "Syntax: " + syntax;
                else if (Arrays.asList("create", "add", "enable", "disable", "delete", "del", "remove", "rm").contains(args.getFirst().toLowerCase())){

                    // Process command
                    String commandName = trimMessage(args.get(1).toLowerCase());
                    commandName = commandName.startsWith(prefix) ? commandName.substring(prefix.length()) : commandName;

                    // Process message
                    switch (args.getFirst().toLowerCase()) {

                        // Enable command
                        case "enable":
                            response = customManager.editCommand(event, commandName, true);
                            break;

                        // Disable command
                        case "disable":
                            response = customManager.editCommand(event, commandName, false);
                            break;

                        // Delete command
                        case "delete":
                        case "del":
                        case "remove":
                        case "rm":
                            response = customManager.deleteCommand(event, commandName);
                            break;

                        // Create command
                        case "create":
                        case "add":
                            if (!message.containsKey(":")) response = "Syntax: " + syntax;
                            else if (customManager.getCommands(event, true).contains(commandName) || customManager.getAliases(event, true).containsKey(commandName) || messageHandler.checkCommand(commandName) || messageHandler.checkAlias(commandName)) response = "Command or Alias already exists";
                            else {

                                // Get command aliases;
                                ArrayList<String> aliases = new ArrayList<>(args.subList(2, message.get(":")));
                                aliases.replaceAll(String::toLowerCase);

                                // Check Aliases
                                for (String alias : aliases) if (customManager.getCommands(event, true).contains(alias) || customManager.getAliases(event, true).containsKey(alias) || messageHandler.checkCommand(alias) || messageHandler.checkAlias(alias)) {
                                    response = "Command or Alias already exists";
                                    break;
                                }

                                if (Objects.equals(response, "Command or Alias already exists")) break;

                                // Get command response
                                StringBuilder stringBuilder = new StringBuilder();
                                for (var i = message.get(":") + 1; i < args.size(); i++) stringBuilder.append(args.get(i)).append(SPACE);
                                String commandResponse = trimMessage(stringBuilder.toString());

                                // Create command
                                response = customManager.createCommand(event, commandName, aliases, commandResponse);
                            }
                            break;

                        // Error
                        default:
                            response = "Syntax: " + syntax;
                            break;
                    }
                } else response = "Syntax: " + syntax;

                // Update custom commands
                messageHandler.updateCustomCommands(customManager.getCustomCommands(), customManager.getCustomAliases());

                // Send message
                botClient.respond(event, getCommand(), response);
            }
        });
    }

    private String getCommandNames(TwitchMessageEvent event, CustomManager customManager) {
        StringBuilder stringBuilder = new StringBuilder("Commands: ");
        ArrayList<String> enabledCommands = customManager.getCommands(event, false);
        ArrayList<String> disabledCommands = customManager.getCommands(event, true);

        for (String command : enabledCommands) stringBuilder.append(command).append(" enabled, ");
        for (String command : disabledCommands) if (!enabledCommands.contains(command)) stringBuilder.append(command).append(" disabled, ");

        if (enabledCommands.size() + disabledCommands.size() == 0) stringBuilder.append("none. ");

        return stringBuilder.substring(0, stringBuilder.length() - 2) + ".";
    }
}