package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.core.CommandHandler;
import de.MCmoderSD.utilities.json.JsonNode;

import java.util.Arrays;
import java.util.HashMap;

import static de.MCmoderSD.utilities.Calculate.getChannel;

public class Help {
    public Help(CommandHandler commandHandler, TwitchChat chat, JsonNode whitelist, JsonNode blacklist, String prefix) {

        // Register command
        commandHandler.registerCommand(new Command("help", "hilfe") { // Command name and aliases
            @Override
            public void execute(ChannelMessageEvent event, String... args) {
                String channel = getChannel(event);

                // Help Commands
                if (args[0].equalsIgnoreCase("commands") || args[0].equalsIgnoreCase("command") || args[0].equalsIgnoreCase("befehle") || args[0].equalsIgnoreCase("befehl")) {
                    StringBuilder message = new StringBuilder("Die verfügbaren Befehle sind: ");

                    HashMap<String, Command> commands = commandHandler.getCommands();

                    for (String command : commands.keySet()) {
                        if (whitelist.containsKey(command.toLowerCase())) {
                            if (Arrays.stream(whitelist.get(command.toLowerCase()).asText().toLowerCase().split("; ")).toList().contains(channel)) message.append(prefix).append(command).append(", ");
                        }
                        else if (blacklist.containsKey(command.toLowerCase())) {
                            if (!Arrays.stream(blacklist.get(command.toLowerCase()).asText().toLowerCase().split("; ")).toList().contains(channel)) message.append(prefix).append(command).append(", ");
                        }
                        else message.append(prefix).append(command).append(", ");
                    }

                    chat.sendMessage(channel, message.substring(0, message.length() - 2) + '.');
                }
            }
        });
    }
}
