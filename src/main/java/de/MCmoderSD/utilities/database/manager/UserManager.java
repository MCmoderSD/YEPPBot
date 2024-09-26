package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.utilities.database.MySQL;
import de.MCmoderSD.utilities.other.Calculate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.HashMap;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class UserManager {
/*
    // Assosiations
    private final MySQL mySQL;

    // Constructor
    public UserManager(MySQL mySQL) {

        // Set associations
        this.mySQL = mySQL;

        // Initialize
        initTables();
    }

    // Initialize Tables
    private void initTables() {
        try {

            // Variables
            Connection connection = mySQL.getConnection();

            // Condition for creating tables
            String condition = "CREATE TABLE IF NOT EXISTS ";

            // SQL statement for creating the fact list table
            connection.prepareStatement(condition +
                    """
                    UserData (
                        id INT PRIMARY KEY,
                        birthday DATE NOT NULL,
                        congratulated BIT NOT NULL DEFAULT 0,
                        FOREIGN KEY (id) REFERENCES users(id)
                    )
                    """
            ).execute();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Add or update user
    public void addBirthday(int id, Date birthday, boolean congratulated) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "INSERT INTO " + "UserData" + " (id, birthday, congratulated) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE birthday = ?, congratulated = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, id); // set id
            preparedStatement.setDate(2, birthday); // set birthday
            preparedStatement.setBoolean(3, congratulated); // set congratulated
            preparedStatement.setDate(4, birthday); // set birthday
            preparedStatement.setBoolean(5, congratulated); // set congratulated
            preparedStatement.executeUpdate(); // execute
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public HashMap<Integer, Date> getBirthday(Date date) {

        // Variables
        HashMap<Integer, Date> users = new HashMap<>();

        try {
            if (!mySQL.isConnected()) mySQL.connect();

            // Prepare statement
            String query = "SELECT id, birthday FROM " + "UserData" + " WHERE birthday = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setDate(1, date);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) users.put(resultSet.getInt("id"), convertToUTC(resultSet.getDate("birthday")));
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return users;
    }*/
}