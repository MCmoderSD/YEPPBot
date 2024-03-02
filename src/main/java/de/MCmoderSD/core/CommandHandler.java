package de.MCmoderSD.core;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.commands.Command;

import de.MCmoderSD.utilities.database.MySQL;

import java.util.HashMap;

import static de.MCmoderSD.utilities.Calculate.*;

public class CommandHandler {

    // Associations
    private final MySQL mySQL;

    // Attributes
    private final HashMap<String, Command> commands;
    private final HashMap<String, String> aliases;
    private final String prefix;

    // Constructor
    public CommandHandler(MySQL mySQL, String prefix) {

        // Init Associations and Attributes
        this.mySQL = mySQL;
        this.prefix = prefix;
        commands = new HashMap<>();
        aliases = new HashMap<>();
    }

    // Register a command
    public void registerCommand(Command command) {

        // Register command
        String name = command.getCommand().toLowerCase();
        commands.put(name, command);

        // Register aliases
        for (String alias : command.getAlias()) aliases.put(alias.toLowerCase(), name);
    }

    // Manually execute a command
    public void executeCommand(ChannelMessageEvent event, String command, String... args) {
        if (commands.containsKey(command) || aliases.containsKey(command)) {

            // Check for alias
            if (aliases.containsKey(command)) command = aliases.get(command);

            // Check for permission
            // ToDo Black and Whitelist

            // Execute command
            getCommand(command).execute(event, args);

            // Log command execution
            System.out.printf("%s%s %s <%s> Executed: %s%s%s", BOLD, logTimestamp(), COMMAND, getChannel(event), command + ": " + String.join(" ", args), BREAK, UNBOLD);
            mySQL.log(logDate(), logTime(), stripBrackets(COMMAND), getChannel(event), getAuthor(event), command + ": " + String.join(" ", args));
        }
    }

    public void handleCommand(ChannelMessageEvent event, String botName) {
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
    }

    // Setter and Getter
    public HashMap<String, Command> getCommands() {
        return commands;
    }

    public Command getCommand(String command) {
        return commands.get(command);
    }

    @SuppressWarnings("unused")
    public void removeCommand(String command) {
        commands.remove(command);
    }
}