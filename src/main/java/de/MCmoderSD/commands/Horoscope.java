package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.Birthdate;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.openAI.OpenAI;
import de.MCmoderSD.utilities.openAI.modules.Chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static de.MCmoderSD.utilities.other.Calculate.cleanArgs;

public class Horoscope {

    // Constructor
    public Horoscope(BotClient botClient, MessageHandler messageHandler, MySQL mySQL, OpenAI openAI) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "horoscope get @user";

        // About
        String[] name = {"horoscope", "horoskop", "horoskope"};
        String description = "Zeigt dein Horoskop an. " + syntax;

        // Get Chat Module and Config
        Chat chat = openAI.getChat();
        JsonNode config = chat.getConfig();

        // Get Parameters
        String instruction = "Gib mir das heutige Horoskop für das folgende Sternzeichen.";
        double temperature = config.get("temperature").asDouble();
        int maxTokens = config.get("maxTokens").asInt();
        double topP = config.get("topP").asDouble();
        double frequencyPenalty = config.get("frequencyPenalty").asDouble();
        double presencePenalty = config.get("presencePenalty").asDouble();


        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                HashMap<Integer, Birthdate> birthdayList = mySQL.getBirthdays();

                // Check Arguments
                if (args.isEmpty()) {
                    Birthdate birthdate = birthdayList.get(event.getUserId());
                    birthdate.getZodiacSign();
                    botClient.respond(event, getCommand(), openAI.getChat().prompt(botClient.getBotName(), instruction, birthdate.getZodiacSign(), temperature, maxTokens, topP, frequencyPenalty, presencePenalty));
                }

                // Send Message

            }
        });
    }
}