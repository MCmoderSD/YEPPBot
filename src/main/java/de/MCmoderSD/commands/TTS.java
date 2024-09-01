package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.other.OpenAi;

import java.util.ArrayList;

public class TTS {

    // Constructor
    public TTS(BotClient botClient, MessageHandler messageHandler, OpenAi openAi) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "prompt <Frage>";

        // About
        String[] name = {"tts", "texttospeech"}; // Command name and aliases
        String description = "Lässt den YEPPBot Sprechen. " + syntax;

        // Set Attributes
        JsonNode config = openAi.getConfig();
        OpenAi.TTSModel model = openAi.getTtsModel();
        String voice = config.get("voice").asText();
        String format = config.get("format").asText();
        double speed = config.get("speed").asDouble();

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Check Admin
                if (!botClient.isAdmin(event)) return;

                // Get Voice if available
                String currentVoice = voice;
                for (String arg : args) if (model.checkVoice(arg.toLowerCase())) currentVoice = arg;

                // Send Message
                botClient.sendAudio(event, openAi.tts(event.getMessage(), currentVoice, format, speed));
            }
        });
    }
}