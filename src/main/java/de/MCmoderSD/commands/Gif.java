package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.giphy.Giphy;
import de.MCmoderSD.objects.TwitchMessageEvent;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Format.*;

public class Gif {

    // Constructor
    public Gif(BotClient botClient, MessageHandler messageHandler, JsonNode apiConfig) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "gif <Thema>";

        // About
        String[] name = {"gif", "giphy", "gify"};
        String description = "Sendet ein GIF zu einem bestimmten Thema. " + syntax;

        // Initialize Giphy API
        Giphy giphy = new Giphy(apiConfig.get("giphy").asText());

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                // Check arguments
                String topic = trimMessage(convertToAscii(processArgs(args)));

                // Query Giphy and send response
                try {
                    botClient.respond(event, getCommand(), trimMessage("Look: " + giphy.queryRandom(topic)[0].getMediaSource()));
                } catch (Exception e) {
                    botClient.respond(event, getCommand(), "Es wurde kein GIF gefunden.");
                }
            }
        });
    }
}