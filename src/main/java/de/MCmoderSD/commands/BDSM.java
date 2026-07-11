package de.MCmoderSD.commands;

import de.MCmoderSD.bdsm.core.BdsmTestApi;
import de.MCmoderSD.bdsm.data.MatchResult;
import de.MCmoderSD.bdsm.data.TestResult;
import de.MCmoderSD.bdsm.enums.Kink;
import de.MCmoderSD.commands.blueprints.CommandBuilder;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.core.TwitchBot;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static de.MCmoderSD.bdsm.enums.Kink.*;
import static de.MCmoderSD.utilities.MessageHelper.tagUser;

public class BDSM extends CommandBuilder {

    // Attributes
    private final BdsmTestApi api;

    // Constructor
    public BDSM(TwitchBot twitchBot) {
        super(twitchBot);

        // Syntax
        var syntax = "Syntax: " + prefix + "BDSM <set|get|match|biggest>";
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
                if (args.isEmpty()) return twitchBot.sendMessage(event, name, "Fehler: Keine Aktion angegeben. " + syntax);

                // Variables
                var user = event.getUser();
                var action = args.getFirst().toLowerCase();

                // Set Action
                if (action.equalsIgnoreCase("set")) {

                    // Check Args
                    if (args.size() < 2) return twitchBot.sendMessage(event, name, "Fehler: Keine Test-ID angegeben. " + setSyntax);

                    // Parse ID
                    var id = args.get(1);

                    // Fetch Test Result
                    var result = fetchTestResult(id);
                    if (result == null) return twitchBot.sendMessage(event, name, "Fehler: Test mit ID '" + id + "' nicht gefunden. " + setSyntax);

                    // Save Test Result
                    var saved = bdsmManager.addTestResult(result, event.getUser());
                    if (!saved) return twitchBot.sendMessage(event, name, "Fehler: Test mit ID '" + id + "' ist bereits vorhanden YEPP.");

                    // Send Success Message
                    return twitchBot.sendMessage(event, name, "Erfolg: Test mit ID '" + id + "' wurde hinzugefügt.");
                }

                if (action.equalsIgnoreCase("get")) {

                    // Check Args
                    if (args.size() < 2) return twitchBot.sendMessage(event, name, "Fehler: Kein Benutzer angegeben. " + getSyntax);

                    // Parse User
                    var targetUser = args.get(1).replace("@", "").toLowerCase();

                    // Fetch Twitch User
                    var twitchUser = fetchTwitchUser(targetUser);
                    if (twitchUser == null) return twitchBot.sendMessage(event, name, "Fehler: Benutzer '" + targetUser + "' nicht gefunden. " + getSyntax);

                    // Fetch User Test Result
                    var result = bdsmManager.getTestResults(twitchUser);
                    if (result == null || result.isEmpty()) return twitchBot.sendMessage(event, name, "Fehler: Kein Test für " + tagUser(twitchUser) + " gefunden YEPP");

                    var testResult = result.getFirst();
                    return twitchBot.sendMessage(event, name, "Test-Ergebnisse für " + tagUser(twitchUser) + ": " + formatTestResult(testResult));
                }

                if (action.equalsIgnoreCase("match")) {

                    // Match User
                    if (args.size() > 1) {

                        // Parse User
                        var targetUser = args.get(1).replace("@", "").toLowerCase();

                        // Fetch Twitch User
                        var twitchUser = fetchTwitchUser(targetUser);
                        if (twitchUser == null) return twitchBot.sendMessage(event, name, "Fehler: Benutzer '" + targetUser + "' nicht gefunden. " + getSyntax);

                        // Fetch User Test Result
                        var result = bdsmManager.getTestResults(event.getUser());
                        if (result == null || result.isEmpty()) return twitchBot.sendMessage(event, name, "Du hast noch keinen Test gemacht. Bitte benutze " + prefix + "BDSM set <Test-ID> um einen Test hinzuzufügen YEPP");

                        // Fetch Target User Test Result
                        var targetResult = bdsmManager.getTestResults(twitchUser);
                        if (targetResult == null || targetResult.isEmpty()) return twitchBot.sendMessage(event, name, "Fehler: Kein Test für " + tagUser(twitchUser) + " gefunden YEPP");

                        // Calculate Match Result
                        var match = api.fetchMatch(result.getFirst(), targetResult.getFirst());
                        if (match == null) return twitchBot.sendMessage(event, name, "Fehler: Match konnte nicht berechnet werden YEPP");

                        // Send Match Result Message
                        return twitchBot.sendMessage(event, name, tagUser(event.getUser()) + " und " + tagUser(twitchUser) + " sind zu " + match.getScore() + "% miteinander kompatibel YEPP");
                    }

                    // Highlight Compatible Users
                    var result = bdsmManager.getTestResults(user);
                    if (result == null || result.isEmpty()) return twitchBot.sendMessage(event, name, "Du hast noch keinen Test gemacht. Bitte benutze " + prefix + "BDSM set <Test-ID> um einen Test hinzuzufügen YEPP");

                    // Fetch All Test Results
                    var allResults = bdsmManager.getLatestTestResults().entrySet();
                    var matches = new LinkedHashMap<TwitchUser, MatchResult>();
                    for (var entry : allResults) {

                        // Variables
                        var targetUser = entry.getKey();
                        var targetResult = entry.getValue();

                        // Skip Self
                        if (targetUser.getId().equals(user.getId())) continue;

                        // Calculate Match Result
                        var match = api.fetchMatch(result.getFirst(), targetResult);
                        if (match != null) matches.put(targetUser, match);
                    }

                    // Sort Matches by Score
                    var sortedMatches = matches.entrySet().stream().sorted((e1, e2) -> Integer.compare(e2.getValue().getScore(), e1.getValue().getScore()));
                    var mostCompatible = sortedMatches.toList().getFirst();

                    // Send Match Result Message
                    if (mostCompatible == null) return twitchBot.sendMessage(event, name, "Fehler: Keine kompatiblen Benutzer gefunden YEPP");
                    return twitchBot.sendMessage(event, name, tagUser(mostCompatible.getKey()) + " ist der kompatibelste Benutzer mit einer Kompatibilität von " + mostCompatible.getValue().getScore() + "% YEPP");
                }

                if (action.equalsIgnoreCase("biggest")) {

                    // Parse Kink
                    var kinkInput = args.get(1).toLowerCase();
                    if (args.size() > 2) kinkInput = kinkInput + args.get(2).toLowerCase();

                    // Parse Kink
                    var kink = parseKink(kinkInput);
                    if (kink == null) return twitchBot.sendMessage(event, name, "Fehler: Ungültiger Kink '" + kinkInput + "'. " + biggestSyntax);

                    // Fetch Biggest Test Result
                    var result = bdsmManager.getBiggest(kink);
                    if (result == null || result.isEmpty()) return twitchBot.sendMessage(event, name, "Fehler: Kein Test für Kink '" + kinkInput + "' gefunden YEPP");

                    // Send Result Message
                    return twitchBot.sendMessage(event, name, "The biggest " + kinkInput + " is " + tagUser(result.keySet().iterator().next()) + " with a score of " + result.values().iterator().next().getScoreMap().get(kink) + "% YEPP");
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