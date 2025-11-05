package de.MCmoderSD.database.manager;

import de.MCmoderSD.database.Database;

import java.sql.Connection;

public class BirthdayManager {

    // Associations
    private final Database database;

    // Attributes
    private final Connection connection;

    // Constructor
    public BirthdayManager(Database database) {

        // Check Parameters
        if (database == null) throw new IllegalArgumentException("Database cannot be null");

        // Set Associations
        this.database = database;

        // Set Attributes
        connection = database.getConnection();
    }
}
