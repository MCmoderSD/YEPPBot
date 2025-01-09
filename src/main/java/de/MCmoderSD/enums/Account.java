package de.MCmoderSD.enums;

public enum Account {

    // Games
    APEX("apex"),
    LEAGUE("league", "lol"),
    RAINBOW("rainbow", "siege"),
    VALORANT("valorant", "valo"),

    // Social Media
    INSTAGRAM("instagram", "insta"),
    TIKTOK("tiktok", "tt"),
    TWITTER("twitter", "x"),
    YOUTUBE("youtube", "yt");

    // Attributes
    private final String name;
    private final String[] aliases;

    // Constructor
    Account(String name, String... aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    // Getters
    public String getName() {
        return name;
    }

    public static Account getAccount(String name) {
        for (Account account : values()) {
            if (account.getName().equalsIgnoreCase(name)) return account;
            for (String alias : account.aliases) if (alias.equalsIgnoreCase(name)) return account;
        }
        return null;
    }
}