package de.MCmoderSD.enums;

public enum Game {

    // Attributes
    private final String name;
    private final String[] aliases;

    // Constructor
    Game(String name, String... aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    // Getters
    public String getName() {
        return name;
    }

    public static Game getGame(String name) {
        for (Game game : values()) {
            if (game.getName().equalsIgnoreCase(name)) return game;
            for (String alias : game.aliases) if (alias.equalsIgnoreCase(name)) return game;
        }
        return null;
    }
}