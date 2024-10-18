package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.objects.TwitchUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static de.MCmoderSD.utilities.other.Calculate.cleanArgs;

public class Info {

    // Constants
    private final String thereAreNoEditors;
    private final String thereAreNoMods;
    private final String thereAreNoVIPs;
    private final String listEditors;
    private final String listMods;
    private final String listVips;

    // Constructor
    public Info(BotClient botClient, MessageHandler messageHandler, HelixHandler helixHandler) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "info <editor/mod/vip>";

        // About
        String[] name = {"info", "information"};
        String description = "Zeigt dir infos über einen Channel. " + syntax;

        // Constants
        thereAreNoEditors = "Es gibt keine Editoren in diesem Channel.";
        thereAreNoMods = "Es gibt keine Moderatoren in diesem Channel.";
        thereAreNoVIPs = "Es gibt keine VIPs in diesem Channel.";
        listEditors = "Editoren:";
        listMods = "Moderatoren:";
        listVips = "VIPs:";


        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                // Check syntax
                if (args.isEmpty()) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                String option = args.getFirst().toLowerCase();

                if (!Arrays.asList("editor", "mod", "vip", "moderator", "editors", "mods", "vips").contains(option)) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                switch (option) {
                    case "editor", "editors" -> {
                        StringBuilder editors = new StringBuilder(String.format("%s ", listEditors));
                        HashSet<TwitchUser> editorList = helixHandler.getEditors(event.getChannelId());

                        if (editorList == null || editorList.isEmpty()) {
                            botClient.respond(event, getCommand(), thereAreNoEditors);
                            return;
                        }

                        // Get editors
                        for (TwitchUser editor : editorList) editors.append(editor.getName()).append(", ");
                        botClient.respond(event, getCommand(), editors.substring(0, editors.length() - 2));
                    }
                    case "mod", "mods", "moderator" -> {
                        StringBuilder mods = new StringBuilder(String.format("%s ", listMods));
                        HashSet<TwitchUser> modList = helixHandler.getModerators(event.getChannelId(), null);

                        if (modList == null || modList.isEmpty()) {
                            botClient.respond(event, getCommand(), thereAreNoMods);
                            return;
                        }

                        // Get moderators
                        for (TwitchUser mod : modList) mods.append(mod.getName()).append(", ");
                        botClient.respond(event, getCommand(), mods.substring(0, mods.length() - 2));
                    }
                    case "vip", "vips" -> {
                        StringBuilder vips = new StringBuilder(String.format("%s ", listVips));
                        HashSet<TwitchUser> vipList = helixHandler.getVIPs(event.getChannelId(), null);

                        if (vipList == null || vipList.isEmpty()) {
                            botClient.respond(event, getCommand(), thereAreNoVIPs);
                            return;
                        }

                        // Get VIPs
                        for (TwitchUser vip : vipList) vips.append(vip.getName()).append(", ");
                        botClient.respond(event, getCommand(), vips.substring(0, vips.length() - 2));
                    }
                }
            }
        });
    }
}