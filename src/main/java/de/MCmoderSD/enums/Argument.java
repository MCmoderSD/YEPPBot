package de.MCmoderSD.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public enum Argument {

    // Commands
    HELP("help", "h"),
    VERSION("version", "v"),
    DEV("dev"),
    CLI("cli"),
    NO_LOG("nolog"),
    GENERATE("generate", "gen"),
    NON_INTERACTIVE("noninteractive", "ni"),
    CONTAINER("container", "docker"),

    // Config
    BOT_CONFIG("botconfig"),
    CHANNEL_LIST("channellist"),
    SQL_CONFIG("sql"),
    HTTPS_SERVER("httpsserver"),
    API_CONFIG("api"),
    OPENAI_CONFIG("openai"),
    HOST("host"),
    PORT("port");

    private final String name;
    private final HashSet<String> alias;

    Argument(String name, String... alias) {
        this.name = name;
        this.alias = new HashSet<>(Arrays.asList(alias));
    }

    public boolean hasNameOrAlias(String input) {
        return name.equals(input.toLowerCase()) || alias.contains(input.toLowerCase());
    }

    public String getConfig(ArrayList<String> args) {

        // Check
        if (this == HELP || this == VERSION || this == DEV || this == CLI || this == NO_LOG || this == GENERATE || this == NON_INTERACTIVE || this == CONTAINER) return null;
        if (args == null || args.isEmpty()) return null;

        // Get Config
        var index = 0;
        for (String arg : args) {
            if (arg.contains(name)) break;
            index++;
        }

        // Check
        if (index + 1 >= args.size()) return null;
        return args.get(index + 1);
    }
}