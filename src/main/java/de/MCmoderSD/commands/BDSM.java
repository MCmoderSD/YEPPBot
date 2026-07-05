package de.MCmoderSD.commands;

import de.MCmoderSD.bdsm.core.BdsmTestApi;
import de.MCmoderSD.bdsm.data.TestResult;
import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.core.TwitchBot;

import java.util.ArrayList;

public class BDSM extends CommandBuilder {

    // Attributes
    private final BdsmTestApi api;

    // Constructor
    public BDSM(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        var syntax = "Syntax: " + prefix + "BDSM set <id>";

        // About
        var name = new String[]{ "BDSM" };
        var description = "Füge einen BDSM-Test hinzu. " + syntax;

        // Initialize API
        api = new BdsmTestApi();


        // Register command
        var registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Check Args
                if (args.size() < 2) return twitchBot.sendMessage(event, name, "Fehler: Keine Aktion angegeben. " + syntax);

                // Parse Action
                if (args.getFirst().equalsIgnoreCase("set")) {

                    // Parse ID
                    var id = args.get(1);

                    // Fetch Test Result
                    var result = fetchTestResult(id);
                    if (result == null) return twitchBot.sendMessage(event, name, "Fehler: Test mit ID '" + id + "' nicht gefunden.");

                    // Save Test Result
                    bdsmManager.addTestResult(result, event.getUser());

                    // Send Success Message
                    return twitchBot.sendMessage(event, name, "Erfolg: Test mit ID '" + id + "' wurde hinzugefügt.");
                } else {
                    return twitchBot.sendMessage(event, name, "Fehler: Ungültige Aktion '" + args.getFirst() + "'. " + syntax);
                }

            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }

    private TestResult fetchTestResult(String id) {
        try {
            return api.fetchResult(id);
        } catch (Exception e) {
            return null;
        }
    }
}