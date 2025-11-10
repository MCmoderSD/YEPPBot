package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.objects.MessageEvent;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.MessageHelper.SPACE;
import static java.lang.String.format;

public class Quote extends CommandBuilder {

    // Constructor
    public Quote(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        String syntax = "Syntax: " + prefix + "quote <add|delete|edit|last> <number>";

        // About
        String[] name = {"quote", "qoute", "zitat", "gänsehosen"};
        String description = "Ist ein Zitat Feature. " + syntax;

        // Responses
        String noQuotesFound = "No quotes found!";
        String invalidQuoteID = "Invalid quote ID";
        String quoteDoesNotExist = "Quote does not exist!";
        String invalidArgs = "See Syntax: " + syntax;

        // Register command
        boolean registered = commandHandler.registerCommand(new Command(description, name) {

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
                    return twitchBot.sendMessage(event, name, format("@%s, #%d: %s", user.getDisplayName(), id + 1, quote));
                }

                // Quote ID
                if (args.size() == 1) {

                    var quoteId = getQuoteId(args.getFirst());
                    if (quoteId == null || quoteId < 0 || quoteId >= quotes.size()) return twitchBot.sendMessage(event, name, invalidQuoteID);

                    // Get Quote
                    var quote = quotes.get(quoteId);

                    // Send Quote
                    return twitchBot.sendMessage(event, name, format("@%s, #%d: %s", user.getDisplayName(), quoteId + 1, quote));
                }

                // Check Permissions
                if (!twitchBot.isPermitted(user, channel)) return false;

                // No args
                if (args.size() < 2) return twitchBot.sendMessage(event, name, invalidArgs);

                // Variables
                var action = args.getFirst().toLowerCase();
                var id = getQuoteId(args.get(1));
                String response;

                // Perform Action
                switch (action) {
                    case "add": {
                        String quote = String.join(SPACE, args.subList(1, args.size()));
                        quoteManager.addQuote(quote, channel);
                        response = String.format("Added quote #%d: %s", quotes.size() + 1, quote);
                        break;
                    }

                    case "rem":
                    case "del":
                    case "remove":
                    case "delete": {
                        if (id < 0 || id >= quotes.size()) response = quoteDoesNotExist;
                        else {
                            quoteManager.removeQuote(id, channel);
                            response = String.format("Removed quote #%d.", id + 1);
                        }
                        break;
                    }

                    case "edit":
                    case "change":
                    case "update": {
                        if (id < 0 || id >= quotes.size()) response = quoteDoesNotExist;
                        else {
                            String quote = String.join(SPACE, args.subList(2, args.size()));
                            quoteManager.editQuote(id, quote, channel);
                            response = String.format("Edited quote #%d: %s", id + 1, quote);
                        }
                        break;
                    }

                    default: {
                        response = invalidArgs;
                        break;
                    }
                }

                // Send Message
                return twitchBot.sendMessage(event, name, response);
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }

    public Integer getQuoteId(String arg) {
        if (arg.startsWith("#")) arg = arg.substring(1);
        try {
            return Integer.parseInt(arg) - 1;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}