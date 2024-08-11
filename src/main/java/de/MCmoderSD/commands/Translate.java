package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.other.OpenAi;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Translate {

    // Attributes
    private final double temperature;
    private final int maxTokens;
    private final double topP;
    private final double frequencyPenalty;
    private final double presencePenalty;

    // Constructor
    public Translate(BotClient botClient, MessageHandler messageHandler, OpenAi openAi) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "translate <Sprache> <Text>";

        // About
        String[] name = {"translator", "translate", "übersetzer", "übersetze"};
        String description = "Kann deine Sätze in jede erdenkliche Sprache übersetzen. " + syntax;

        // Load Config
        JsonNode config = openAi.getConfig();
        temperature = 0;
        maxTokens = config.get("maxTokens").asInt();
        topP = config.get("topP").asDouble();
        frequencyPenalty = config.get("frequencyPenalty").asDouble();
        presencePenalty = config.get("presencePenalty").asDouble();

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                String response;
                if (args.size() < 2) response = syntax;
                else {

                    // Check for language
                    String language = args.getFirst();

                    // Process text
                    String text = trimMessage(processArgs(args)).replace(language, "");
                    String instruction = trimMessage("Please translate the following text into " + language + ":");

                    // Translate
                    response = openAi.prompt(botClient.getBotName(), instruction, text, temperature, maxTokens, topP, frequencyPenalty, presencePenalty);
                }

                // Send Message
                botClient.respond(event, getCommand(), response);
            }
        });
    }
}