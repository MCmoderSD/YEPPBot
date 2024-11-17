package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.JavaAudioLibrary.AudioFile;
import de.MCmoderSD.utilities.database.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;

import static de.MCmoderSD.utilities.other.Format.*;
import static de.MCmoderSD.utilities.other.Util.readAllLines;

public class AssetManager {

    // Associations
    private final MySQL mySQL;

    // Constructor
    public AssetManager(MySQL mySQL) {

        // Set associations
        this.mySQL = mySQL;

        // Initialize
        initTables();
        initFacts();
        initInsults();
        initJokes();
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
                    factList (
                    fact_id INT PRIMARY KEY AUTO_INCREMENT,
                    en_percent varchar(500),
                    en_people varchar(500),
                    en_verb varchar(500),
                    en_frequency varchar(500),
                    en_adjective varchar(500),
                    en_object varchar(500),
                    de_percent varchar(500),
                    de_people varchar(500),
                    de_verb varchar(500),
                    de_frequency varchar(500),
                    de_adjective varchar(500),
                    de_object varchar(500)
                    )
                    """
            ).execute();

            // SQL statement for creating the insult list table
            connection.prepareStatement(condition +
                    """
                    insultList (
                    insult_id INT PRIMARY KEY AUTO_INCREMENT,
                    en varchar(500),
                    de varchar(500)
                    )
                    """
            ).execute();

            // SQL statement for creating the joke list table
            connection.prepareStatement(condition +
                    """
                    jokeList (
                    joke_id INT PRIMARY KEY AUTO_INCREMENT,
                    en varchar(500),
                    de varchar(500)
                    )
                    """
            ).execute();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void initFacts() {
        try {
            if (!mySQL.isConnected()) mySQL.connect();

            // Variables
            Connection connection = mySQL.getConnection();

            // Check if jokes are already in the database
            String countQuery = "SELECT COUNT(fact_id) FROM factList";
            PreparedStatement countStatement = connection.prepareStatement(countQuery);
            ResultSet countResult = countStatement.executeQuery();
            countResult.next();
            if (countResult.getInt(1) > 0) return;

            ArrayList<String> facts = readAllLines("/assets/factList.tsv");
            ArrayList<String> en_percent = new ArrayList<>();
            ArrayList<String> en_people = new ArrayList<>();
            ArrayList<String> en_verb = new ArrayList<>();
            ArrayList<String> en_frequency = new ArrayList<>();
            ArrayList<String> en_adjective = new ArrayList<>();
            ArrayList<String> en_object = new ArrayList<>();
            ArrayList<String> de_percent = new ArrayList<>();
            ArrayList<String> de_people = new ArrayList<>();
            ArrayList<String> de_verb = new ArrayList<>();
            ArrayList<String> de_frequency = new ArrayList<>();
            ArrayList<String> de_adjective = new ArrayList<>();
            ArrayList<String> de_object = new ArrayList<>();

            for (String fact : facts) {
                String[] factSplit = fact.split("\t");
                if (factSplit.length > 1 && !(factSplit[1].isEmpty() || factSplit[1].isBlank())) en_percent.add(factSplit[1]);
                if (factSplit.length > 2 && !(factSplit[2].isEmpty() || factSplit[2].isBlank())) en_people.add(factSplit[2]);
                if (factSplit.length > 3 && !(factSplit[3].isEmpty() || factSplit[3].isBlank())) en_verb.add(factSplit[3]);
                if (factSplit.length > 4 && !(factSplit[4].isEmpty() || factSplit[4].isBlank())) en_frequency.add(factSplit[4]);
                if (factSplit.length > 5 && !(factSplit[5].isEmpty() || factSplit[5].isBlank())) en_adjective.add(factSplit[5]);
                if (factSplit.length > 6 && !(factSplit[6].isEmpty() || factSplit[6].isBlank())) en_object.add(factSplit[6]);
                if (factSplit.length > 7 && !(factSplit[7].isEmpty() || factSplit[7].isBlank())) de_percent.add(factSplit[7]);
                if (factSplit.length > 8 && !(factSplit[8].isEmpty() || factSplit[8].isBlank())) de_people.add(factSplit[8]);
                if (factSplit.length > 9 && !(factSplit[9].isEmpty() || factSplit[9].isBlank())) de_verb.add(factSplit[9]);
                if (factSplit.length > 10 && !(factSplit[10].isEmpty() || factSplit[10].isBlank())) de_frequency.add(factSplit[10]);
                if (factSplit.length > 11 && !(factSplit[11].isEmpty() || factSplit[11].isBlank())) de_adjective.add(factSplit[11]);
                if (factSplit.length > 12 && !(factSplit[12].isEmpty() || factSplit[12].isBlank())) de_object.add(factSplit[12]);
            }

            for (var i = 0; i < facts.size(); i++) {
                String query = "INSERT INTO factList (fact_id) VALUES (?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, i + 1);
                preparedStatement.executeUpdate();
            }

            for (var i = 0; i < en_percent.size(); i++) {
                String query = "UPDATE factList SET en_percent = ? WHERE fact_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, en_percent.get(i));
                preparedStatement.setInt(2, i + 1);
                preparedStatement.executeUpdate();
            }

            for (var i = 0; i < en_people.size(); i++) {
                String query = "UPDATE factList SET en_people = ? WHERE fact_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, en_people.get(i));
                preparedStatement.setInt(2, i + 1);
                preparedStatement.executeUpdate();
            }

            for (var i = 0; i < en_verb.size(); i++) {
                String query = "UPDATE factList SET en_verb = ? WHERE fact_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, en_verb.get(i));
                preparedStatement.setInt(2, i + 1);
                preparedStatement.executeUpdate();
            }

            for (var i = 0; i < en_frequency.size(); i++) {
                String query = "UPDATE factList SET en_frequency = ? WHERE fact_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, en_frequency.get(i));
                preparedStatement.setInt(2, i + 1);
                preparedStatement.executeUpdate();
            }

            for (var i = 0; i < en_adjective.size(); i++) {
                String query = "UPDATE factList SET en_adjective = ? WHERE fact_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, en_adjective.get(i));
                preparedStatement.setInt(2, i + 1);
                preparedStatement.executeUpdate();
            }

            for (var i = 0; i < en_object.size(); i++) {
                String query = "UPDATE factList SET en_object = ? WHERE fact_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, en_object.get(i));
                preparedStatement.setInt(2, i + 1);
                preparedStatement.executeUpdate();
            }

            for (var i = 0; i < de_percent.size(); i++) {
                String query = "UPDATE factList SET de_percent = ? WHERE fact_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, de_percent.get(i));
                preparedStatement.setInt(2, i + 1);
                preparedStatement.executeUpdate();
            }

            for (var i = 0; i < de_people.size(); i++) {
                String query = "UPDATE factList SET de_people = ? WHERE fact_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, de_people.get(i));
                preparedStatement.setInt(2, i + 1);
                preparedStatement.executeUpdate();
            }

            for (var i = 0; i < de_verb.size(); i++) {
                String query = "UPDATE factList SET de_verb = ? WHERE fact_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, de_verb.get(i));
                preparedStatement.setInt(2, i + 1);
                preparedStatement.executeUpdate();
            }

            for (var i = 0; i < de_frequency.size(); i++) {
                String query = "UPDATE factList SET de_frequency = ? WHERE fact_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, de_frequency.get(i));
                preparedStatement.setInt(2, i + 1);
                preparedStatement.executeUpdate();
            }

            for (var i = 0; i < de_adjective.size(); i++) {
                String query = "UPDATE factList SET de_adjective = ? WHERE fact_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, de_adjective.get(i));
                preparedStatement.setInt(2, i + 1);
                preparedStatement.executeUpdate();
            }

            for (var i = 0; i < de_object.size(); i++) {
                String query = "UPDATE factList SET de_object = ? WHERE fact_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, de_object.get(i));
                preparedStatement.setInt(2, i + 1);
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void initInsults() {
        try {
            if (!mySQL.isConnected()) mySQL.connect();

            // Variables
            Connection connection = mySQL.getConnection();

            // Check if jokes are already in the database
            String countQuery = "SELECT COUNT(insult_id) FROM insultList";
            PreparedStatement countStatement = connection.prepareStatement(countQuery);
            ResultSet countResult = countStatement.executeQuery();
            countResult.next();
            if (countResult.getInt(1) > 0) return;

            ArrayList<String> insults =  readAllLines("/assets/insultList.tsv");
            ArrayList<String> en = new ArrayList<>();
            ArrayList<String> de = new ArrayList<>();

            for (String insult : insults) {
                String[] insultSplit = insult.split("\t");
                if (insultSplit.length > 1 && !(insultSplit[1].isEmpty() || insultSplit[1].isBlank())) en.add(insultSplit[1]);
                if (insultSplit.length > 2 && !(insultSplit[2].isEmpty() || insultSplit[2].isBlank())) de.add(insultSplit[2]);
            }

            for (var i = 0; i < insults.size(); i++) {
                String query = "INSERT INTO insultList (insult_id) VALUES (?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, i + 1);
                preparedStatement.executeUpdate();
            }

            for (var i = 0; i < en.size(); i++) {
                String query = "UPDATE insultList SET en = ? WHERE insult_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, en.get(i));
                preparedStatement.setInt(2, i + 1);
                preparedStatement.executeUpdate();
            }

            for (var i = 0; i < de.size(); i++) {
                String query = "UPDATE insultList SET de = ? WHERE insult_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, de.get(i));
                preparedStatement.setInt(2, i + 1);
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void initJokes() {
        try {
            if (!mySQL.isConnected()) mySQL.connect();

            // Variables
            Connection connection = mySQL.getConnection();

            // Check if jokes are already in the database
            String countQuery = "SELECT COUNT(joke_id) FROM jokeList";
            PreparedStatement countStatement = connection.prepareStatement(countQuery);
            ResultSet countResult = countStatement.executeQuery();
            countResult.next();
            if (countResult.getInt(1) > 0) return;

            ArrayList<String> jokes =  readAllLines("/assets/jokeList.tsv");
            ArrayList<String> en = new ArrayList<>();
            ArrayList<String> de = new ArrayList<>();

            for (String joke : jokes) {
                String[] jokeSplit = joke.split("\t");
                if (jokeSplit.length > 1 && !(jokeSplit[1].isEmpty() || jokeSplit[1].isBlank())) en.add(jokeSplit[1]);
                if (jokeSplit.length > 2 && !(jokeSplit[2].isEmpty() || jokeSplit[2].isBlank())) de.add(jokeSplit[2]);
            }

            for (var i = 0; i < jokes.size(); i++) {
                String query = "INSERT INTO jokeList (joke_id) VALUES (?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, i + 1);
                preparedStatement.executeUpdate();
            }

            for (var i = 0; i < en.size(); i++) {
                String query = "UPDATE jokeList SET en = ? WHERE joke_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, en.get(i));
                preparedStatement.setInt(2, i + 1);
                preparedStatement.executeUpdate();
            }

            for (var i = 0; i < de.size(); i++) {
                String query = "UPDATE jokeList SET de = ? WHERE joke_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, de.get(i));
                preparedStatement.setInt(2, i + 1);
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Get Fact
    public String getFact(String language) {

        // Set language
        String lang;
        switch (language) {
            case "en":
                lang = "en";
                break;
            case "de":
                lang = "de";
                break;
            default:
                return "Error: Invalid language";
        }

        StringBuilder fact = new StringBuilder();

        String[] parts = new String[] {"_percent", "_people", "_verb", "_frequency", "_adjective", "_object"};

        // Generate fact
        for (String part : parts) {
            try {
                if (!mySQL.isConnected()) mySQL.connect();

                Connection connection = mySQL.getConnection();

                // Get the total number of fact parts
                String countQuery = "SELECT COUNT(fact_id) FROM factList WHERE " + lang + part + " IS NOT NULL";
                PreparedStatement countStatement = connection.prepareStatement(countQuery);
                ResultSet countResult = countStatement.executeQuery();

                // Get the total number of fact parts
                countResult.next();
                var totalFacts = countResult.getInt(1);

                // Get the fact part at the random index
                String factQuery = "SELECT " + lang + part + " FROM factList WHERE fact_id = ?";
                PreparedStatement factStatement = connection.prepareStatement(factQuery);
                factStatement.setInt(1, (int) (totalFacts * Math.random() + 1));
                ResultSet factResult = factStatement.executeQuery();

                // Get the fact part
                factResult.next();
                fact.append(factResult.getString(lang + part)).append(" ");

                // Close resources
                factResult.close();

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return trimMessage(fact.toString());
    }

    // Get Insult
    public String getInsult(String language) {

        // Set language
        String lang;
        switch (language) {
            case "en":
                lang = "en";
                break;
            case "de":
                lang = "de";
                break;
            default:
                return "Error: Invalid language";
        }

        try {
            if (!mySQL.isConnected()) mySQL.connect();

            Connection connection = mySQL.getConnection();


            // Get the total number of insults
            String countQuery = "SELECT COUNT(insult_id) FROM insultList WHERE " + lang + " IS NOT NULL";
            PreparedStatement countStatement = connection.prepareStatement(countQuery);
            ResultSet countResult = countStatement.executeQuery();

            // Get the total number of insults
            countResult.next();
            var totalInsults = countResult.getInt(1);

            // Get the insult at the random index
            String insultQuery = "SELECT " + lang + " FROM insultList WHERE insult_id = ?";
            PreparedStatement insultStatement = connection.prepareStatement(insultQuery);
            insultStatement.setInt(1, (int) (totalInsults * Math.random() + 1));
            ResultSet insultResult = insultStatement.executeQuery();

            // Get the insult
            insultResult.next();
            return insultResult.getString(lang);

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return "Error: Database error";
    }

    // Get Joke
    public String getJoke(String language) {

        // Set language
        String lang;
        switch (language) {
            case "en":
                lang = "en";
                break;
            case "de":
                lang = "de";
                break;
            default:
                return "Error: Invalid language";
        }

        try {
            if (!mySQL.isConnected()) mySQL.connect();

            Connection connection = mySQL.getConnection();

            // Get the total number of jokes
            String countQuery = "SELECT COUNT(joke_id) FROM jokeList WHERE " + lang + " IS NOT NULL";
            PreparedStatement countStatement = connection.prepareStatement(countQuery);
            ResultSet countResult = countStatement.executeQuery();

            // Get the total number of jokes
            countResult.next();
            var totalJokes = countResult.getInt(1);

            // Get the joke at the random index
            String jokeQuery = "SELECT " + lang + " FROM jokeList WHERE joke_id = ?";
            PreparedStatement jokeStatement = connection.prepareStatement(jokeQuery);
            jokeStatement.setInt(1, (int) (totalJokes * Math.random() + 1));
            ResultSet jokeResult = jokeStatement.executeQuery();

            // Get the jokes
            jokeResult.next();
            return jokeResult.getString(lang);

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return "Error: Database error";
    }

    public AudioFile getTTSAudio(String input) {
        try {
            if (!mySQL.isConnected()) mySQL.connect();

            Connection connection = mySQL.getConnection();

            String query = "SELECT audioData FROM TTSLog WHERE message = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, input);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) return new AudioFile(resultSet.getBytes("audioData"));

            // Close resources
            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
}