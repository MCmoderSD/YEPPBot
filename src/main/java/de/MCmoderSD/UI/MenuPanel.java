package de.MCmoderSD.UI;

import de.MCmoderSD.core.BotClient;
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

import static de.MCmoderSD.utilities.other.Calculate.*;
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
    public MenuPanel(Frame frame, Dimension size) {

        // Init Panel
        super();
        setLayout(null);
        setBackground(DARK);
        setForeground(PURPLE);

        // Set Size
        var height = Math.toIntExact(Math.round(size.height * 0.1));
        Dimension panelSize = new Dimension(size.width, height);
        setPreferredSize(panelSize);

        // Variables
        int fontSize = panelSize.width / 50;
        int padding = panelSize.width / 100;

        Font font = new Font("Roboto", Font.PLAIN, fontSize);
        messageHistory = new Stack<>();

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
        textField.setBounds(panelSize.width / 5, padding, Math.toIntExact(Math.round(panelSize.width / 1.6)) + 2 * padding, panelSize.height - 3 * padding);
        textField.setFont(font);
        textField.setBackground(LIGHT);
        textField.setBorder(new LineBorder(LIGHT, padding / 2));
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
        sendButton.setBounds(Math.toIntExact(Math.round(panelSize.width - panelSize.width / 6.25 + 2 * padding)), padding, Math.toIntExact(Math.round(panelSize.width / 6.25 - 4 * padding)), panelSize.height - 3 * padding);
        sendButton.setFont(font);
        sendButton.setBackground(PURPLE);
        sendButton.setForeground(WHITE);
        sendButton.addActionListener(e -> sendMessage());
        add(sendButton);

        frame.add(this, BorderLayout.SOUTH);
        frame.pack();
        this.frame = frame;
    }

    // Send Message
    private void sendMessage() {
        BotClient botClient = frame.getBotClient();

        String channel = getChannel();
        String message = trimMessage(textField.getText());

        if (getChannel().length() < 3) {
            new JOptionPane("Channel must be at least 3 characters long", JOptionPane.ERROR_MESSAGE).createDialog("Error").setVisible(true);
            return;
        }

        if (message.equalsIgnoreCase("message") || message.isEmpty()) {
            if (message.isEmpty()) new JOptionPane("Message must be at least 1 character long", JOptionPane.ERROR_MESSAGE).createDialog("Error").setVisible(true);
            botClient.joinChannel(channel);
            return;
        }

        botClient.write(channel, message);
        textField.setText("");
        messageHistory.push(message);
        messageIndex = messageHistory.size();
    }

    // Get Channel
    public String getChannel() {
        String channel = channelField.getText().replaceAll(" ", "").toLowerCase();
        return channel.length() < 3 ? "" : channel.equalsIgnoreCase("channel") ? "" : channel;
    }
}