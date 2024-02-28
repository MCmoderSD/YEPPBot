package de.MCmoderSD.core;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.commands.Command;

import java.util.HashMap;

import static de.MCmoderSD.utilities.Calculate.*;

@SuppressWarnings("ALL")
public class CommandHandler {

    // Attributes
    private final HashMap<String, Command> commands;
    private final HashMap<String, String> aliases;
    private final String prefix;

    // Constructor
    public CommandHandler(String prefix) {
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

            // Execute command
            getCommand(command).execute(event, args);

            // Log command execution
            System.out.printf("%s%s %s <%s> Executed: %s%s%s", BOLD, logTimestamp(), COMMAND, getChannel(event), command, BREAK, UNBOLD);
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

    public void removeCommand(String command) {
        commands.remove(command);
    }
}
