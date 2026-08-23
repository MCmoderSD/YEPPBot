package de.MCmoderSD.data;

import java.io.Serializable;

public record TimerState(boolean running, int remaining) implements Serializable {

    // Constructor
    public TimerState {
        if (remaining < 0) remaining = 0;
    }

    // Format Remaining Seconds as hh:mm:ss
    public String formatted() {

        // Calculate time components
        var hours = remaining / 3600;
        var minutes = (remaining % 3600) / 60;
        var seconds = remaining % 60;

        // Return formatted string
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public String formatState() {
        if (running() && isExpired()) return "Der Timer ist abgelaufen. YEPP";
        if (running()) return "Der Timer läuft noch " + formatted() + " YEPP";
        if (isExpired()) return "Der Timer steht auf 00:00:00 YEPP";
        return "Der Timer ist pausiert bei " + formatted() + " YEPP";
    }

    // Check if the Timer has run out
    public boolean isExpired() {
        return remaining == 0;
    }
}