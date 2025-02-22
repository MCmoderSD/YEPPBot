package de.MCmoderSD.enums;

@SuppressWarnings("unused")
public enum SubTier {

    // Constants
    NONE(0),
    TIER1(1),
    TIER2(2),
    TIER3(3),
    PRIME(4);

    // Attributes
    private final int value;
    private final String name;

    // Constructor
    SubTier(int value) {
        this.value = value;
        this.name = switch (value) {
            case 1 -> "Tier 1";
            case 2 -> "Tier 2";
            case 3 -> "Tier 3";
            case 4 -> "Prime";
            default -> "None";
        };
    }

    // Get Value
    public int getValue() {
        return value;
    }

    // Get Name
    public String getName() {
        return name;
    }

    // Get Sub Tier
    public static SubTier getSubTier(int value) {
        return switch (value) {
            case 1 -> TIER1;
            case 2 -> TIER2;
            case 3 -> TIER3;
            case 4 -> PRIME;
            default -> NONE;
        };
    }

    // Get Sub Tier
    public static SubTier getSubTier(String name) {
        return switch (name.toLowerCase()) {
            case "tier 1" -> TIER1;
            case "tier 2" -> TIER2;
            case "tier 3" -> TIER3;
            case "prime" -> PRIME;
            default -> NONE;
        };
    }
}