package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;

import de.MCmoderSD.utilities.database.MySQL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Counter {

    // Constructor
    public Counter(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat, ArrayList<String> admins) {

        // Syntax
        String syntax = "Syntax: " + commandHandler.getPrefix() + "counter create/delete/reset/show/set/list <name> <value>";

        // About
        String[] name = {"counter", "zähler", "counters"};
        String description = "Erstellt einen Zähler, der mit jedem Aufruf um 1 erhöht wird. " + syntax;


        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

                String channel = getChannel(event);
                String author = getAuthor(event);
                String response = syntax;

                HashMap<String, Integer> counters = mySQL.getCounters(event);

                if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
                    sendMessage(mySQL, chat, event, getCommand(), channel, "Counters: " + counters.keySet().toString().substring(1, counters.keySet().toString().length() - 1) + ".", args);
                    return;
                }

                String verb = args[0].toLowerCase();

                // Check syntax
                if (args.length < 2) {
                    sendMessage(mySQL, chat, event, getCommand(), channel, response, args);
                    return;
                }

                String counterName = args[1].toLowerCase();

                if (!Arrays.asList("create", "add", "delete", "remove", "del", "rm", "reset", "show", "set").contains(verb)) {
                    sendMessage(mySQL, chat, event, getCommand(), channel, response, args);
                    return;
                }

                if (commandHandler.getCommands().containsKey(counterName) && commandHandler.getAliases().containsKey(counterName) && mySQL.getCommands(event, true).contains(counterName) && mySQL.getAliases(event, true).containsKey(counterName) && counters.containsKey(counterName)) {
                    sendMessage(mySQL, chat, event, getCommand(), channel, response, args);
                    return;
                }

                // Check action
                switch (verb) {
                    case "create":
                    case "add":
                        if (!(admins.contains(author) || channel.equals(author))) return;
                        response = mySQL.createCounter(event, counterName);
                        break;
                    case "delete":
                    case "remove":
                    case "del":
                    case "rm":
                        if (!(admins.contains(author) || channel.equals(author))) return;
                        response = mySQL.deleteCounter(event, counterName);
                        break;
                    case "reset":
                        if (!(admins.contains(author) || channel.equals(author))) return;
                        response = mySQL.editCounter(event, counterName, 0);
                        break;
                    case "show":
                        response = "Counter " + counterName + ": " + counters.get(counterName);
                        break;
                    case "set":
                        if (!(admins.contains(author) || channel.equals(author))) return;
                        if (args.length > 2)
                            response = mySQL.editCounter(event, counterName, Integer.parseInt(args[2]));
                        else response = "Syntax: " + syntax;
                        break;
                    default:
                        response = "Syntax: " + syntax;
                        break;
                }

                // Send message
                sendMessage(mySQL, chat, event, getCommand(), channel, response, args);
            }
        });
    }

    // Send Message
    private void sendMessage(MySQL mySQL, TwitchChat chat, ChannelMessageEvent event, String command, String channel, String response, String... args) {

        // Send Message
        chat.sendMessage(channel, response);

        // Log response
        mySQL.logResponse(event, command, processArgs(args), response);
    }
}