package de.MCmoderSD.utilities.other;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Reader {

    private final HashMap<String, ArrayList<String>> cache;

    public Reader() {
        cache = new HashMap<>();
    }

    // If the path is in the resources folder
    public ArrayList<String> lineRead(String path) {
        if (cache.containsKey(path)) return cache.get(path);
        ArrayList<String> lines = new ArrayList<>();

        try {
            InputStream inputStream = getClass().getResourceAsStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) lines.add(line);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        cache.put(path, lines);
        return lines;
    }

    // If the path is absolute
    public ArrayList<String> lineRead(String path, boolean isAbsolute) {
        if (cache.containsKey(path)) return cache.get(path);
        if (!isAbsolute) return lineRead(path);
        ArrayList<String> lines = new ArrayList<>();

        try (InputStream inputStream = new FileInputStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) lines.add(line);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        cache.put(path, lines);
        return lines;
    }
}