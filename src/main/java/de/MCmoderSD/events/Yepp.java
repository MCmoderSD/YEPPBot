package de.MCmoderSD.events;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.InteractionHandler;

import de.MCmoderSD.utilities.database.MySQL;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Yepp {

    // Constructor
    public Yepp(MySQL mySQL, InteractionHandler interactionHandler, TwitchChat chat) {

        // About
        String[] name = {"yepp"};

        // Register Event
        interactionHandler.registerEvent(new Event(name) {
            @Override
            public void execute(ChannelMessageEvent event) {
                String response = "YEPP";
                chat.sendMessage(getChannel(event), response);
                mySQL.logResponse(event, getEvent(), getMessage(event), response);
            }
        });
    }
}