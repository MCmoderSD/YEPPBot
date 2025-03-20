package de.MCmoderSD.commands;

@SuppressWarnings("ALL")
public class Social {
    // ToDo: Currently Disabled, rework needed
/*
    // Constructor
    public Social(BotClient botClient, MessageHandler messageHandler, SQL sql) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "social <instagram|tiktok|twitter|youtube> (set) <link>";

        // About
        String[] name = {"social", "socials", "socialmedia"};
        String description = "Zeigt die Social Media Links des Streamers an:" + syntax;

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

                    // Update Social Media Link
                    Account account = Account.getAccount(args.getFirst());
                    if (account == null) botClient.respond(event, getCommand(), syntax);
                    else {
                        String link = String.join(SPACE, args.subList(2, args.size()));
                        boolean success = channelManager.setAccountValue(event.getChannelId(), account, link);
                        if (success) botClient.respond(event, getCommand(), account.getName() + " Link updated to " + link);
                        else botClient.respond(event, getCommand(), account.getName() + " Link could not be updated!");
                        return;
                    }
                }

                // Get Account
                Account account = Account.getAccount(String.join(SPACE, args).toLowerCase());
                if (account == null) botClient.respond(event, getCommand(), syntax);
                else {
                    String link = channelManager.getAccountValue(event.getChannelId(), account);
                    if (link == null) botClient.respond(event, getCommand(), account.getName() + " Link not set!");
                    else botClient.respond(event, getCommand(), account.getName() + ": " + link);
                }
            }
        });
    }*/
}