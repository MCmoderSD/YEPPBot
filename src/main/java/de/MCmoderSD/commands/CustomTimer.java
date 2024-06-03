package de.MCmoderSD.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.CommandHandler;
import de.MCmoderSD.objects.Timer;

import de.MCmoderSD.utilities.database.MySQL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class CustomTimer {

    // Constructor
    public CustomTimer(MySQL mySQL, CommandHandler commandHandler, TwitchChat chat, ArrayList<String> admins) {

        // ToDo Make it possible to create timers with hours, minutes and seconds

        String prefix = commandHandler.getPrefix();
        //String syntax = prefix + "CustomTimer create/enable/disable/delete/list name 1000M/500h/30m/10s : response";
        String syntax = prefix + "CustomTimer create/enable/disable/delete/list name 1000M : response";

        // About
        String[] name = {"customtimer", "ctimer", "ct"};
        //String description = "Kann benutzt werden, um eigene Timer zu erstellen M = Messages, h = hours m = minutes, s = seconds. Syntax: " + syntax;
        String description = "Kann benutzt werden, um eigene Timer zu erstellen M = Messages. Syntax: " + syntax;

        // Register command
        commandHandler.registerCommand(new Command(description, name) {
            @Override
                public void execute(ChannelMessageEvent event, String... args) {

                // Get author and channel
                String author = getAuthor(event);
                String channel = getChannel(event);
                String response = syntax;

                // Check if user is a moderator
                if (!(channel.equals(author) || admins.contains(author))) return;

                // Get Custom Timers
                HashMap<String, Timer> customTimers = mySQL.getCustomTimers(event, chat);

                if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
                    sendMessage(mySQL, chat, event, getCommand(), channel, getCustomTimerNames(channel, customTimers, mySQL.getActiveCustomTimers(chat)), args);
                    return;
                }

                // Get Custom Timer syntax
                HashMap<String, Integer> message = new HashMap<>();
                for (var i = 1; i < args.length; i++) message.put(args[i], i);

                if (args.length < 2) sendMessage(mySQL, chat, event, getCommand(), channel, response, args);
                else if (Arrays.asList("create", "add", "enable", "disable", "delete", "del", "remove", "rm", "list").contains(args[0].toLowerCase())) {

                    // Get Custom Timer name
                    String timerName = args[1].toLowerCase();

                    // Process message
                    switch (args[0].toLowerCase()) {

                            // Enable Custom Timer
                        case "enable":
                            response = customTimers.containsKey(timerName) ? mySQL.editCustomTimer(event, timerName, customTimers.get(timerName).getTime() + "M", customTimers.get(timerName).getResponse(), true) : "Custom Timer does not exist";
                            break;

                            // Disable Custom Timer
                        case "disable":
                            response = customTimers.containsKey(timerName) ? mySQL.editCustomTimer(event, timerName, customTimers.get(timerName).getTime() + "M", customTimers.get(timerName).getResponse(), false) : "Custom Timer does not exist";
                            break;

                            // Delete Custom Timer
                        case "delete":
                        case "del":
                        case "remove":
                        case "rm":
                            response = customTimers.containsKey(timerName) ? mySQL.deleteCustomTimer(event, timerName) : "Custom Timer does not exist";
                            break;

                            // Create Custom Timer
                        case "create":
                        case "add":
                            if (!message.containsKey(":") && args.length < 4) response = "Syntax: " + syntax;
                            else if (customTimers.containsKey(timerName)) response = "Custom Timer already exists";
                            else {

                                // Get Time
                                if (inValidCustomTime(args[2])) {
                                    sendMessage(mySQL, chat, event, getCommand(), channel, "Not valid format or to long time" , args);
                                    return;
                                }

                                // Get Custom Timer response
                                StringBuilder stringBuilder = new StringBuilder();
                                for (var i = message.get(":") + 1; i < args.length; i++) stringBuilder.append(args[i]).append(" ");
                                String timerResponse = trimMessage(stringBuilder.toString());

                                // Create command
                               response = mySQL.createCustomTimer(event, timerName, args[2], timerResponse);
                            }
                            break;

                            // Error
                        default:
                            response = "Syntax: " + syntax;
                            break;
                    }
                } else response = "Syntax: " + syntax;

                sendMessage(mySQL, chat, event, getCommand(), channel, response, args);
                commandHandler.updateCustomTimers();
            }
        });
    }

    // Send Message
    private void sendMessage(MySQL mySQL, TwitchChat chat, ChannelMessageEvent event, String command, String channel, String response, String... args) {

        // Send Message
        chat.sendMessage(channel, response);

        // Log response
        mySQL.logResponse(event, command, processArgs(args), response);
    }

    private String getCustomTimerNames(String channel, HashMap<String, Timer> customTimers, ArrayList<Timer> enabledTimers) {
        if (customTimers.isEmpty()) return "No Custom Timers available";

        StringBuilder stringBuilder = new StringBuilder("Custom Timers: ");
        ArrayList<String> enabledTimersNames = new ArrayList<>();

        for (var timer : enabledTimers) {
            if (timer.getChannel().equals(channel)) stringBuilder.append(timer.getName()).append(" enabled, ");
            enabledTimersNames.add(timer.getName());
        }

        for (var timer : customTimers.values()) if (timer.getChannel().equals(channel) && !enabledTimersNames.contains(timer.getName())) stringBuilder.append(timer.getName()).append(" disabled, ");

        return stringBuilder.substring(0, stringBuilder.length() - 2) + ".";
    }

    @SuppressWarnings({"EnhancedSwitchMigration"})
    private boolean inValidCustomTime(String time) {

        // Check if time is a valid number
        try {
            Long.parseLong(time.substring(0, time.length() - 1));
        } catch (NumberFormatException e) {
            return false;
        }

        long timeValue = Long.parseLong(time.substring(0, time.length() - 1));
        char timeType = time.charAt(time.length() - 1);

        //if (!(timeType == 'M' || timeType == 'h' || timeType == 'm' || timeType == 's')) return false; ToDo Fix for all
        if (!(timeType == 'M')) return false;

        switch (timeType) {
            //noinspection DataFlowIssue ToDo Fix
            case 'M', 's':
                return timeValue > 9223372036854775806L;
            /*case 'h':
                return timeValue <= 2562047788015215L;
            case 'm':
                return timeValue <= 153722867280912930L;*/
            default:
                return false;
        }

    }
}
