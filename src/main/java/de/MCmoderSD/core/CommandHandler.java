package de.MCmoderSD.core;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.commands.Command;

import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.json.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class CommandHandler {

    // Associations
    private final MySQL mySQL;

    // Constants
    private final JsonNode whiteList;
    private final JsonNode blackList;
    private final String prefix;

    // Attributes
    private final HashMap<String, Command> commands;
    private final HashMap<String, String> aliases;
    private final HashMap<Command, ArrayList<String>> whiteListMap;
    private final HashMap<Command, ArrayList<String>> blackListMap;

    // Constructor
    public CommandHandler(MySQL mySQL, JsonNode whiteList, JsonNode blackList, String prefix) {

        // Init Constants and Attributes
        this.mySQL = mySQL;
        this.prefix = prefix;
        this.whiteList = whiteList;
        this.blackList = blackList;
        commands = new HashMap<>();
        aliases = new HashMap<>();
        whiteListMap = new HashMap<>();
        blackListMap = new HashMap<>();
    }

    // Register a command
    public void registerCommand(Command command) {

        // Register command
        String name = command.getCommand().toLowerCase();
        commands.put(name, command);

        // Register aliases
        for (String alias : command.getAlias()) aliases.put(alias.toLowerCase(), name);

        // White and Blacklist
        if (whiteList.containsKey(name))
            whiteListMap.put(command, new ArrayList<>(Arrays.asList(whiteList.get(name.toLowerCase()).asText().toLowerCase().split("; "))));
        if (blackList.containsKey(name))
            blackListMap.put(command, new ArrayList<>(Arrays.asList(blackList.get(name.toLowerCase()).asText().toLowerCase().split("; "))));
    }

    // Manually execute a command
    public void executeCommand(ChannelMessageEvent event, String command, String... args) {
        if (commands.containsKey(command) || aliases.containsKey(command)) {

            // Check for alias
            if (aliases.containsKey(command)) command = aliases.get(command);

            // Get Command
            Command cmd = commands.get(command);

            // Check for WhiteList
            if (whiteListMap.containsKey(cmd) && !whiteListMap.get(cmd).contains(getChannel(event))) return;

            // Check for BlackList
            if (blackListMap.containsKey(cmd) && blackListMap.get(cmd).contains(getChannel(event))) return;

            // MySQL Log
            mySQL.logCommand(event, cmd.getCommand(), processArgs(args));

            // Console Log
            System.out.printf("%s%s %s <%s> Executed: %s%s%s", BOLD, logTimestamp(), COMMAND, getChannel(event), command + ": " + processArgs(args), BREAK, UNBOLD);

            // Execute command
            cmd.execute(event, args);
        }
    }

    public void handleCommand(ChannelMessageEvent event, String botName) {
        new Thread(() -> {

            // Get message
            String message = getMessage(event);

            // Check for prefix
            if (!message.startsWith(prefix) || getAuthor(event).equals(botName)) return;

            // Convert message to command and arguments
            String[] split = message.split(" ");
            String command = split[0].substring(1).toLowerCase();
            String[] args = new String[split.length - 1];
            System.arraycopy(split, 1, args, 0, split.length - 1);

            // Execute command
            executeCommand(event, command, args);
        }).start();
    }

    // Setter and Getter
    public HashMap<String, Command> getCommands() {
        return commands;
    }

    public HashMap<String, String> getAliases() {
        return aliases;
    }

    @SuppressWarnings("unused")
    public void removeCommand(String command) {
        commands.remove(command);
    }

    public String getPrefix() {
        return prefix;
    }
}