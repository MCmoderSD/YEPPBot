package de.MCmoderSD.commands;

import com.fasterxml.jackson.databind.JsonNode;
import de.MCmoderSD.commands.blueprints.Command;
import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.core.MessageHandler;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.riot.core.RiotAPI;
import de.MCmoderSD.riot.enums.Cluster;
import de.MCmoderSD.riot.enums.Region;
import de.MCmoderSD.riot.enums.Tier;
import de.MCmoderSD.riot.objects.Entry;
import de.MCmoderSD.riot.objects.Summoner;
import de.MCmoderSD.sql.Driver;

import java.util.ArrayList;
import java.util.Arrays;

import static de.MCmoderSD.utilities.other.Format.*;

public class Riot {

    // Constructor
    public Riot(BotClient botClient, MessageHandler messageHandler, JsonNode apiConfig, JsonNode database) {

        // Syntax
        String syntax = "Syntax: " + botClient.getPrefix() + "riot <Username#Tag> (Region)";

        // About
        String[] name = {"riot", "league", "lolstats"};
        String description = "Zeigt den Rank des Streamers an:" + syntax;

        // Init Riot API
        RiotAPI riotAPI = new RiotAPI(apiConfig.get("riot").asText(), Cluster.EUROPE, Driver.DatabaseType.MARIADB, database);

        // Register command
        messageHandler.addCommand(new Command(description, name) {

            @Override
            public void execute(TwitchMessageEvent event, ArrayList<String> args) {

                // Check args
                if (args.isEmpty()) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                // Get Username
                String[] input = String.join(SPACE, args).split("#");
                if (input.length < 2) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }
                String username = input[0].trim();
                String[] split = input[1].split(SPACE);
                String tag = split[0];
                Region region = Region.EUW1;

                // Get Region
                if (split.length > 1) {
                    region = Region.getRegion(split[1]);
                    if (region == null) {
                        botClient.respond(event, getCommand(), "Invalid Region! Use one of the following: " + Arrays.toString(Region.getRegions()).substring(1, Arrays.toString(Region.getRegions()).length() - 1));
                        return;
                    }
                }

                // Check Username
                boolean empty = username.isEmpty() || tag.isEmpty() || username.isBlank() || tag.isBlank();
                boolean  invalid = username.length() < 3 || tag.length() < 3 || username.length() > 16 || tag.length() > 5;
                if (empty || invalid) {
                    botClient.respond(event, getCommand(), syntax);
                    return;
                }

                // Variables
                Summoner summoner;
                Entry entry;

                try {

                    // Get User
                    summoner = riotAPI.getSummoner(username, tag, region);
                    if (summoner == null) {
                        botClient.respond(event, getCommand(), "Player not found! Make sure you entered the correct Username, Tag and Region.");
                        return;
                    }

                    // Get Entry
                    entry = riotAPI.getEntry(summoner, region);

                } catch (Exception e) {
                    botClient.respond(event, getCommand(), "Player not found: " + username + " #" + tag + " in " + region.getRegion());
                    return;
                }

                // Check Entry
                if (entry == null) {
                    botClient.respond(event, getCommand(), "Could not retrieve data for " + username + "#" + tag + " in " + region.getRegion());
                    return;
                }

                // Respond
                String name = username + " #" + tag;
                String level = String.valueOf(summoner.getSummonerLevel());
                Tier tier = Tier.getTier(entry.getTier());
                String rank = tier.getName() + SPACE + entry.getRank();
                String lp = String.valueOf(entry.getLeaguePoints());
                String winRate = String.format("%.2f", (double) entry.getWins() / (entry.getWins() + entry.getLosses()) * 100) + "%";
                String response = name + ": level " + level + " and is currently " + rank + " with " + lp + " LP and a " + winRate + " win rate.";
                botClient.respond(event, getCommand(), response);
            }
        });
    }
}