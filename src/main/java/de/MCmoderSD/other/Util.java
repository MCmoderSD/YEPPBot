package de.MCmoderSD.other;

public class Util {

    public static boolean startsWith(String string, String... prefixes) {
        for (String prefix : prefixes) if (string.startsWith(prefix)) return true;
        return false;
    }

    public static boolean endsWith(String string, String... suffixes) {
        for (String suffix : suffixes) if (string.endsWith(suffix)) return true;
        return false;
    }

}