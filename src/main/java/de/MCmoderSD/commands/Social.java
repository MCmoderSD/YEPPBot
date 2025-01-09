package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.enums.Account;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.database.manager.ChannelManager;

import java.util.ArrayList;
import java.util.Arrays;

public class Social {

    // Constructor
    public Social(BotClient botClient, MessageHandler messageHandler, MySQL mySQL) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "social <instagram|tiktok|twitter|youtube> (set) <link>";

        // About
        String[] name = {"social", "socials", "socialmedia"};
        String description = "Zeigt die Social Media Links des Streamers an:" + syntax;

        // Init Associations
        ChannelManager channelManager = mySQL.getChannelManager();

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

                    // Update Social Media Link
                    Account account = Account.getAccount(args.getFirst());
                    if (account == null) botClient.respond(event, getCommand(), syntax);
                    else {
                        String link = String.join(" ", args.subList(2, args.size()));
                        boolean success = channelManager.setAccountValue(event.getChannelId(), account, link);
                        if (success) botClient.respond(event, getCommand(), account.getName() + " Link updated to " + link);
                        else botClient.respond(event, getCommand(), account.getName() + " Link could not be updated!");
                        return;
                    }
                }

                // Get Account
                Account account = Account.getAccount(String.join(" ", args).toLowerCase());
                if (account == null) botClient.respond(event, getCommand(), syntax);
                else {
                    String link = channelManager.getAccountValue(event.getChannelId(), account);
                    if (link == null) botClient.respond(event, getCommand(), account.getName() + " Link not set!");
                    else botClient.respond(event, getCommand(), account.getName() + ": " + link);
                }
            }
        });
    }
}