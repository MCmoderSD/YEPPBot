package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.openai.core.OpenAI;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Format.*;

public class Translate {

    // Constructor
    public Translate(BotClient botClient, MessageHandler messageHandler, OpenAI openAI) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "translate <Sprache> <Text>";

        // About
        String[] name = {"translator", "translate", "übersetzer", "übersetze"};
        String description = "Kann deine Sätze in jede erdenkliche Sprache übersetzen. " + syntax;

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Clean Args
                ArrayList<String> cleanArgs = cleanArgs(args);
                args.clear();
                args.addAll(cleanArgs);

                String response;
                if (args.size() < 2) response = syntax;
                else {

                    // Check for language
                    String language = args.getFirst();

                    // Process text
                    String text = trimMessage(concatArgs(args)).replace(language, EMPTY);
                    String devMessage = trimMessage("Please translate the following text into " + language + ":");

                    // Translate
                    response = openAI.prompt(
                            null,
                            event.getUser(),
                            null,
                            0d,
                            null,
                            null,
                            null,
                            null,
                            devMessage,
                            text,
                            null
                    );

                    // Filter Response for argument injection
                    response = removePrefix(response);
                }

                // Send Message
                botClient.respond(event, getCommand(), response);
            }
        });
    }
}