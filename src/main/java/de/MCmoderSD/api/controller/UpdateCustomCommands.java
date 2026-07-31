package de.MCmoderSD.api.controller;

import de.MCmoderSD.api.objects.ApiResponse;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.helix.objects.TwitchUser;

public class UpdateCustomCommands extends Controller {

    // Endpoint
    public static final String ENDPOINT = "UpdateCustomCommands";

    // Constructor
    public UpdateCustomCommands(TwitchBot twitchBot, String key) {
        super(twitchBot, ENDPOINT, key);
    }

    // Execute Controller
    @Override
    protected ApiResponse execute(TwitchUser channel) {

        // Reload Custom Commands from Database
        twitchBot.getCommandManager().fetchCustomCommandsForChannel(channel);

        // Return
        return ApiResponse.ok("Updated custom commands for channel: " + channel.getDisplayName());
    }
}