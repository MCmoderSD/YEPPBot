package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.data.TimerState;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.core.TwitchBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Timer extends CommandBuilder {

    // Constants
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([dhms])");
    private static final Pattern DURATION_FORMAT = Pattern.compile("(\\d+[dhms])+");

    // Constructor
    public Timer(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        var syntax = "Syntax: " + prefix + "Timer <start|stop|add|remove|set|reset> [time]";

        // Errors
        var missingTime = "Fehler: Es fehlt eine Zeitangabe. Beispiele: 300, 5m, 1h30m. " + syntax;
        var invalidTime = "Fehler: Ungültige Zeitangabe. Beispiele: 300, 5m, 1h30m. YEPP";

        // About
        var name = new String[]{ "Timer" };
        var description = "Steuert den Timer des Kanals: " + syntax;


        // Register command
        var registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Variables
                var argsSize = args.size();
                var user = event.getUser();
                var channel = event.getChannel();
                var initialState = subathonManager.getTimer(channel);

                // Show Status
                if (args.isEmpty() || (argsSize == 1 && Arrays.asList("status", "time", "show", "zeit").contains(args.getFirst().toLowerCase()))) {
                    return twitchBot.sendMessage(event, name, subathonManager.getTimer(channel).formatState());
                }

                // Parse Action
                var action = args.getFirst().toLowerCase();

                // Check Permissions
                if (!twitchBot.isPermitted(user, channel)) return false;

                // Start Timer
                if (Arrays.asList("start", "resume", "continue", "weiter").contains(action)) {
                    if (!subathonManager.startTimer(channel)) return twitchBot.sendMessage(event, name, "Der Timer läuft bereits. " + initialState.formatState());
                    return twitchBot.sendMessage(event, name, "Der Timer wurde gestartet. " + subathonManager.getTimer(channel).formatState());
                }

                // Pause Timer
                if (Arrays.asList("stop", "pause", "halt").contains(action)) {
                    if (!subathonManager.pauseTimer(channel)) return twitchBot.sendMessage(event, name, "Der Timer läuft aktuell nicht. " + initialState.formatState());
                    return twitchBot.sendMessage(event, name, "Der Timer wurde pausiert. " + subathonManager.getTimer(channel).formatState());
                }

                // Reset Timer
                if (Arrays.asList("reset", "zurücksetzen").contains(action)) {
                    subathonManager.resetTimer(channel);
                    return twitchBot.sendMessage(event, name, "Der Timer wurde zurückgesetzt. " + subathonManager.getTimer(channel).formatState());
                }

                if (Arrays.asList("add", "remove", "set", "hinzufügen", "entfernen", "setzen").contains(action)) {

                    // Check Arguments
                    if (argsSize < 2) return twitchBot.sendMessage(event, name, missingTime);

                    // Parse Duration
                    var duration = parseDuration(args.get(1));
                    if (duration < 0) return twitchBot.sendMessage(event, name, invalidTime);
                    var time = new TimerState(false, duration).formatted();

                    switch (action) {
                        case "add", "hinzufügen" -> {
                            subathonManager.addTime(channel, duration);
                            return twitchBot.sendMessage(event, name, "Es wurden " + time + " zum Timer hinzugefügt. " + subathonManager.getTimer(channel).formatState());
                        }
                        case "remove", "entfernen" -> {
                            subathonManager.removeTime(channel, duration);
                            return twitchBot.sendMessage(event, name, "Es wurden " + time + " vom Timer entfernt. " + subathonManager.getTimer(channel).formatState());
                        }
                        case "set", "setzen" -> {
                            subathonManager.setTime(channel, duration);
                            return twitchBot.sendMessage(event, name, "Der Timer wurde auf " + time + " gesetzt. " + subathonManager.getTimer(channel).formatState());
                        }
                    }
                }

                // Send Syntax Message
                return twitchBot.sendMessage(event, name, syntax);
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }

    private static int parseDuration(String input) {

        // Check Parameters
        if (input == null || input.isBlank()) return -1;

        // Normalize Input
        var duration = input.trim().toLowerCase();

        // Plain Seconds
        if (duration.matches("\\d+")) {
            try {
                var seconds = Long.parseLong(duration);
                return seconds > Integer.MAX_VALUE ? -1 : (int) seconds;
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        // Check Format
        if (!DURATION_FORMAT.matcher(duration).matches()) return -1;

        // Sum Up the Units
        var matcher = DURATION_PATTERN.matcher(duration);
        var total = 0L;
        while (matcher.find()) {
            try {
                var value = Long.parseLong(matcher.group(1));
                total += switch (matcher.group(2)) {
                    case "d" -> value * 86400L;
                    case "h" -> value * 3600L;
                    case "m" -> value * 60L;
                    default -> value;
                };
            } catch (NumberFormatException e) {
                return -1;
            }
            if (total < 0 || total > Integer.MAX_VALUE) return -1;
        }

        // Return the Duration in Seconds
        return (int) total;
    }
}