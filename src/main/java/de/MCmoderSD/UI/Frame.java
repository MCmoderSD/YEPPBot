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

        // Init Frame
        super("YEPPBot v" + Main.VERSION);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Set Colors
        setBackground(DARK);
        setForeground(PURPLE);

        // Set Layout
        setLayout(new BorderLayout());
        this.main = main;

        // Icon
        ImageReader imageReader = new ImageReader();
        setIconImage(imageReader.read("/images/icon.png"));

        // Variables
        var multiplyer = 0.75;
        var rawHeight = getToolkit().getScreenSize().getHeight() * multiplyer;
        var rawWidth = rawHeight * ((double) 4 / 3);
        var height = Math.toIntExact(Math.round(rawHeight));
        var width = Math.toIntExact(Math.round(rawWidth));
        Dimension size = new Dimension(width, height);

        // Add Panel
        logPanel = new LogPanel(this, size);
        menuPanel = new MenuPanel(this, size);

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