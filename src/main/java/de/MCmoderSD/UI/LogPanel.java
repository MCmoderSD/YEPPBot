package de.MCmoderSD.UI;

import de.MCmoderSD.utilities.frontend.RoundedTextArea;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import static de.MCmoderSD.utilities.other.Calculate.*;
import static java.awt.Color.WHITE;

public class LogPanel extends JPanel {

    // Associations
    private final Frame frame;

    // Attributes
    private final RoundedTextArea logArea;

    // Constructor
    public LogPanel(Frame frame, Dimension size) {
        super();
        setLayout(null);
        setPreferredSize(size);
        setBackground(DARK);
        setForeground(PURPLE);
        setVisible(true);

        // Variables
        int fontSize = size.width / 50;
        int padding = size.width / 100;

        // Font
        Font font = new Font("Roboto", Font.PLAIN, fontSize);

        // Log Area
        logArea = new RoundedTextArea();
        logArea.setBounds(padding, padding, size.width - 2 * padding, size.height - 2 * padding);
        logArea.setBackground(LIGHT);
        logArea.setForeground(WHITE);
        logArea.setFont(font);
        add(logArea);

        // Add to Frame
        frame.add(this, BorderLayout.NORTH);
        frame.pack();
        this.frame = frame;
    }

    // Setter
    public void appendText(String type, String channel, String author, String message) {
        if (frame.getChannel().equals(channel) || frame.getChannel().length() < 3)
            logArea.appendText(type + " <" + channel + "> " + author + ": " + trimMessage(message));
    }
}