package de.MCmoderSD.enums;

public enum Account {

    // Games
    APEX("Apex Legends","apex", "apex legends"),
    LEAGUE("League of Legends", "league", "lol", "league of legends", "liga der legenden"),
    RAINBOW("Rainbow Six Siege", "rainbow", "siege", "rainbow six siege", "r6", "r6s", "r6 siege"),
    VALORANT("VALORANT", "valorant", "valo", "valorankt", "valoranked"),

    // Social Media
    INSTAGRAM("Instagram", "instagram","insta"),
    TIKTOK("TikTok", "tiktok", "tt"),
    TWITTER("Twitter", "twitter", "x"),
    YOUTUBE("YouTube", "youtube", "yt");

    // Attributes
    private final String table;
    private final String name;
    private final String[] aliases;

    // Constructor
    Account(String name, String table, String... aliases) {
        this.name = name;
        this.table = table;
        this.aliases = aliases;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getTable() {
        return table;
    }

    public static Account getAccount(String name) {
        for (Account account : values()) {
            if (account.getName().equalsIgnoreCase(name) || account.getTable().equalsIgnoreCase(name)) return account;
            for (String alias : account.aliases) if (alias.equalsIgnoreCase(name)) return account;
        }
        return null;
    }
}