package de.MCmoderSD.core;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.commands.Command;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.HashMap;
import java.util.NoSuchElementException;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class MessageHandler {

    // Associations
    private final BotClient botClient;
    private final MySQL mySQL;
    private final Frame frame;

    // Attributes
    private final HashMap<Integer, ArrayList<String>> blackList;
    private final HashMap<String, Command> commandList;
    private final HashMap<String, String> aliasList;
    private final HashMap<Integer, Integer> lurkList;


    public MessageHandler(BotClient botClient, MySQL mySQL, Frame frame) {

        // Initialize Associations
        this.botClient = botClient;
        this.mySQL = mySQL;
        this.frame = frame;

        // Initialize Attributes
        blackList = new HashMap<>();
        commandList = new HashMap<>();
        aliasList = new HashMap<>();
        lurkList = new HashMap<>();

        // Update Black List
        updateBlackList();
        updateLurkList(mySQL.getLurkManager().getLurkList());
    }

    public void addCommand(Command command) {

        // Register command
        String name = command.getCommand().toLowerCase();
        commandList.put(name, command);

        // Register aliases
        for (String alias : command.getAlias()) aliasList.put(alias.toLowerCase(), name);
    }

    public void updateBlackList() {
        blackList.clear();
        blackList.putAll(mySQL.getBlackListManager().getBlackList());
    }

    public void handleMessage(TwitchMessageEvent event) {
        new Thread(() -> {

            // Log Message
            mySQL.getLogManager().logMessage(event);
            event.logToConsole();
            if (!botClient.hasArg("cli"))
                frame.log(event.getType(), event.getChannel(), event.getUser(), event.getMessage());

            // Handle Timers;
            handleTimers(event);

            // Check for Lurk
            if (lurkList.containsKey(event.getUserId())) handleLurk(event);

            // Check for Command
            if (event.hasCommand()) {
                handleCommand(event);
                return;
            }

            // Reply YEPP
            if (event.hasBotName()) {
                botClient.respond(event, "replyYEPP", tagUser(event) + " YEPP");
                return;
            }

            // Say YEPP
            if (event.hasYEPP()) botClient.respond(event, "YEPP", "YEPP");
        }).start();
    }

    private void handleLurk(TwitchMessageEvent event) {
        new Thread(() -> {

            // Variables
            var channelID = event.getChannelId();
            var userID = event.getUserId();
            String response;

            HashMap<Timestamp, ArrayList<Integer>> lurker;
            Timestamp start;
            ArrayList<Integer> lurkChannel;

            try {
                lurker = mySQL.getLurkManager().getLurkTime(userID);
                start = lurker.keySet().iterator().next();
                lurkChannel = lurker.get(start);
            } catch (NoSuchElementException e) {
                return;
            }

            if (channelID == lurkChannel.getFirst()) { // Stop lurking

                // Remove user from lurk list
                updateLurkList(mySQL.getLurkManager().removeLurker(userID));

                // Send message
                response = tagUser(event) + " war " + formatLurkTime(start) + " im Lurk!";
                botClient.respond(event, "stoppedLurk", response);

            } else if (!lurkChannel.contains(channelID)) { // Snitch on lurked channel

                // Add user to traitor list
                lurkChannel.add(channelID);
                StringBuilder traitors = new StringBuilder();
                for (var i = 1; i < lurkChannel.size(); i++) traitors.append(lurkChannel.get(i)).append("\t");
                mySQL.getLurkManager().addTraitor(userID, traitors.toString());

                // Send message
                response = tagUser(event) + " ist ein verräter, hab den kek gerade im chat von " + tagChannel(event) + " gesehen!";
                botClient.respond(new TwitchMessageEvent(
                        event.getTimestamp(),
                        lurkChannel.getFirst(),
                        event.getUserId(),
                        mySQL.queryName("channels", lurkChannel.getFirst()),
                        event.getUser(),
                        event.getMessage(),
                        event.getSubMonths(),
                        event.getSubStreak(),
                        event.getSubTier(),
                        event.getBits()
                        ), "traitor", response);
            }
        }).start();
    }

    private void handleTimers(TwitchMessageEvent event) {
        new Thread(() -> {
            // TODO: Implement Timers
        }).start();
    }

    private void handleCommand(TwitchMessageEvent event) {

        // Variables
        ArrayList<String> parts = formatCommand(event);
        String trigger = parts.getFirst();

        // Check for Alias
        if (aliasList.containsKey(trigger)) {
            trigger = aliasList.get(trigger);
            parts.set(0, trigger);
        }

        // Check for Command
        if (commandList.containsKey(trigger)) {
            if (isBlackListed(event, trigger)) return;
            Command command = commandList.get(trigger);
            parts.removeFirst();

            // Log Command
            mySQL.getLogManager().logCommand(event, trigger, processArgs(parts));

            // Execute Command
            command.execute(event, parts);
        }

        // Check for Custom Command
        // TODO: Implement Custom Commands

        // Check for Counter
        // TODO: Implement Counter
    }

    private ArrayList<String> formatCommand(TwitchMessageEvent event) {

        // Variables
        String message = event.getMessage();

        // Find Start
        if (message.indexOf(botClient.getPrefix()) == 0) message = message.substring(1);
        else message = message.substring(message.indexOf(" " + botClient.getPrefix()) + 2);

        // Split Command
        String[] split = trimMessage(message).split(" ");
        return new ArrayList<>(Arrays.asList(split));
    }

    private String formatLurkTime(Timestamp startTime) {

        // Variables
        StringBuilder response = new StringBuilder();
        long time = getTimestamp().getTime() - startTime.getTime();

        // Years
        long years = time / 31536000000L;
        time %= 31536000000L;
        if (years > 1) response.append(years).append(" Jahre, ");
        else if (years > 0) response.append(years).append(" Jahr, ");

        // Months
        long months = time / 2592000000L;
        time %= 2592000000L;
        if (months > 1) response.append(months).append(" Monate, ");
        else if (months > 0) response.append(months).append(" Monat, ");

        // Weeks
        long weeks = time / 604800000L;
        time %= 604800000L;
        if (weeks > 1) response.append(weeks).append(" Wochen, ");
        else if (weeks > 0) response.append(weeks).append(" Woche, ");

        // Days
        long days = time / 86400000L;
        time %= 86400000L;
        if (days > 1) response.append(days).append(" Tage, ");
        else if (days > 0) response.append(days).append(" Tag, ");

        // Hours
        long hours = time / 3600000L;
        time %= 3600000L;
        if (hours > 1) response.append(hours).append(" Stunden, ");
        else if (hours > 0) response.append(hours).append(" Stunde, ");

        // Minutes
        long minutes = time / 60000L;
        time %= 60000L;
        if (minutes > 1) response.append(minutes).append(" Minuten, ");
        else if (minutes > 0) response.append(minutes).append(" Minute, ");

        // Seconds
        long seconds = time / 1000L;
        if (seconds > 1) response.append(seconds).append(" Sekunden, ");
        else if (seconds > 0) response.append(seconds).append(" Sekunde, ");

        // Return
        return response.substring(0, response.length() - 2);
    }

    public void updateLurkList(HashMap<Integer, Integer> lurkList) {
        this.lurkList.clear();
        this.lurkList.putAll(lurkList);
    }

    // Getter
    public boolean isBlackListed(TwitchMessageEvent event, String command) {
        if (!blackList.containsKey(event.getChannelId())) return false;
        else return blackList.get(event.getChannelId()).contains(command.toLowerCase());
    }

    public boolean checkLurk(TwitchMessageEvent event) {
        return lurkList.containsKey(event.getUserId());
    }
}