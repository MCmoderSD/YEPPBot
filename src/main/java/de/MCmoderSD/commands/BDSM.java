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
import static de.MCmoderSD.utilities.MessageHelper.SPACE;
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
        var matchSyntax = "Syntax: " + prefix + "BDSM match <@User|Top> <amount>";
        var biggestSyntax = "Syntax: " + prefix + "BDSM biggest <ageplayer|brat|brat tamer|daddy|mommy|degrader|dominant|degradee|little|masochist|master|mistress|nonmonogamist|owner|hunter|hunter|pet|prey|prey|rigger|rope bunny|sadist|slave|submissive|sub|switch|vanilla|voyeur|exhibitionist|experimentalist>";

        // About
        var name = new String[]{ "BDSM", "BDSM-Test", "Kink" };
        var description = "Füge einen BDSM-Test hinzu. " + syntax;

        // Initialize API
        api = new BdsmTestApi();


        // Register command
        var registered = commandHandler.registerCommand(new Command(description, name) {

            @Override
            public boolean execute(MessageEvent event, ArrayList<String> args) {

                // Check Args
                if (args.size() < 2) return twitchBot.sendMessage(event, name, "Fehler: Keine Aktion angegeben. " + syntax);

                // Variables
                var action = args.getFirst().toLowerCase();

                // Handle Actions
                return switch (action) {
                    case "set" -> actionSet(event, args, name, setSyntax);
                    case "get" -> actionGet(event, args, name, getSyntax);
                    case "match" -> actionMatch(event, args, name, matchSyntax);
                    case "biggest" -> actionBiggest(event, args, name, biggestSyntax);
                    default -> twitchBot.sendMessage(event, name, "Fehler: Ungültige Aktion '" + args.getFirst() + "'. " + syntax);
                };
            }
        });

        if (!registered) throw new IllegalStateException("Command registration failed for command: " + name[0]);
    }

    // Action Methods
    private boolean actionSet(MessageEvent event, ArrayList<String> args, String name, String setSyntax) {

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

    private boolean actionGet(MessageEvent event, ArrayList<String> args, String name, String getSyntax) {

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

    private boolean actionMatch(MessageEvent event, ArrayList<String> args, String name, String matchSyntax) {

        // Variables
        var user = event.getUser();

        // Match User
        if (args.get(1).startsWith("@")) {

            // Parse User
            var targetUser = args.get(1).substring(1).toLowerCase();

            // Fetch Twitch User
            var twitchUser = fetchTwitchUser(targetUser);
            if (twitchUser == null) return twitchBot.sendMessage(event, name, "Fehler: Benutzer '" + targetUser + "' nicht gefunden YEPP");

            // Fetch User Test Result
            var result = bdsmManager.getTestResults(user);
            if (result == null || result.isEmpty()) return twitchBot.sendMessage(event, name, "Du hast noch keinen Test gemacht. Bitte benutze " + prefix + "BDSM set <Test-ID> um einen Test hinzuzufügen YEPP");

            // Fetch Target User Test Result
            var targetResult = bdsmManager.getTestResults(twitchUser);
            if (targetResult == null || targetResult.isEmpty()) return twitchBot.sendMessage(event, name, "Fehler: Kein Test für " + tagUser(twitchUser) + " gefunden YEPP");

            // Calculate Match Result
            var match = api.fetchMatch(result.getFirst(), targetResult.getFirst());
            if (match == null) return twitchBot.sendMessage(event, name, "Fehler: Match konnte nicht berechnet werden YEPP");

            // Send Match Result Message
            return twitchBot.sendMessage(event, name, tagUser(user) + " und " + tagUser(twitchUser) + " sind zu " + match.getScore() + "% miteinander kompatibel YEPP");
        }

        // Top Matches
        if (args.get(1).equalsIgnoreCase("top")) {

            // Fetch User Test Result
            var result = bdsmManager.getTestResults(user);
            if (result == null || result.isEmpty()) return twitchBot.sendMessage(event, name, "Du hast noch keinen Test gemacht. Bitte benutze " + prefix + "BDSM set <Test-ID> um einen Test hinzuzufügen YEPP");

            // Fetch All Test Results
            var allResults = bdsmManager.getLatestTestResults().entrySet();
            var matchCache = bdsmManager.getMatchCache(result.getFirst());
            var matches = new LinkedHashMap<TwitchUser, MatchResult>();
            for (var entry : allResults) {

                // Variables
                var targetUser = entry.getKey();
                var targetResult = entry.getValue();

                // Skip Self
                if (targetUser.equals(user)) continue;

                // Check Cache
                if (matchCache.containsKey(targetResult.getId())) {
                    matches.put(targetUser, matchCache.get(targetResult.getId()));
                    continue;
                }

                // Calculate Match Result
                var match = api.fetchMatch(result.getFirst(), targetResult);
                if (match == null) continue;

                // Add to Matches
                matches.put(targetUser, match);
                bdsmManager.addMatch(match);
            }

            // Sort Matches by Score
            var sortedMatches = matches.entrySet().stream().sorted((e1, e2) -> Integer.compare(e2.getValue().getScore(), e1.getValue().getScore()));

            // Parse Amount
            var amount = 1;
            if (args.size() > 2) {
                try {
                    amount = Integer.parseInt(args.get(2));
                } catch (NumberFormatException e) {
                    return twitchBot.sendMessage(event, name, "Fehler: Ungültige Anzahl '"  + args.get(2) + "'. " + matchSyntax);
                }
            }

            // Get Most Compatible User
            var mostCompatible = sortedMatches.limit(amount).toList();
            if (mostCompatible.isEmpty()) return twitchBot.sendMessage(event, name, "Fehler: Keine kompatiblen Benutzer gefunden YEPP");

            // Send Match Result Message
            if (amount == 1) return twitchBot.sendMessage(event, name, tagUser(user) + " ist am kompatibelsten mit " + tagUser(mostCompatible.getFirst().getKey()) + " mit einer kompatibilität von " + mostCompatible.getFirst().getValue().getScore() + "% YEPP");

            // Send Match Result Message for Multiple Users
            if (amount > 1) {
                amount = 0;
                var sb = new StringBuilder();
                for (var entry : mostCompatible) {
                    var string = tagUser(entry.getKey()) + " (" + entry.getValue().getScore() + "%), ";
                    if (sb.length() + string.length() > 458) break;
                    sb.append(string);
                    amount++;
                }

                // Send Result Message
                var message = tagUser(user) + " deine " + amount + " kompatibelsten Benutzer sind: " + sb.substring(0, sb.length() - 2) + " YEPP";
                return twitchBot.sendMessage(event, name,  message);
            }
        }

        // Invalid Match Action
        return twitchBot.sendMessage(event, name, "Fehler: Ungültige Match-Aktion '" + args.get(1) + "'. " + matchSyntax);
    }

    private boolean actionBiggest(MessageEvent event, ArrayList<String> args, String name, String biggestSyntax) {

        // Parse Kink
        var kinkInput = args.get(1).toLowerCase();
        if (args.size() > 2) kinkInput = kinkInput + SPACE + args.get(2).toLowerCase();

        // Parse Kink
        var kink = parseKink(kinkInput);
        if (kink == null) return twitchBot.sendMessage(event, name, "Fehler: Ungültiger Kink '" + kinkInput + "'. " + biggestSyntax);

        // Fetch Biggest Test Result
        var result = bdsmManager.getBiggest(kink);
        if (result == null || result.isEmpty()) return twitchBot.sendMessage(event, name, "Fehler: Kein Test für Kink '" + kinkInput + "' gefunden YEPP");

        // Send Result Message
        return twitchBot.sendMessage(event, name, "The biggest " + kinkInput + " is " + tagUser(result.keySet().iterator().next()) + " with a score of " + result.values().iterator().next().getScoreMap().get(kink) + "% YEPP");
    }

    // Helper Methods
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