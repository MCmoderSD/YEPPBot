package de.MCmoderSD.UI;

import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.imageloader.ImageLoader;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import java.io.IOException;

import java.net.URISyntaxException;

import static de.MCmoderSD.main.Main.VERSION;
import static de.MCmoderSD.utilities.other.Format.*;

public class Frame extends JFrame {

    // Associations
    private final MenuPanel menuPanel;
    private final LogPanel logPanel;

    // Constructor
    public Frame() {

        // Init Frame
        super("YEPPBot v" + VERSION);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Set Colors
        setBackground(DARK);
        setForeground(PURPLE);

        // Set Layout
        setLayout(new BorderLayout());

        // Icon
        try {
            setIconImage(ImageLoader.loadImage("/images/icon.png", false));
        } catch (IOException | URISyntaxException e) {
            System.err.println("Error while loading icon: " + e.getMessage());
        }

        // Variables
        var multiplier = 0.75f;
        var rawHeight = Math.round(getToolkit().getScreenSize().getHeight() * multiplier);
        var rawWidth = rawHeight * (4f / 3f);
        var height = Math.round(rawHeight);
        var width = Math.round(rawWidth);
        Dimension size = new Dimension(width, height);

        // Add Panel
        logPanel = new LogPanel(this, size);
        menuPanel = new MenuPanel(this, size);

        // Set Visible
        centerJFrame(this);
    }

    public static void centerJFrame(JFrame frame) {
        frame.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((dim.width - frame.getWidth()) / 2, (dim.height - frame.getHeight()) / 2);
    }

    // Setter
    public void log(String channel, String user, String message) {
        logPanel.appendText(channel, user, message);
        logPanel.scrollToBottom();
    }

    public void log(TwitchMessageEvent event) {
        logPanel.appendText(event.getChannel(), event.getUser(), event.getMessage());
    }

    public void clearLog() {
        logPanel.clear();
    }

    public void showMessage(String message, String title) {
        new Thread(() -> JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE)).start();
    }

    // Getter
    public String getChannel() {
        return menuPanel.getChannel();
    }
}