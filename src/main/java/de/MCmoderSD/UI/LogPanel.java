package de.MCmoderSD.UI;

import de.MCmoderSD.utilities.frontend.RoundedTextArea;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import static de.MCmoderSD.utilities.other.Format.*;

public class LogPanel extends JPanel {

    // Associations
    private final Frame frame;

    // Attributes
    private final RoundedTextArea logArea;

    // Constructor
    public LogPanel(Frame frame, Dimension size) {

        // Init Panel
        super();
        setLayout(null);
        setBackground(DARK);
        setForeground(PURPLE);

        // Set Frame
        this.frame = frame;

        // Set Size
        var height = Math.toIntExact(Math.round(size.height * 0.9));
        Dimension panelSize = new Dimension(size.width, height);
        setPreferredSize(panelSize);

        // Variables
        var fontSize = panelSize.width / 50;
        var padding = panelSize.width / 100;

        // Font
        Font font = new Font("Roboto", Font.PLAIN, fontSize);

        // Log Area
        logArea = new RoundedTextArea();
        logArea.setBounds(padding, padding, panelSize.width - 2 * padding, panelSize.height - 2 * padding);
        logArea.setBackground(LIGHT);
        logArea.setForeground(WHITE);
        logArea.setFont(font);
        add(logArea);

        // Add to Frame
        frame.add(this, BorderLayout.NORTH);
        frame.pack();
    }

    // Setter
    public void appendText(String type, String channel, String user, String message) {
        if (frame.getChannel().equals(channel) || frame.getChannel().length() < 3)
            logArea.appendText(type + " <" + channel + "> " + user + ": " + trimMessage(message));
    }
}