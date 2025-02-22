package de.MCmoderSD.enums;

@SuppressWarnings("unused")
public enum Social {

    // Constants
    INSTAGRAM("instagram", "insta"),
    TWITTER("twitter", "x"),
    YOUTUBE("youtube", "yt");

    // Attributes
    private final String name;
    private final String[] aliases;

    // Constructor
    Social(String name, String... aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    // Getters
    public String getName() {
        return name;
    }

    public static Social getSocial(String name) {
        for (Social social : values()) {
            if (social.getName().equalsIgnoreCase(name)) return social;
            for (String alias : social.aliases) if (alias.equalsIgnoreCase(name)) return social;
        }
        return null;
    }
}