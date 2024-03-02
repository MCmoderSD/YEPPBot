package de.MCmoderSD.events;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.core.InteractionHandler;

import static de.MCmoderSD.utilities.Calculate.*;

public class Yepp {

    // Constructor
    public Yepp(InteractionHandler interactionHandler, TwitchChat chat) {

        // Register Event
        interactionHandler.registerEvent(new Event("yepp") { // Event Name and Alias
            @Override
            public void execute(ChannelMessageEvent event) {
                chat.sendMessage(getChannel(event), "YEPP");
            }
        });
    }
}