package de.MCmoderSD.database.manager;

import de.MCmoderSD.database.Database;

public class BirthdayManager {

    // Associations
    private final Database database;

    // Constructor
    public BirthdayManager(Database database) {

        // Check Parameters
        if (database == null) throw new IllegalArgumentException("Database cannot be null");

        // Set Associations
        this.database = database;
    }
}