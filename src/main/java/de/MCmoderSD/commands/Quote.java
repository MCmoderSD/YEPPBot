package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.core.TwitchBot;

import java.util.ArrayList;
import java.util.Arrays;

import static de.MCmoderSD.utilities.MessageHelper.*;
import static java.lang.String.format;

public class Quote extends CommandBuilder {

    // Constructor
    public Quote(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        var syntax = "Syntax: " + prefix + "Quote <add|delete|edit|last> <number>";

        // About
        var name = new String[]{"Quote", "Qoute", "Zitat", "Gänsehosen"};
        var description = "Verwaltet Zitate im Chat. " + syntax;

        // Responses
        var noQuotesFound = "No quotes found!";
        var invalidQuoteID = "Invalid quote ID";
        var quoteDoesNotExist = "Quote does not exist!";
        var invalidArgs = "See Syntax: " + syntax;

        // Register command
        var registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Variables
                var channel = event.getChannel();
                var user = event.getUser();
                var quotes = quoteManager.getQuotes(channel);
                var noArgs = args.isEmpty();
                var noQuotes = quotes.isEmpty();

                // No Args and No Quotes
                if (noArgs && noQuotes) return twitchBot.sendMessage(event, name, noQuotesFound);

                // No Args but Quotes Exist
                if (noArgs) {

                    // Get Random Quote
                    var id = (int) (Math.random() * quotes.size());
                    var quote = quotes.get(id);

                    // Send Quote
                    return twitchBot.sendMessage(event, name, format("%s, #%d: %s", tagUser(user), id + 1, quote));
                }

                // Action
                var action = args.getFirst().toLowerCase();
                var response = invalidArgs;

                // First or Last Quote
                if (Arrays.asList("first", "last").contains(action)) {
                    switch (action) {

                        // First Quote
                        case "first": {
                            if (noQuotes) response = noQuotesFound;
                            else {
                                var quote = quotes.get(0);
                                response = String.format("%s, #%d: %s", tagUser(user), 1, quote);
                            }
                            break;
                        }

                        // Last Quote
                        case "last": {
                            if (noQuotes) response = noQuotesFound;
                            else {
                                var lastId = quotes.size() - 1;
                                var quote = quotes.get(lastId);
                                response = String.format("%s, #%d: %s", tagUser(user), lastId + 1, quote);
                            }
                            break;
                        }
                    }

                    // Send Message
                    return twitchBot.sendMessage(event, name, response);
                }

                // Quote ID
                if (args.size() == 1) {

                    // Check Quotes
                    var quoteId = parseQuoteID(args.getFirst());
                    if (quoteId == null || quoteId < 0 || quoteId >= quotes.size()) return twitchBot.sendMessage(event, name, invalidQuoteID);

                    // Get Quote
                    var quote = quotes.get(quoteId);

                    // Send Quote
                    return twitchBot.sendMessage(event, name, format("%s, #%d: %s", tagUser(user), quoteId + 1, quote));
                }

                // Check Permissions
                if (!twitchBot.isPermitted(user, channel)) return false;

                // No args
                if (args.size() < 2) return twitchBot.sendMessage(event, name, invalidArgs);

                // Quote ID
                var id = parseQuoteID(args.get(1));

                // Perform Action
                switch (action) {

                    // Add Quote
                    case "add": {
                        var quote = String.join(SPACE, args.subList(1, args.size()));
                        quoteManager.addQuote(quote, channel);
                        response = String.format("Added quote #%d: %s", quotes.size() + 1, quote);
                        break;
                    }

                    // Remove Quote
                    case "rem":
                    case "del":
                    case "remove":
                    case "delete": {
                        if (id == null || id < 0 || id >= quotes.size()) response = quoteDoesNotExist;
                        else {
                            quoteManager.removeQuote(id, channel);
                            response = String.format("Removed quote #%d.", id + 1);
                        }
                        break;
                    }

                    // Edit Quote
                    case "edit":
                    case "change":
                    case "update": {
                        if (id == null || id < 0 || id >= quotes.size()) response = quoteDoesNotExist;
                        else {
                            var quote = String.join(SPACE, args.subList(2, args.size()));
                            quoteManager.editQuote(id, quote, channel);
                            response = String.format("Edited quote #%d: %s", id + 1, quote);
                        }
                        break;
                    }
                }

                // Send Message
                return twitchBot.sendMessage(event, name, response);
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }

    // Parse Quote ID
    private static Integer parseQuoteID(String arg) {
        while (arg.startsWith("#")) arg = arg.substring(1);
        try {
            return Integer.parseInt(arg) - 1;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}