package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.SQL;
import de.MCmoderSD.JavaAudioLibrary.AudioFile;
import de.MCmoderSD.OpenAI.modules.Speech;

import java.util.ArrayList;

public class TTS {

    // Constants
    private final String ttsWasSent;

    // Constructor
    public TTS(BotClient botClient, MessageHandler messageHandler, SQL sql, Speech speech) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "prompt <Frage>";

        // About
        String[] name = {"tts", "texttospeech"}; // Command name and aliases
        String description = "Lässt den YEPPBot Sprechen. " + syntax;

        // Constants
        ttsWasSent = "TTS wurde gesendet YEPP";

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Check Admin
                if (!(botClient.isPermitted(event) || botClient.isAdmin(event))) return;

                // Get Voice if available
                String currentVoice = null;
                for (String arg : args) if (speech.getModel().checkVoice(arg.toLowerCase())) currentVoice = arg;

                String message = event.getMessage();

                AudioFile audioFile = sql.getAssetManager().getTTSAudio(message);
                if (audioFile == null) audioFile = speech.speak(event.getMessage(), currentVoice, null, null);

                // Send Audio
                botClient.sendAudio(event, audioFile);
                botClient.respond(event, getCommand(), ttsWasSent);
            }
        });
    }
}