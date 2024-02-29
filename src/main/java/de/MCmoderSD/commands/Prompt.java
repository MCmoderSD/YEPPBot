package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.core.CommandHandler;
import de.MCmoderSD.utilities.json.JsonNode;
import de.MCmoderSD.utilities.json.JsonUtility;

public class Prompt {

    // Constructor
    public Prompt(CommandHandler commandHandler, TwitchChat chat) {

        // ToDo - Connect to GPT
        JsonUtility jsonUtility = new JsonUtility();
        JsonNode config = jsonUtility.load("/api/ChatGPT.json");

        // Register command
        commandHandler.registerCommand(new Command("prompt", "gpt") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {

            }
        });
    }
}