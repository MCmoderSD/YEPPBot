package de.MCmoderSD.events;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.InteractionHandler;

import de.MCmoderSD.utilities.database.MySQL;

import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class StoppedLurk {

    // Constructor
    public StoppedLurk(MySQL mySQL, InteractionHandler interactionHandler, TwitchChat chat, HashMap<String, String> lurkChannel, HashMap<String, Long> lurkTime) {

        // Register Event
        interactionHandler.registerEvent(new Event("$stoppedlurk") {
            @Override
            public void execute(ChannelMessageEvent event) {
                String author = getAuthor(event);
                String channel = getChannel(event);
                String lurk = lurkChannel.get(author);
                String response;

                if (lurk.equals(channel)) {

                    // Send message
                    response = tagAuthor(event) + " war " + getLurkTime(lurkTime.get(author)) + " im Lurk!";
                    chat.sendMessage(getChannel(event), response);

                } else { // Snitch on lurked channel

                    // Send message
                    response = tagAuthor(event) + " ist ein verräter, hab den kek gerade im chat von " + tagChannel(event) + " gesehen!";
                    chat.sendMessage(lurk, response);
                }

                // Log response
                mySQL.logResponse(event, getEvent(), getMessage(event), response);

                // Remove user from lurk list
                lurkChannel.remove(author);
                lurkTime.remove(author);
            }
        });
    }

    // Calculate the time the user was lurking
    private String getLurkTime(long time) {
        long lurkedSeconds = (System.nanoTime() - time) / 1000000000;
        long lurkedMinutes = lurkedSeconds / 60;
        long lurkedHours = lurkedMinutes / 60;
        long lurkedDays = lurkedHours / 24;
        long lurkedWeeks = lurkedDays / 7;

        // Calculate the time
        lurkedDays %= 7;
        lurkedHours %= 24;
        lurkedMinutes %= 60;
        lurkedSeconds %= 60;

        StringBuilder lurkTime = new StringBuilder();

        if (lurkedWeeks > 0) lurkTime.append(lurkedWeeks).append(lurkedWeeks > 1 ? " Wochen " : " Woche ");
        if (lurkedDays > 0) lurkTime.append(lurkedDays).append(lurkedDays > 1 ? " Tage " : " Tag ");
        if (lurkedHours > 0) lurkTime.append(lurkedHours).append(lurkedHours > 1 ? " Stunden " : " Stunde ");
        if (lurkedMinutes > 0) lurkTime.append(lurkedMinutes).append(lurkedMinutes > 1 ? " Minuten " : " Minute ");
        if (lurkedSeconds > 0) lurkTime.append(lurkedSeconds).append(lurkedSeconds > 1 ? " Sekunden" : " Sekunde");

        return lurkTime.toString();
    }
}