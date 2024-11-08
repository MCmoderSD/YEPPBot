package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.Event;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.database.manager.EventManager;

public class DickDestroyDecember extends Event {

    // Constructor
    public DickDestroyDecember(BotClient botClient, MessageHandler messageHandler, MySQL mySQL, HelixHandler helixHandler) {

        // Call Event Constructor
        super(botClient, messageHandler, mySQL, helixHandler,
                EventManager.Event.DDD,
                "It's Dick-Destroy-December YEPP Type !ddd join in Chat to participate, type !ddd leave if you have sinned. Check the Status of others with !nnn status @user YEPP Happy December YEPP",
                "It's not December, you don't have to beat your meat. YEPP",
                "User not found.",
                "%s hasn't joined Dick-Destroy-December. YEPP",
                "%s is still in Dick-Destroy-December. YEPP",
                "Bro's meat already gave up on day %d YEPP",
                "You haven't even joined Dick-Destroy-December. YEPP",
                "You already joined Dick-Destroy-December. YEPP",
                "You already left Dick-Destroy-December. YEPP",
                "You joined Dick-Destroy-December. YEPP",
                "You left Dick-Destroy-December. YEPP",
                "Error: Couldn't join Dick-Destroy-December. YEPP",
                "Error: Couldn't leave Dick-Destroy-December. YEPP",
                "No one has joined Dick-Destroy-December. YEPP",
                "Everyone's meat already gave up. YEPP",
                "Everyone is still in Dick-Destroy-December. YEPP",
                "%s are fighting, but %s already gave up. YEPP",
                "%s are still fighting. YEPP",
                "%s gave already up. YEPP",
                "Dick-Destroy-December",
                "dickdestroydecember", "ddd", "destroydickdecember", "dicdestroydecember"
        );
    }
}