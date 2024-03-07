package de.MCmoderSD.UI;

import de.MCmoderSD.utilities.frontend.RoundedButton;
import de.MCmoderSD.utilities.frontend.RoundedTextField;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Stack;

import static de.MCmoderSD.utilities.other.Calculate.*;
import static java.awt.Color.WHITE;
import static java.awt.event.KeyEvent.*;

public class MenuPanel extends JPanel {

    // Associations
    private final Frame frame;

    // Attributes
    private final RoundedTextField channelField;
    private final RoundedTextField textField;

    // Variables
    private final Stack<String> messageHistory;
    private String lastMessage;
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
        messageHistory = new Stack<>();

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
        textField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                int keyCode = evt.getKeyCode();
                if (keyCode == VK_ENTER) sendMessage();
                if (keyCode == VK_ESCAPE) textField.setText("");
                if (keyCode == VK_TAB) channelField.requestFocus();

                // Message History
                if (keyCode == VK_UP) {
                    if (!messageHistory.isEmpty() && messageIndex > 0) {
                        messageIndex--;
                        textField.setText(messageHistory.get(messageIndex));
                    } else if (messageIndex == 0) {
                        textField.setText(lastMessage);
                    }
                }

                if (keyCode == VK_DOWN) {
                    if (!messageHistory.isEmpty() && messageIndex < messageHistory.size() - 1) {
                        messageIndex++;
                        textField.setText(messageHistory.get(messageIndex));
                    } else {
                        lastMessage = textField.getText();
                        textField.setText("");
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
        textField.setText("");
        messageHistory.push(message);
        messageIndex = messageHistory.size();
    }

    // Get Channel
    public String getChannel() {
        String channel = channelField.getText().replace(" ", "").toLowerCase();
        return channel.length() < 3 ? "" : channel.equalsIgnoreCase("channel") ? "" : channel;
    }
}