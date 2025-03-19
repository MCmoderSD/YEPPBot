package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.enums.Account;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.SQL;
import de.MCmoderSD.utilities.database.manager.ChannelManager;

import java.util.ArrayList;
import java.util.Arrays;

import static de.MCmoderSD.utilities.other.Format.*;

@SuppressWarnings("ALL")
public class Rank {
    // ToDo: Currently Disabled, rework needed
/*
    // Constructor
    public Rank(BotClient botClient, MessageHandler messageHandler, SQL sql) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "rank <valo|lol|siege|apex> (set) <rank>";

        // About
        String[] name = {"rank", "rang"};
        String description = "Zeigt den Rank des Streamers an:" + syntax;

        // Init Associations
        ChannelManager channelManager = sql.getChannelManager();

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Check args
                if (args.isEmpty()) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                // Set Rank
                if (args.size() > 1 && Arrays.asList("set", "update").contains(args.get(1).toLowerCase())) {

                    // Check if user is moderator or admin
                    if (!(botClient.isAdmin(event) || botClient.isPermitted(event))) return;

                    // Update Rank
                    Account account = Account.getAccount(args.getFirst());
                    if (account == null) botClient.respond(event, getCommand(), syntax);
                    else {
                        String rank = String.join(SPACE, args.subList(2, args.size()));
                        boolean success = channelManager.setAccountValue(event.getChannelId(), account, rank);
                        if (success) botClient.respond(event, getCommand(), account.getName() + " Rank updated to " + rank);
                        else botClient.respond(event, getCommand(), account.getName() + " Rank could not be updated!");
                        return;
                    }
                }

                // Get Account
                Account account = Account.getAccount(String.join(SPACE, args).toLowerCase());
                if (account == null) botClient.respond(event, getCommand(), syntax);
                else {
                    String rank = channelManager.getAccountValue(event.getChannelId(), account);
                    if (rank == null) botClient.respond(event, getCommand(), "No rank set for " + account.getName());
                    else botClient.respond(event, getCommand(), account.getName() + " Rank: " + rank);
                }
            }
        });
    }*/
}