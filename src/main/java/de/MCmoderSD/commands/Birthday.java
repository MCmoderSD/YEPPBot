package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.handlers.BirthdayHandler;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.data.Birthdate;
import de.MCmoderSD.core.TwitchBot;

import java.time.Month;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import static de.MCmoderSD.utilities.MessageHelper.tagUser;
import static java.time.Month.*;

public class Birthday extends CommandBuilder {

    // Constructor
    public Birthday(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        String syntax = "Syntax: " + prefix + "Birthday <set|get|until|in|next>";
        String setSyntax = "Syntax: " + prefix + "Birthday set DD.MM.CCYY";
        String getSyntax = "Syntax: " + prefix + "Birthday get <user>";
        String inSyntax = "Syntax: " + prefix + "Birthday in <month>";
        String nextSyntax = "Syntax: " + prefix + "Birthday next <amount>";

        // About
        String[] name = { "Birthday", "bday", "Geburtstag", "bd", "geb", "gb" };
        String description = "Setzt deinen Geburtstag. " + syntax;


        // Register command
        boolean registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Check Args
                if (args.isEmpty()) return twitchBot.sendMessage(event, name, syntax);

                // Parse action
                String action = args.getFirst().toLowerCase();

                // Variables
                var user = event.getUser();
                var channel = event.getChannel();
                var argsSize = args.size();

                // Set/Delete Action
                if (Arrays.asList("set", "edit", "delete", "del", "remove", "rem").contains(action)) {

                    // Get Birthday Handler
                    BirthdayHandler birthdayHandler = twitchBot.getBirthdayHandler();

                    // Set Action
                    if (Arrays.asList("set", "edit").contains(action)) {

                        // Check Args
                        if (argsSize < 2) return twitchBot.sendMessage(event, name, setSyntax);

                        // Parse Date
                        String dateString = args.get(1);
                        String[] dateParts = dateString.split("\\.");

                        // Validate Date Parts
                        if (dateParts.length != 3) return twitchBot.sendMessage(event, name, setSyntax);

                        try {

                            // Parse Day, Month, Year
                            var day = Integer.parseInt(dateParts[0]);
                            var month = Integer.parseInt(dateParts[1]);
                            var year = Integer.parseInt(dateParts[2]);

                            // Create Birthdate
                            Birthdate birthdate;
                            try {
                                birthdate = new Birthdate(day, month, year);
                            } catch (IllegalArgumentException e) {
                                return twitchBot.sendMessage(event, name, "Ungültiges Datum: " + e.getMessage());
                            }

                            // Set Birthday
                            birthdayHandler.addBirthday(user, birthdate);

                            // Send Confirmation
                            return twitchBot.sendMessage(event, name, "Dein Geburtstag wurde auf " + birthdate.getFormattedDate() + " gesetzt. YEPP");

                        } catch (NumberFormatException e) {
                            return twitchBot.sendMessage(event, name, setSyntax);
                        } catch (IllegalArgumentException e) {
                            return twitchBot.sendMessage(event, name, "Ungültiges Datum: " + e.getMessage());
                        }
                    }

                    // Delete Action
                    birthdayHandler.removeBirthday(user);
                    return twitchBot.sendMessage(event, name, "Dein Geburtstag wurde gelöscht. YEPP");
                }

                // Get Action
                if (action.equals("get")) {

                    // Check Args
                    if (argsSize < 2) return twitchBot.sendMessage(event, name, getSyntax);

                    // Parse Target User
                    String targetUserName = args.get(1);
                    while (targetUserName.startsWith("@")) targetUserName = targetUserName.substring(1);
                    TwitchUser targetUser = userHandler.getTwitchUser(targetUserName.toLowerCase());

                    // Validate Target User
                    if (targetUser == null) return twitchBot.sendMessage(event, name, "Fehler: Nutzer @" + targetUserName + " nicht gefunden. YEPP");

                    // Get Birthdate
                    Birthdate birthdate = birthdayManager.getBirthday(targetUser);
                    if (birthdate == null) return twitchBot.sendMessage(event, name, "Der Nutzer " + tagUser(targetUser) + " hat keinen Geburtstag gesetzt. YEPP");

                    // Send Birthdate
                    return twitchBot.sendMessage(event, name, "Der Geburtstag von " + tagUser(targetUser) + " ist der " + birthdate.getFormattedDate() + ". YEPP");
                }

                // Variables
                HashMap<TwitchUser, Birthdate> birthdays = getBirthdays(channel);
                LinkedHashMap<TwitchUser, Birthdate> sortedBirthdays = sortBirthdaysByUpcoming(birthdays);

                // Handle Actions
                switch (action) {

                    // Until Action
                    case "until" -> {

                        // Variables
                        TwitchUser targetUser;
                        Birthdate birthdate;

                        // Parse Target User
                        if (argsSize > 2) {

                            // Parse Name
                            String targetUserName = args.get(1);
                            while (targetUserName.startsWith("@")) targetUserName = targetUserName.substring(1);
                            targetUser = userHandler.getTwitchUser(targetUserName.toLowerCase());

                            // Validate Target User
                            if (targetUser == null) return twitchBot.sendMessage(event, name, "Fehler: Nutzer @" + targetUserName + " nicht gefunden. YEPP");
                        } else targetUser = user;

                        // Get Birthdate
                        birthdate = birthdays.get(targetUser);

                        // Validate Birthdate
                        if (birthdate == null)
                            return twitchBot.sendMessage(event, name, "Der Nutzer " + tagUser(targetUser) + " hat keinen Geburtstag gesetzt. YEPP");

                        // Calculate Time Until Birthday
                        var timeUntil = timeUntilBirthday(birthdate);
                        String formattedDuration = formatTimeDuration(timeUntil, TimeUnit.DAYS);

                        // Send Message
                        return twitchBot.sendMessage(event, name, "Bis zum Geburtstag von " + tagUser(targetUser) + " am " + birthdate.getFormattedDate() + " sind es noch " + formattedDuration + ". YEPP");

                        // Send Message
                    }

                    // In Action
                    case "in" -> {

                        // Check Args
                        if (argsSize < 2) return twitchBot.sendMessage(event, name, inSyntax);

                        // Parse Month
                        Month month = parseMonth(args.get(1));
                        if (month == null) return twitchBot.sendMessage(event, name, inSyntax);

                        // Find Birthdays in Month
                        ArrayList<String> usersInMonth = new ArrayList<>();
                        for (var entry : sortedBirthdays.entrySet()) {
                            if (entry.getValue().month() == month.getValue()) {
                                usersInMonth.add(tagUser(entry.getKey()) + " am " + entry.getValue().getFormattedDate());
                            }
                        }

                        // Check Results
                        String monthName = month.name().toLowerCase();
                        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
                        if (usersInMonth.isEmpty()) return twitchBot.sendMessage(event, name, "In " + monthName + " hat niemand Geburtstag. YEPP");

                        // Build Message
                        ArrayList<StringBuilder> messages = new ArrayList<>();
                        StringBuilder message = new StringBuilder("In " + monthName + " haben folgende Nutzer Geburtstag: ");

                        // Fill Messages
                        for (var i = 0; i < usersInMonth.size(); i++) {
                            String userInMonth = usersInMonth.get(i);
                            if (message.length() + userInMonth.length() + 2 > 500) {
                                messages.add(message);
                                message = new StringBuilder();
                            }
                            message.append(userInMonth);
                            if (i < usersInMonth.size() - 1) message.append(", ");
                        }
                        messages.add(message); // Add last message

                        // Send Messages
                        boolean success = true;
                        for (var msg : messages) success &= twitchBot.sendMessage(event, name, msg.toString());
                        return success;
                    }


                    // Next Action
                    case "next" -> {

                        // Find Next Birthdays
                        ArrayList<String> nextBirthdays = new ArrayList<>();
                        for (var entry : sortedBirthdays.entrySet()) {
                            nextBirthdays.add(tagUser(entry.getKey()) + " am " + entry.getValue().getFormattedDate());
                        }

                        // Calculate max Amount
                        var maxAmount = 0;
                        var length = "Die nächsten Geburtstage sind: ".length();
                        for (var nextBirthday : nextBirthdays) {
                            length += nextBirthday.length() + 2;
                            if (length > 500) break;
                            maxAmount++;
                        }

                        // Parse Amount
                        var amount = maxAmount;
                        if (argsSize > 1) {
                            try {
                                amount = Integer.parseInt(args.get(1));
                                if (amount < 1) amount = 1;
                                if (amount > maxAmount) amount = maxAmount;
                            } catch (NumberFormatException e) {
                                return twitchBot.sendMessage(event, name, nextSyntax);
                            }
                        }

                        // Build Message
                        StringBuilder message = new StringBuilder("Die nächsten %d Geburtstage sind: ".formatted(amount));
                        for (var i = 0; i < amount; i++) {
                            message.append(nextBirthdays.get(i));
                            if (i < amount - 1) message.append(", ");
                        }

                        // Send Message
                        return twitchBot.sendMessage(event, name, message.toString());
                    }
                }

                // Invalid Arguments
                return twitchBot.sendMessage(event, name, "Fehler: Ungültige Argumente. " + syntax);
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }

    // Get Birthdays of Channel Community
    private HashMap<TwitchUser, Birthdate> getBirthdays(TwitchUser channel) {

        // Variables
        HashMap<TwitchUser, Birthdate> birthdays = birthdayManager.getBirthdays();

        // Get Users
        HashSet<TwitchUser> users = new HashSet<>();
        users.addAll(roleHandler.getSubscribers(channel));  // Subscribers
        users.addAll(roleHandler.getModerators(channel));   // Moderators
        users.addAll(roleHandler.getFollowers(channel));    // Followers
        users.addAll(chatHandler.getChatters(channel));     // Chatters
        users.addAll(roleHandler.getEditors(channel));      // Editors
        users.addAll(roleHandler.getVIPs(channel));         // VIPs
        users.add(channel);                                 // Channel Owner

        // Map Birthdays
        HashMap<TwitchUser, Birthdate> birthdayMap = new HashMap<>();
        for (var user : users) {
            user = new TwitchUser(user);
            var birthdate = birthdays.get(user);
            if (birthdate != null) birthdayMap.put(user, birthdate);
        }

        // Return Birthdays
        return birthdayMap;
    }

    // Sort Birthdays by Upcoming
    private static LinkedHashMap<TwitchUser, Birthdate> sortBirthdaysByUpcoming(HashMap<TwitchUser, Birthdate> birthdayMap) {

        // Create a list from elements of HashMap
        ArrayList<Map.Entry<TwitchUser, Birthdate>> list = new ArrayList<>(birthdayMap.entrySet());

        // Sort the list based on time until birthday
        list.sort(Comparator.comparingLong(entry -> timeUntilBirthday(entry.getValue())));

        // Put sorted data back into a LinkedHashMap
        LinkedHashMap<TwitchUser, Birthdate> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<TwitchUser, Birthdate> entry : list) sortedMap.put(entry.getKey(), entry.getValue());

        // Return Map
        return sortedMap;
    }

    // Calculate Time Until Birthday
    private static long timeUntilBirthday(Birthdate birthdate) {

        // Current Date
        Calendar now = Calendar.getInstance();
        var currentYear = now.get(Calendar.YEAR);
        var currentMonth = now.get(Calendar.MONTH) + 1; // Months are 0-based
        var currentDay = now.get(Calendar.DAY_OF_MONTH);

        // Birthday This Year
        Calendar birthdayThisYear = Calendar.getInstance();
        birthdayThisYear.set(Calendar.YEAR, currentYear);
        birthdayThisYear.set(Calendar.MONTH, birthdate.month() - 1); // Months are
        birthdayThisYear.set(Calendar.DAY_OF_MONTH, birthdate.day());

        // If birthday has already occurred this year, set to next year
        if (currentMonth > birthdate.month() || (currentMonth == birthdate.month() && currentDay > birthdate.day())) {
            birthdayThisYear.set(Calendar.YEAR, currentYear + 1);
        }

        // Calculate difference
        return birthdayThisYear.getTimeInMillis() - now.getTimeInMillis();
    }

    // Format Time Duration
    @SuppressWarnings("SameParameterValue")
    private static String formatTimeDuration(long duration, TimeUnit unit) {

        // Calculate time components
        long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        // Build String
        return switch (unit) {
            case DAYS -> days + " Tage";
            case HOURS -> {
                long totalHours = TimeUnit.MILLISECONDS.toHours(duration);
                yield totalHours + " Stunden";
            }
            case MINUTES -> {
                long totalMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
                yield totalMinutes + " Minuten";
            }
            default -> days + " Tage, " + hours + " Stunden, " + minutes + " Minuten, " + seconds + " Sekunden";
        };
    }

    // Parse Month from String
    private static Month parseMonth(String monthString) {
        while (monthString.startsWith("0")) monthString = monthString.substring(1);
        return switch (monthString.toLowerCase()) {
            case "1", "january", "jan", "januar", "jänner" -> JANUARY;
            case "2", "february", "feb", "februar" -> FEBRUARY;
            case "3", "march", "mar", "märz" -> MARCH;
            case "4", "april", "apr" -> APRIL;
            case "5", "may", "mai" -> MAY;
            case "6", "june", "jun", "juni" -> JUNE;
            case "7", "july", "jul", "juli" -> JULY;
            case "8", "august", "aug" -> AUGUST;
            case "9", "september", "sep", "sept" -> SEPTEMBER;
            case "10", "october", "oct", "oktober", "okt" -> OCTOBER;
            case "11", "november", "nov" -> NOVEMBER;
            case "12", "december", "dec", "dezember", "dez" -> DECEMBER;
            default -> null;
        };
    }
}