package de.MCmoderSD.enums;

import de.MCmoderSD.utilities.other.Format;

public enum LoyaltyType {

    // Constants
    FOLLOW(Format.FOLLOW),
    SUBSCRIPTION(Format.SUBSCRIBE),
    GIFT(Format.GIFT);


    // Attributes
    private final String tag;

    // Constructor
    LoyaltyType(String tag) {
        this.tag = tag;
    }

    // Get Tag
    public String getTag() {
        return tag;
    }
}
