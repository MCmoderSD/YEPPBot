package de.MCmoderSD.commands;

@SuppressWarnings("ALL")
public class Whitelist {
    // ToDo: Currently Disabled, rework of YEPPConnect
/*
    // Constructor
    public Whitelist(BotClient botClient, MessageHandler messageHandler, SQL sql) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "whitelist join/kick <McUsername>";

        // About
        String[] name = {"whitelist"};
        String description = "Adde dich selbst auf die Minecraft Server Whitelist" + syntax;

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                // Check args
                if (args.isEmpty()) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                ArrayList<String> addVerbs = new ArrayList<>(Arrays.asList("join", "add"));
                ArrayList<String> removeVerbs = new ArrayList<>(Arrays.asList("kick", "remove", "delete"));
                ArrayList<String> allVerbs = new ArrayList<>();
                allVerbs.addAll(addVerbs);
                allVerbs.addAll(removeVerbs);

                String verb = args.getFirst().toLowerCase();
                if (allVerbs.contains(verb) && args.size() < 2) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                if (removeVerbs.contains(verb) && !(botClient.isPermitted(event) || botClient.isAdmin(event))) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                if (addVerbs.contains(verb)) botClient.respond(event, getCommand(), sql.getYEPPConnect().editWhitelist(event, args.get(1).toLowerCase(), true));
                else if (removeVerbs.contains(verb))botClient.respond(event, getCommand(), sql.getYEPPConnect().editWhitelist(event, args.get(1).toLowerCase(), false));
                else botClient.respond(event, getCommand(), syntax);
            }
        });
    }*/
}