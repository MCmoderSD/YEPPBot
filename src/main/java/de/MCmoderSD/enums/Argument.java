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

    // Config
    BOT_CONFIG("botconfig"),
    CHANNEL_LIST("channellist"),
    MYSQL_CONFIG("mysqlconfig"),
    HTTPS_SERVER("httpserver"),
    API_CONFIG("apiconfig"),
    OPENAI_CONFIG("openaiconfig"),
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

    public boolean hasNameOrAlias(ArrayList<String> args) {
        if (args == null || args.isEmpty()) return false;
        for (String arg : args) if (hasNameOrAlias(arg)) return true;
        return false;
    }

    public String getConfig(ArrayList<String> args) {

        // Check
        if (this == HELP || this == VERSION || this == DEV || this == CLI || this == NO_LOG || this == GENERATE) return null;
        if (args == null || args.isEmpty()) return null;
        if (!hasNameOrAlias(args)) return null;

        // Get Config
        var index = args.indexOf("-" + name);
        if (index == -1) index = args.indexOf("/" + name);
        if (index == -1) index = args.indexOf(name);
        if (index == -1) return null;
        return args.get(index + 1);
    }
}