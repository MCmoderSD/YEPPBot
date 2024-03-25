package de.MCmoderSD.utilities.other;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class Reader {

    // Methods
    public ArrayList<String> lineRead(String path) {
        ArrayList<String> lines = new ArrayList<>();

        try {
            InputStream inputStream = getClass().getResourceAsStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) lines.add(line);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return lines;
    }
}