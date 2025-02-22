package de.MCmoderSD.commands;

import de.MCmoderSD.commands.blueprints.Event;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.HelixHandler;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.utilities.database.SQL;
import de.MCmoderSD.utilities.database.manager.EventManager;

public class NoNutNovember extends Event {

    // Constructor
    public NoNutNovember(BotClient botClient, MessageHandler messageHandler, HelixHandler helixHandler, SQL sql) {

        // Call Event Constructor
        super(botClient, messageHandler, helixHandler, sql,
                EventManager.Event.NNN,
                "It's Not-Nut-November YEPP Type !nnn join in Chat to participate, type !nnn leave if you have sinned. Check the Status of others with !nnn status @user YEPP Happy November YEPP",
                "It's not November, you can nut all you want! YEPP",
                "User not found.",
                "%s hasn't joined No-Nut-November. YEPP",
                "%s is still in No-Nut-November. YEPP",
                "Bro already busted a nut on day %d YEPP",
                "You haven't even joined No-Nut-November. YEPP",
                "You already joined No-Nut-November. YEPP",
                "You already failed No-Nut-November - Try again next year! YEPP",
                "You joined No-Nut-November. YEPP",
                "You failed No-Nut-November - Try again next year! YEPP",
                "Error: Couldn't join No-Nut-November. YEPP",
                "Error: Couldn't leave No-Nut-November. YEPP",
                "No one has joined No-Nut-November. YEPP",
                "Everyone has already busted a nut. YEPP",
                "Everyone is still in No-Nut-November. YEPP",
                "%s are still fighting, but %s already busted a nut. YEPP",
                "%s are still fighting. YEPP",
                "%s already busted a nut. YEPP",
                "No-Nut-November",
        "nonutnovember", "nnn", "no-nut-november"
        );
    }
}