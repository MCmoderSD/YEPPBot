package de.MCmoderSD.commands;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.objects.TwitchUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Info {

    // Constructor
    public Info(BotClient botClient, MessageHandler messageHandler, HelixHandler helixHandler) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "info <editor/mod/vip>";

        // About
        String[] name = {"info", "information"};
        String description = "Zeigt dir infos über einen Channel. " + syntax;


        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Check syntax
                if (args.isEmpty()) {
                    botClient.respond(event, getCommand(), "Syntax: " + botClient.getPrefix() + "info <editor/mod/vip>");
                    return;
                }

                String option = args.getFirst().toLowerCase();

                if (!Arrays.asList("editor", "mod", "vip", "moderator", "editors", "mods", "vips").contains(option)) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                switch (option) {
                    case "editor", "editors" -> {
                        StringBuilder editors = new StringBuilder("Editoren: ");
                        HashSet<TwitchUser> editorList = helixHandler.getEditors(event.getChannelId());

                        if (editorList == null || editorList.isEmpty()) {
                            botClient.respond(event, getCommand(), "Es gibt keine Editoren in diesem Channel.");
                            return;
                        }

                        // Get editors
                        for (TwitchUser editor : editorList) editors.append(editor.getName()).append(", ");
                        botClient.respond(event, getCommand(), editors.substring(0, editors.length() - 2));
                    }
                    case "mod", "mods", "moderator" -> {
                        StringBuilder mods = new StringBuilder("Moderatoren: ");
                        HashSet<TwitchUser> modList = helixHandler.getModerators(event.getChannelId());

                        if (modList == null || modList.isEmpty()) {
                            botClient.respond(event, getCommand(), "Es gibt keine Moderatoren in diesem Channel.");
                            return;
                        }

                        // Get moderators
                        for (TwitchUser mod : modList) mods.append(mod.getName()).append(", ");
                        botClient.respond(event, getCommand(), mods.substring(0, mods.length() - 2));
                    }
                    case "vip", "vips" -> {
                        StringBuilder vips = new StringBuilder("VIPs: ");
                        HashSet<TwitchUser> vipList = helixHandler.getVIPs(event.getChannelId());

                        if (vipList == null || vipList.isEmpty()) {
                            botClient.respond(event, getCommand(), "Es gibt keine VIPs in diesem Channel.");
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