package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.database.manager.QuoteManager;

import java.util.ArrayList;
import java.util.Arrays;


import static de.MCmoderSD.utilities.other.Format.*;

public class Quote {

    // Constants
    private final String listEmpty;
    private final String noPermission;
    private final String quoteNotExist;

    // Attributes
    private ArrayList<String> quotes;

    // Constructor
    public Quote(BotClient botClient, MessageHandler messageHandler, MySQL mySQL) {

        // Init Associations
        QuoteManager quoteManager = mySQL.getQuoteManager();

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "quote <add|delete|edit> <number>";

        // Constants
        listEmpty = "There are no quotes!";
        noPermission = "Insufficient Permissions!";
        quoteNotExist = "Quote does not exist!";

        // About
        String[] name = {"quote", "qoute", "zitat", "gänsehosen"};
        String description = "Ist ein Zitat Feature. " + syntax;


        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Variables
                quotes = quoteManager.getQuotes(event.getChannelId());
                String response = syntax;

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                // Check args
                if (args.isEmpty()) {
                    if (quotes.isEmpty()) botClient.respond(event, getCommand(), listEmpty);
                    else botClient.respond(event, getCommand(), getQuote(event, (int) (Math.random() * quotes.size())));
                    return;
                }

                // Check verb
                String verb = args.getFirst().toLowerCase();
                if (!Arrays.asList("add", "rem", "remove", "del", "delete", "edit", "change", "update").contains(verb)) {
                    if (args.getFirst().startsWith("#")) verb = verb.substring(1);
                    try {
                        var number = Integer.parseInt(verb) - 1;
                        botClient.respond(event, getCommand(), getQuote(event, number));
                        return;
                    } catch (NumberFormatException e) {
                        botClient.respond(event, getCommand(), response);
                        return;
                    }
                }

                // Check Args
                var number = 0;
                if (args.size() < 2 && !verb.equals("add")) {
                    botClient.respond(event, getCommand(), response);
                    return;
                }

                // Get Number
                if (!verb.equals("add")) {
                    try {
                        number = Integer.parseInt(args.get(1)) - 1;
                    } catch (NumberFormatException e) {
                        botClient.respond(event, getCommand(), response);
                        return;
                    }
                }

                // Check Permission
                if (!(botClient.isPermitted(event) || botClient.isAdmin(event))) {
                    botClient.respond(event, getCommand(), noPermission);
                    return;
                }

                // Response
                switch (verb) {
                    case "add":
                        response = quoteManager.addQuote(event.getChannelId(), String.join(" ", args.subList(1, args.size())));
                        break;
                    case "rem":
                    case "del":
                    case "remove":
                    case "delete":
                        if (number < 0 || number >= quotes.size()) response = quoteNotExist;
                        else response = quoteManager.removeQuote(event.getChannelId(), number);
                        break;
                    case "edit":
                    case "change":
                    case "update":
                        if (number < 0 || number >= quotes.size()) response = quoteNotExist;
                        else response = quoteManager.editQuote(event.getChannelId(), number, String.join(" ", args.subList(2, args.size())));
                        break;
                }

                // Send Message
                botClient.respond(event, getCommand(), response);
            }
        });
    }

    private String getQuote(TwitchMessageEvent event, int number) {
        if (number < 0 || number >= quotes.size()) return quoteNotExist;
        else return String.format("%s, #%d: %s", tagUser(event), number + 1, quotes.get(number));
    }
}