package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.database.manager.CustomManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Counter {

    // Constructor
    public Counter(BotClient botClient, MessageHandler messageHandler, MySQL mySQL) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "counter create/delete/reset/show/set/list <name> <value>";


        // About
        String[] name = {"counter", "zähler", "counters"};
        String description = "Erstellt einen Zähler, der mit jedem Aufruf um 1 erhöht wird. " + syntax;

        // Variables
        CustomManager customManager = mySQL.getCustomManager();

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Variables
                String response;
                HashMap<String, Integer> counters = customManager.getCounters(event);

                if (!args.isEmpty() && args.getFirst().equalsIgnoreCase("list")) {
                    String list = counters.keySet().toString().substring(1, counters.keySet().toString().length() - 1);
                    botClient.respond(event, getCommand(), "Counters: " + (list.isEmpty() ? "none" : list) + ".");
                    return;
                }

                String verb = args.getFirst().toLowerCase();

                // Check syntax
                if (args.size() < 2) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                String counterName = args.get(1).toLowerCase();

                if (!Arrays.asList("create", "add", "delete", "remove", "del", "rm", "reset", "show", "set", "edit").contains(verb)) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                // Check action
                switch (verb) {
                    case "create":
                    case "add":
                        if (!(botClient.isAdmin(event) || botClient.isPermitted(event))) return;
                        if (messageHandler.checkCommand(counterName) || messageHandler.checkAlias(counterName) || customManager.getCommands(event, true).contains(counterName) || customManager.getAliases(event, true).containsKey(counterName) || counters.containsKey(counterName)) response = "Command, Alias or Counter with the same name already exits!";
                        else response = customManager.createCounter(event, counterName);
                        break;
                    case "delete":
                    case "remove":
                    case "del":
                    case "rm":
                        if (!(botClient.isAdmin(event) || botClient.isPermitted(event))) return;
                        response = customManager.deleteCounter(event, counterName);
                        break;
                    case "reset":
                        if (!(botClient.isAdmin(event) || botClient.isPermitted(event))) return;
                        response = customManager.editCounter(event, counterName, 0);
                        break;
                    case "show":
                        response = "Counter " + counterName + ": " + counters.get(counterName);
                        break;
                    case "set":
                    case "edit":
                        if (!(botClient.isAdmin(event) || botClient.isPermitted(event))) return;
                        if (args.size() > 2)
                            response = customManager.editCounter(event, counterName, Integer.parseInt(args.get(2)));
                        else response = "Syntax: " + syntax;
                        break;
                    default:
                        response = "Syntax: " + syntax;
                        break;
                }

                // Update counters
                messageHandler.updateCounters(customManager.getCustomCounters());

                // Send message
                botClient.respond(event, getCommand(), response);
            }
        });
    }
}