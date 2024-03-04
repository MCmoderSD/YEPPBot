package de.MCmoderSD.UI;

import de.MCmoderSD.utilities.frontend.RoundedButton;
import de.MCmoderSD.utilities.frontend.RoundedTextField;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;

import static de.MCmoderSD.utilities.Calculate.*;
import static java.awt.Color.WHITE;

public class MenuPanel extends JPanel {

    // Associations
    private final Frame frame;

    // Attributes
    private final RoundedTextField channelField;
    private final RoundedTextField textField;

    // Variables
    private final ArrayList<String> messages;
    private int messageIndex;

    // Constructor
    public MenuPanel(Frame frame) {
        super();
        setLayout(null);
        setPreferredSize(new Dimension(1000, 80));
        setBackground(DARK);
        setForeground(PURPLE);
        setVisible(true);
        frame.add(this, BorderLayout.SOUTH);
        frame.pack();
        this.frame = frame;

        Font font = new Font("Roboto", Font.PLAIN, 20);
        messages = new ArrayList<>();

        // Channel Input
        channelField = new RoundedTextField(1, "Channel");
        channelField.setBounds(10, 10, 180, 50);
        channelField.setFont(font);
        channelField.setHorizontalAlignment(JTextField.CENTER);
        channelField.setBackground(LIGHT);
        channelField.setBorder(new LineBorder(LIGHT, 5));
        add(channelField);

        // Text Input
        textField = new RoundedTextField(1, "Message");
        textField.setBounds(200, 10, 650, 50);
        textField.setFont(font);
        textField.setBackground(LIGHT);
        textField.setBorder(new LineBorder(LIGHT, 5));
        add(textField);

        // Key Listener
        textField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) sendMessage();
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) textField.setText("");
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_TAB) channelField.requestFocus();

                // Message History
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_UP) {
                    if (messageIndex > 0) {
                        messageIndex--;
                        textField.setText(messages.get(messageIndex));
                    }
                }

                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
                    if (messageIndex < messages.size() - 1) {
                        messageIndex++;
                        textField.setText(messages.get(messageIndex));
                    }
                }

            }
        });

        // Send Button
        RoundedButton sendButton = new RoundedButton("Send");
        sendButton.setBounds(frame.getWidth() - 155, 10, 130, 50);
        sendButton.setFont(font);
        sendButton.setBackground(PURPLE);
        sendButton.setForeground(WHITE);
        sendButton.addActionListener(e -> sendMessage());
        add(sendButton);

        frame.add(this);
        frame.pack();
    }

    // Send Message
    private void sendMessage() {
        String channel = channelField.getText().replace(" ", "").toLowerCase();
        String message = textField.getText();
        while (message.startsWith(" ")) message = message.substring(1);
        while (message.endsWith(" ")) message = message.substring(0, message.length() - 1);

        if (getChannel().length() < 3) {
            new JOptionPane("Channel must be at least 3 characters long", JOptionPane.ERROR_MESSAGE).createDialog("Error").setVisible(true);
            return;
        }

        if (message.isEmpty()) {
            new JOptionPane("Message must be at least 1 character long", JOptionPane.ERROR_MESSAGE).createDialog("Error").setVisible(true);
            return;
        }
        frame.getBotClient().sendMessage(channel, message);
        messages.add(message);
        textField.setText("");
    }

    // Get Channel
    public String getChannel() {
        String channel = channelField.getText().replace(" ", "").toLowerCase();
        return channel.length() < 3 ? "" : channel.equalsIgnoreCase("channel") ? "" : channel;
    }
}