package de.MCmoderSD.utilities;

import tools.jackson.databind.JsonNode;

import static de.MCmoderSD.utilities.MessageHelper.*;

public class ConfigValidator extends de.MCmoderSD.helix.utilities.ConfigValidator {

    public static boolean validateTwitchConfig(JsonNode config) throws IllegalArgumentException {

        // Check Config
        if (config == null || config.isNull() || config.isEmpty()) throw new IllegalArgumentException("Twitch config cannot be null or empty");
        if (!config.has("owner") || config.get("owner").isNull() || !config.get("owner").isArray() || config.get("owner").isEmpty()) throw new IllegalArgumentException("Twitch config missing 'owner'");
        if (!config.has("botAlias") || config.get("botAlias").isNull() || !config.get("botAlias").isArray() || config.get("botAlias").isEmpty()) throw new IllegalArgumentException("Twitch config missing 'botAlias'");
        if (!config.has("oauthToken") || config.get("oauthToken").isNull() || !config.get("oauthToken").isString()) throw new IllegalArgumentException("Twitch config missing 'oauthToken'");
        if (!config.has("prefix") || config.get("prefix").isNull() || config.get("prefix").isEmpty() || !config.get("prefix").isArray()) throw new IllegalArgumentException("Twitch config missing 'prefix'");
        if (!config.has("application") || config.get("application").isNull() || config.get("application").isEmpty()) throw new IllegalArgumentException("Twitch config missing 'application'");

        // Parse and Check owner
        JsonNode ownerArray = config.get("owner");
        for (var owner : ownerArray) {
            if (owner == null || owner.isNull() || !(owner.isNumber() || owner.isString())) throw new IllegalArgumentException("Twitch config 'owner' contains an invalid value");
            if (owner.isNumber()) {
                var ownerId = owner.asInt();
                if (ownerId <= 0) throw new IllegalArgumentException("Twitch config 'owner' contains an invalid user ID: " + ownerId);
            } else {
                String ownerName = owner.asString();
                if (ownerName.isBlank() || ownerName.contains(SPACE)) throw new IllegalArgumentException("Twitch config 'owner' contains an invalid username: " + ownerName);
            }
        }

        // Parse and check bot alias
        JsonNode botAliasArray = config.get("botAlias");
        for (var botAlias : botAliasArray) {
            if (botAlias == null || botAlias.isNull() || !botAlias.isString()) throw new IllegalArgumentException("Twitch config 'botAlias' contains a null or non-text value");
            String botAliasText = botAlias.asString();
            if (botAliasText.isBlank() || botAliasText.contains(SPACE)) throw new IllegalArgumentException("Twitch config 'botAlias' contains an invalid alias: " + botAliasText);
        }

        // Parse and check channel
        if (!config.has("channel")) {

            // Validate channel array
            if (config.get("channel").isNull() || !config.get("channel").isArray() || config.get("channel").isEmpty()) throw new IllegalArgumentException("Twitch config missing 'channel'");

            // Check each channel entry
            JsonNode channelArray = config.get("channel");
            for (var channel : channelArray) {
                if (channel == null || channel.isNull() || !(channel.isNumber() || channel.isString()))
                    throw new IllegalArgumentException("Twitch config 'channel' contains an invalid value");
                if (channel.isNumber()) {
                    var channelId = channel.asInt();
                    if (channelId <= 0) throw new IllegalArgumentException("Twitch config 'channel' contains an invalid channel ID: " + channelId);
                } else {
                    String channelName = channel.asString();
                    if (channelName.isBlank() || channelName.contains(SPACE)) throw new IllegalArgumentException("Twitch config 'channel' contains an invalid channel name: " + channelName);
                }
            }
        }

        // Check oauthToken
        String oauthToken = config.get("oauthToken").asString();
        if (oauthToken.isBlank() || oauthToken.contains(SPACE)) throw new IllegalArgumentException("Twitch config 'oauthToken' is invalid");

        // Check prefix
        JsonNode prefixArray = config.get("prefix");
        if (prefixArray == null || prefixArray.isNull() || prefixArray.isEmpty() || !prefixArray.isArray()) throw new IllegalArgumentException("Twitch config 'prefix' must be a non-empty array");
        for (var prefix : prefixArray) {
            if (prefix == null || prefix.isNull() || !prefix.isString()) throw new IllegalArgumentException("Twitch config 'prefix' contains a null or non-text value");
            String prefixText = prefix.asString();
            if (prefixText.isBlank() || prefixText.contains(SPACE)) throw new IllegalArgumentException("Twitch config 'prefix' contains an invalid prefix: " + prefixText);
        }

        // Validate using parent class
        return validateApplicationConfig(config.get("application"));
    }
}