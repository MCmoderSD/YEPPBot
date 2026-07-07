package de.MCmoderSD.commands;

import de.MCmoderSD.bdsm.core.BdsmTestApi;
import de.MCmoderSD.bdsm.data.TestResult;
import de.MCmoderSD.bdsm.enums.Kink;
import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.core.TwitchBot;

import java.util.ArrayList;

import static de.MCmoderSD.bdsm.enums.Kink.*;
import static de.MCmoderSD.utilities.MessageHelper.tagUser;

public class BDSM extends CommandBuilder {

    // Attributes
    private final BdsmTestApi api;

    // Constructor
    public BDSM(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        var syntax = "Syntax: " + prefix + "BDSM <set|get|biggest>";
        var setSyntax = "Syntax: " + prefix + "BDSM set <Test-ID>";
        var getSyntax = "Syntax: " + prefix + "BDSM get <@User>";
        var biggestSyntax = "Syntax: " + prefix + "BDSM biggest <ageplayer|brat|brat tamer|daddy|mommy|degrader|dominant|degradee|little|masochist|master|mistress|nonmonogamist|owner|hunter|hunter|pet|prey|prey|rigger|rope bunny|sadist|slave|submissive|sub|switch|vanilla|voyeur|exhibitionist|experimentalist>";

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

                var action = args.getFirst().toLowerCase();

                // Set Action
                if (action.equalsIgnoreCase("set")) {

                    // Parse ID
                    var id = args.get(1);

                    // Fetch Test Result
                    var result = fetchTestResult(id);
                    if (result == null) return twitchBot.sendMessage(event, name, "Fehler: Test mit ID '" + id + "' nicht gefunden. " + setSyntax);

                    // Save Test Result
                    var saved = bdsmManager.addTestResult(result, event.getUser());
                    if (!saved) return twitchBot.sendMessage(event, name, "Fehler: Test mit ID '" + id + "' ist bereits vorhanden.");

                    // Send Success Message
                    return twitchBot.sendMessage(event, name, "Erfolg: Test mit ID '" + id + "' wurde hinzugefügt.");
                }

                if (action.equalsIgnoreCase("get")) {

                    // Parse User
                    var targetUser = args.get(1).replace("@", "").toLowerCase();

                    // Fetch Twitch User
                    var twitchUser = fetchTwitchUser(targetUser);
                    if (twitchUser == null) return twitchBot.sendMessage(event, name, "Fehler: Benutzer '" + targetUser + "' nicht gefunden. " + getSyntax);

                    var result = bdsmManager.getTestResults(twitchUser);
                    if (result == null || result.isEmpty()) return twitchBot.sendMessage(event, name, "Fehler: Kein Test für Benutzer '" + targetUser + "' gefunden.");

                    var testResult = result.getFirst();
                    return twitchBot.sendMessage(event, name, "Test-Ergebnisse für " + tagUser(twitchUser) + ": " + formatTestResult(testResult));
                }

                if (action.equalsIgnoreCase("biggest")) {

                    var kink = parseKink(args.get(1));
                    if (kink == null) return twitchBot.sendMessage(event, name, "Fehler: Ungültiger Kink '" + args.get(1) + "'. " + biggestSyntax);

                    // Fetch Biggest Test Result
                    var result = bdsmManager.getBiggest(kink);
                    if (result == null || result.isEmpty()) return twitchBot.sendMessage(event, name, "Fehler: Kein Test für Kink '" + args.get(1) + "' gefunden.");

                    // Send Result Message
                    return twitchBot.sendMessage(event, name, "The biggest " + args.get(1) + " is " + tagUser(result.keySet().iterator().next()) + " with a score of " + result.values().iterator().next().getScoreMap().get(kink) + "%");
                }

                return twitchBot.sendMessage(event, name, "Fehler: Ungültige Aktion '" + args.getFirst() + "'. " + syntax);
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

    private TwitchUser fetchTwitchUser(String username) {
        try {
            return userHandler.getTwitchUser(username);
        } catch (Exception e) {
            return null;
        }
    }

    private static String formatTestResult(TestResult result) {
        StringBuilder message = new StringBuilder();
        for (var score : result.getScores()) {
            if (score.score() == 0) continue;
            message.append(score.kink().name()).append(": ").append(score.score()).append("%, ");
        }
        return message.substring(0, message.length() - 2);
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static Kink parseKink(String kink) {
        return switch (kink.toLowerCase()) {
            case "ageplayer" -> Ageplayer;
            case "brat"  -> Brat;
            case "brattamer", "brat tamer" -> BratTamer;
            case "daddy", "mommy" -> DaddyMommy;
            case "degrader" -> Degrader;
            case "dominant" -> Dominant;
            case "degradee" -> Degradee;
            case "little" -> Little;
            case "masochist" -> Masochist;
            case "master", "mistress" -> MasterMistress;
            case "nonmonogamist" -> NonMonogamist;
            case "owner" -> Owner;
            case "primal hunter", "hunter" -> PrimalHunter;
            case "pet" -> Pet;
            case "primal prey", "prey" -> PrimalPrey;
            case "rigger" -> Rigger;
            case "ropebunny", "rope bunny" -> RopeBunny;
            case "sadist" -> Sadist;
            case "slave" -> Slave;
            case "submissive", "sub" -> Submissive;
            case "switch" -> Switch;
            case "vanilla" -> Vanilla;
            case "voyeur" -> Voyeur;
            case "exhibitionist" -> Exhibitionist;
            case "experimentalist" -> Experimentalist;
            default -> null;
        };
    }
}