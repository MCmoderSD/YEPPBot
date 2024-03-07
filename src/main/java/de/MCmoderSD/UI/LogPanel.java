package de.MCmoderSD.UI;

import de.MCmoderSD.utilities.frontend.RoundedTextArea;

import javax.swing.*;
import java.awt.*;

import static de.MCmoderSD.utilities.other.Calculate.*;
import static java.awt.Color.WHITE;

public class LogPanel extends JPanel {

    // Associations
    private final Frame frame;

    // Attributes
    private final RoundedTextArea logArea;

    // Constructor
    public LogPanel(Frame frame) {
        super();
        setLayout(null);
        setPreferredSize(new Dimension(1000, 720));
        setBackground(DARK);
        setForeground(PURPLE);
        setVisible(true);

        Font font = new Font("Roboto", Font.PLAIN, 20);

        logArea = new RoundedTextArea();
        logArea.setBounds(10, 10, 980, 700);
        logArea.setBackground(LIGHT);
        logArea.setForeground(WHITE);
        logArea.setFont(font);
        add(logArea);

        frame.add(this, BorderLayout.NORTH);
        frame.pack();
        this.frame = frame;
    }

    // Setter
    public void appendText(String type, String channel, String author, String message) {
        if (frame.getChannel().equals(channel)) logArea.appendText("[" + type + "] <" + channel + "> " + author + ": " + message);
        if (frame.getChannel().length() < 3) logArea.appendText("[" + type + "] <" + channel + "> " + author + ": " + message);
    }
}