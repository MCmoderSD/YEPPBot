package de.MCmoderSD.handlers;

import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.data.Birthdate;
import de.MCmoderSD.database.Database;
import de.MCmoderSD.database.manager.BirthdayManager;
import de.MCmoderSD.helix.handler.*;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import static de.MCmoderSD.utilities.MessageHelper.tagUser;

public class BirthdayHandler {

    // Associations
    private final TwitchBot twitchBot;

    // Database
    private final Database database;
    private final BirthdayManager birthdayManager;

    // Attributes
    private final ConcurrentHashMap<TwitchUser, Birthdate> birthdays;
    private final HashSet<TwitchUser> congratulatedToday;

    // Constructor
    public BirthdayHandler(TwitchBot twitchBot) {

        // Check Parameters
        if (twitchBot == null) throw new IllegalArgumentException("TwitchBot cannot be null");

        // Set Associations
        this.twitchBot = twitchBot;

        // Set Database
        database = twitchBot.getDatabase();
        birthdayManager = database.getBirthdayManager();

        // Initialize Attributes
        birthdays = new ConcurrentHashMap<>(birthdayManager.getBirthdays());
        congratulatedToday = new HashSet<>();

        // Initialize Weekly Reset Thread
        new Thread(() -> {
            while (true) {
                try {

                    // Sleep 7 days
                    Thread.sleep(604800000L);
                    congratulatedToday.clear();

                } catch (InterruptedException e) {
                    throw new RuntimeException("Birthday Daily Reset Thread interrupted", e);
                }
            }
        }, "Birthday-Daily-Reset-Thread").start();
    }

    public void handleBirthday(MessageEvent event) {
        new Thread(() -> {

            // Check Parameters
            if (event == null) throw new IllegalArgumentException("MessageEvent event cannot be null");

            // Get User
            var user = event.getUser();

            // Check if Congratulated Today
            if (congratulatedToday.contains(user)) return;

            // Check if Birthday
            var birthdate = birthdays.get(user);
            if (!birthdate.isToday()) return;
            else congratulatedToday.add(user);

            // Send Congratulations
            twitchBot.sendMessage(event, "Birthday-Congratulations", String.format("Alles Gute zu deinem %d. Geburtstag, %s! YEPP", birthdate.getAge(), tagUser(user)));

        }, "Handle-Birthday-" + event.getId().toString()).start();
    }

    public void addBirthday(TwitchUser user, Birthdate birthdate) {

        // Check Parameters
        if (user == null) throw new IllegalArgumentException("TwitchUser user cannot be null");
        if (birthdate == null) throw new IllegalArgumentException("Birthdate birthdate cannot be null");

        // Add Birthday
        birthdays.put(user, birthdate);
        congratulatedToday.remove(user);
        birthdayManager.addBirthday(birthdate, user);
    }

    public void removeBirthday(TwitchUser user) {

        // Check Parameters
        if (user == null) throw new IllegalArgumentException("TwitchUser user cannot be null");

        // Remove Birthday
        birthdays.remove(user);
        birthdayManager.deleteBirthday(user);
        congratulatedToday.remove(user);
    }
}