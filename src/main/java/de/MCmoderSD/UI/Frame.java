package de.MCmoderSD.UI;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.main.Main;

import de.MCmoderSD.utilities.image.ImageReader;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class Frame extends JFrame {

    // Associations
    private final Main main;
    private final MenuPanel menuPanel;
    private final LogPanel logPanel;

    // Constructor
    public Frame(Main main) {
        super();
        setTitle("YEPPBot v2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setBackground(DARK);
        setForeground(PURPLE);
        setLayout(new BorderLayout());
        this.main = main;

        // Icon
        ImageReader imageReader = new ImageReader();
        setIconImage(imageReader.read("/images/icon.png"));

        // Variables
        int width = 1000;
        var ratio = 0.8;

        // Add Panel
        menuPanel = new MenuPanel(this, new Dimension(width, Math.toIntExact(Math.round(width * ratio * 0.1))));
        logPanel = new LogPanel(this, new Dimension(width, Math.toIntExact(Math.round(width * ratio * 0.9))));

        // Set Visible
        pack();
        setLocation(centerJFrame(this));
        setVisible(true);
        requestFocusInWindow();
    }

    // Setter
    public void log(String type, String channel, String author, String message) {
        logPanel.appendText(type, channel, author, message);
    }

    // Getter
    public String getChannel() {
        return menuPanel.getChannel();
    }

    public BotClient getBotClient() {
        return main.getBotClient();
    }
}