package de.MCmoderSD.handlers;

import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.database.Database;
import de.MCmoderSD.database.manager.ChannelManager;
import de.MCmoderSD.database.manager.CommandManager;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static de.MCmoderSD.utilities.MessageHelper.*;

public class CommandHandler {

    // Database
    private final ChannelManager channelManager;
    private final CommandManager commandManager;

    // Attributes
    private final ArrayList<String> prefixes;
    private final HashMap<String, Command> commandMap;
    private final HashMap<String, String> aliasMap;
    private final HashMap<TwitchUser, HashSet<String>> blacklist;

    // Constructor
    public CommandHandler(TwitchBot twitchBot) {

        // Check Parameters
        if (twitchBot == null) throw new IllegalArgumentException("TwitchBot cannot be null");

        // Set Database
        Database database = twitchBot.getDatabase();
        channelManager = database.getChannelManager();
        commandManager = database.getCommandManager();

        // Initialize Attributes
        prefixes = twitchBot.getPrefixes();
        commandMap = new HashMap<>();
        aliasMap = new HashMap<>();
        blacklist = database.getChannelManager().getBlacklist();
    }

    // Update Blacklist
    private void updateBlacklist(HashMap<TwitchUser, HashSet<String>> blacklist) {

        // Check Parameters
        if (blacklist == null) throw new IllegalArgumentException("Blacklist cannot be null");

        // Update Blacklist
        this.blacklist.clear();
        this.blacklist.putAll(blacklist);
    }

    // Format Command
    private ArrayList<String> formatCommand(MessageEvent event) {

        // Variables
        String message = event.getMessage();

        // Find Prefix
        String prefix = null;
        for (var p : prefixes) if (message.startsWith(p) || message.contains(SPACE + p)) prefix = p;
        if (prefix == null) throw new IllegalStateException("Prefix not found in message: " + message);

        // Find Start
        if (message.indexOf(prefix) == 0) message = message.substring(1);       // Remove Prefix
        else message = message.substring(message.indexOf(SPACE + prefix) + 2);  // Remove Space and Prefix

        // Split Arguments
        return new ArrayList<>(Arrays.asList(normalizeMessage(message).split(SPACE)));
    }

    // Check Blacklist
    private boolean isBlacklisted(MessageEvent event, String command) {
        if (!blacklist.containsKey(event.getChannel())) return false;
        return blacklist.get(event.getChannel()).contains(command);
    }

    // Check if Command is Blacklisted
    private boolean isBlacklisted(TwitchUser channel, String command) {

        // Check Parameters
        if (channel == null) throw new IllegalArgumentException("Channel cannot be null");
        if (command == null || command.isBlank()) throw new IllegalArgumentException("Command cannot be null or blank");

        // Normalize Command
        command = command.toLowerCase();

        // Convert Alias to Command
        if (aliasMap.containsKey(command)) command = aliasMap.get(command);

        // Check Blacklist
        if (!blacklist.containsKey(channel)) return false;
        return blacklist.get(channel).contains(command);
    }

    // Handle Command
    public boolean handleCommand(MessageEvent event) {

        // Check Parameters
        if (event == null) throw new IllegalArgumentException("MessageEvent cannot be null");

        // Variables
        ArrayList<String> parts = formatCommand(event);
        String trigger = parts.getFirst().toLowerCase();

        // Check for Alias
        if (aliasMap.containsKey(trigger)) {
            trigger = aliasMap.get(trigger);
            parts.set(0, trigger);
        }

        // Check for Command
        if (commandMap.containsKey(trigger)) {

            // Check Blacklist
            if (isBlacklisted(event, trigger)) return false;

            // Get Command
            Command command = commandMap.get(trigger);
            parts.removeFirst();

            // Execute Command
            boolean success = command.execute(event, parts);

            // Log Command
            if (success) {
                System.out.printf("%s <%s> #%s executed command: %s%n", COMMAND, event.getChannel().getDisplayName(), event.getUser().getDisplayName(), trigger);
                commandManager.logCommand(event, trigger, parts);
            }

            // Return
            return success;
        }

        // Command not found
        return false;
    }

    // Register Command
    @SuppressWarnings("SameReturnValue")
    public boolean registerCommand(Command command) {

        // Check Parameters
        if (command == null) throw new IllegalArgumentException("Command cannot be null");
        
        // Variables
        var name = command.getName().toLowerCase();
        
        // Check if Command already exists
        if (commandMap.containsKey(name)) throw new IllegalArgumentException("Command " + command.getName() + " is already registered");
        for (var alias : command.getAliases()) if (aliasMap.containsKey(alias.toLowerCase())) throw new IllegalArgumentException("Alias " + alias + " is already registered");
        
        // Register Command
        commandMap.put(name, command);
        for (var alias : command.getAliases()) aliasMap.put(alias.toLowerCase(), name);

        // Return
        return true;
    }

    // Check if Command is Blacklisted
    public boolean blacklistAdd(TwitchUser channel, String command) {

        // Check Parameters
        if (channel == null) throw new IllegalArgumentException("Channel cannot be null");
        if (command == null || command.isBlank()) throw new IllegalArgumentException("Command cannot be null or blank");

        // Normalize Command
        command = command.toLowerCase();

        // Convert Alias to Command
        if (aliasMap.containsKey(command)) command = aliasMap.get(command);

        // Check if command exists
        if (!commandMap.containsKey(command)) return false;

        // Check if already blacklisted
        if (isBlacklisted(channel, command)) return false;

        // Update Blacklist
        updateBlacklist(channelManager.blacklistAdd(channel, command));
        return true;
    }

    // Check if Command is Blacklisted
    public boolean blacklistRemove(TwitchUser channel, String command) {

        // Check Parameters
        if (channel == null) throw new IllegalArgumentException("Channel cannot be null");
        if (command == null || command.isBlank()) throw new IllegalArgumentException("Command cannot be null or blank");

        // Normalize Command
        command = command.toLowerCase();

        // Convert Alias to Command
        if (aliasMap.containsKey(command)) command = aliasMap.get(command);

        // Check if command exists
        if (!commandMap.containsKey(command)) return false;

        // Check if not blacklisted
        if (!isBlacklisted(channel, command)) return false;

        // Update Blacklist
        updateBlacklist(channelManager.blacklistRemove(channel, command));
        return true;
    }
}