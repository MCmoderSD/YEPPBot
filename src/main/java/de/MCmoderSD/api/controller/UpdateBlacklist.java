package de.MCmoderSD.api.controller;

import de.MCmoderSD.api.objects.ApiResponse;
import de.MCmoderSD.core.TwitchBot;
import de.MCmoderSD.helix.objects.TwitchUser;

public class UpdateBlacklist extends Controller {

    // Endpoint
    public static final String ENDPOINT = "UpdateBlacklist";

    // Constructor
    public UpdateBlacklist(TwitchBot twitchBot, String key) {
        super(twitchBot, ENDPOINT, key, false);
    }

    // Execute Controller
    @Override
    protected ApiResponse execute(TwitchUser channel) {

        // Reload Blacklist from Database
        var entries = twitchBot.getCommandHandler().fetchBlacklist();

        // Return
        return ApiResponse.ok("Updated blacklist with " + entries + " entries");
    }
}
