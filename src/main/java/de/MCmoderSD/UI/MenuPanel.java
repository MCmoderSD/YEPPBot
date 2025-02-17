package de.MCmoderSD.UI;

import de.MCmoderSD.main.Main;
import de.MCmoderSD.utilities.frontend.RoundedButton;
import de.MCmoderSD.utilities.frontend.RoundedTextField;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.util.Stack;

import static de.MCmoderSD.utilities.other.Format.*;
import static java.awt.event.KeyEvent.*;

public class MenuPanel extends JPanel {

    // Associations
    private final Frame frame;

    // Attributes
    private final RoundedTextField channelField;
    private final RoundedTextField textField;

    // Variables
    private final Stack<String> messageHistory;
    private final Stack<String> channelHistory;
    private String lastMessage;
    private String lastChannel;
    private int messageIndex;
    private int channelIndex;

    // Constructor
    public MenuPanel(Frame frame, Dimension size) {

        // Init Panel
        super();
        setLayout(null);
        setBackground(DARK);
        setForeground(PURPLE);

        // Set Frame
        this.frame = frame;

        // Set Size
        var height = Math.round(size.height * 0.1f);
        Dimension panelSize = new Dimension(size.width, height);
        setPreferredSize(panelSize);

        // Variables
        var fontSize = panelSize.width / 50;
        var padding = panelSize.width / 100;

        Font font = new Font("Roboto", Font.PLAIN, fontSize);
        messageHistory = new Stack<>();
        channelHistory = new Stack<>();

        // Channel Input
        channelField = new RoundedTextField(1, "Channel");
        channelField.setBounds(padding, padding, panelSize.width / 5 - 2 * padding, panelSize.height - 3 * padding);
        channelField.setFont(font);
        channelField.setHorizontalAlignment(JTextField.CENTER);
        channelField.setBackground(LIGHT);
        channelField.setBorder(new LineBorder(LIGHT, padding / 2));
        add(channelField);

        // Text Input
        textField = new RoundedTextField(1, "Message");
        textField.setBounds(panelSize.width / 5, padding, Math.round(panelSize.width / 1.6f) + 2 * padding, panelSize.height - 3 * padding);
        textField.setFont(font);
        textField.setBackground(LIGHT);
        textField.setBorder(new LineBorder(LIGHT, padding / 2));
        add(textField);

        // Message History
        textField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                var keyCode = evt.getKeyCode();
                if (keyCode == VK_ENTER) sendMessage();
                if (keyCode == VK_ESCAPE) textField.setText(EMPTY);
                if (keyCode == VK_TAB) channelField.requestFocus();

                // Message History
                if (keyCode == VK_UP) {
                    if (!messageHistory.isEmpty() && messageIndex > 0) {
                        messageIndex--;
                        textField.setText(messageHistory.get(messageIndex));
                    } else if (messageIndex == 0) textField.setText(lastMessage);
                }

                if (keyCode == VK_DOWN) {
                    if (!messageHistory.isEmpty() && messageIndex < messageHistory.size() - 1) {
                        messageIndex++;
                        textField.setText(messageHistory.get(messageIndex));
                    } else {
                        lastMessage = textField.getText();
                        textField.setText(EMPTY);
                    }
                }
            }
        });

        // Channel History
        channelField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                var keyCode = evt.getKeyCode();
                if (keyCode == VK_ENTER) joinChannel();
                if (keyCode == VK_ESCAPE) channelField.setText(EMPTY);
                if (keyCode == VK_TAB) textField.requestFocus();

                // Channel History
                if (keyCode == VK_UP) {
                    if (!channelHistory.isEmpty() && channelIndex > 0) {
                        channelIndex--;
                        channelField.setText(channelHistory.get(channelIndex));
                    } else if (channelIndex == 0) channelField.setText(lastChannel);
                }

                if (keyCode == VK_DOWN) {
                    if (!channelHistory.isEmpty() && channelIndex < channelHistory.size() - 1) {
                        channelIndex++;
                        channelField.setText(channelHistory.get(channelIndex));
                    } else {
                        lastChannel = channelField.getText();
                        channelField.setText(EMPTY);
                    }
                }
            }
        });

        // Send Button
        RoundedButton sendButton = new RoundedButton("Send");
        sendButton.setBounds(Math.round(panelSize.width - panelSize.width / 6.25f + 2f * padding), padding, Math.round(panelSize.width / 6.25f - 4f * padding), panelSize.height - 3 * padding);
        sendButton.setFont(font);
        sendButton.setBackground(PURPLE);
        sendButton.setForeground(WHITE);
        sendButton.addActionListener(e -> sendMessage());
        add(sendButton);

        frame.add(this, BorderLayout.SOUTH);
        frame.pack();
    }

    private void joinChannel() {
        String channel = getChannel();
        if (channel.length() < 4) {
            new JOptionPane("Channel must be at least 4 characters long", JOptionPane.ERROR_MESSAGE).createDialog("Error").setVisible(true);
            return;
        }
        Main.botClient.joinChannel(channel);
        channelHistory.push(channel);
        channelIndex = channelHistory.size();
    }

    // Send Message
    private void sendMessage() {

        // Variables
        String channel = getChannel();
        String message = trimMessage(textField.getText());

        // Check Channel
        if (getChannel().length() < 4) {
            new JOptionPane("Channel must be at least 4 characters long", JOptionPane.ERROR_MESSAGE).createDialog("Error").setVisible(true);
            return;
        }

        // Slash Commands
        if (message.startsWith("/")) {
            switch (message.toLowerCase()) {
                case "/clear" -> frame.clearLog();
                case "/exit" -> System.exit(0);
                default -> new JOptionPane("Unknown command: " + message, JOptionPane.ERROR_MESSAGE).createDialog("Error").setVisible(true);
            }

            // Clean Up
            cleanUpTextFields(message);
            return;
        }

        // Check Message
        if (message.equalsIgnoreCase("message") || message.isEmpty()) {
            if (message.isEmpty()) new JOptionPane("Message must be at least 1 character long", JOptionPane.ERROR_MESSAGE).createDialog("Error").setVisible(true);
            Main.botClient.joinChannel(channel);
            channelHistory.push(channel);
            return;
        }

        // Channel History
        if (!channelHistory.contains(channel)) {
            channelHistory.push(channel);
            channelIndex = channelHistory.size();
        }

        // Filter Message
        message = message.replaceAll(TAB, SPACE).replaceAll(BREAK, SPACE).replaceAll("\r", SPACE);
        while (message.contains("  ")) message = message.replaceAll(" {2}", SPACE);

        // Send Message
        Main.botClient.write(channel, message);

        // Clean Up
        cleanUpTextFields(message);
    }

    // Clean Up Text Fields
    private void cleanUpTextFields(String message) {
        textField.setText(EMPTY);
        messageHistory.push(message);
        messageIndex = messageHistory.size();
    }

    // Get Channel
    public String getChannel() {
        String channel = channelField.getText().replaceAll(SPACE, EMPTY).toLowerCase();
        return channel.length() < 4 ? EMPTY : channel.equalsIgnoreCase("channel") ? EMPTY : channel;
    }
}