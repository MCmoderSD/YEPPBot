package de.MCmoderSD.UI;

import de.MCmoderSD.utilities.frontend.RoundedButton;
import de.MCmoderSD.utilities.frontend.RoundedTextField;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class MenuPanel extends JPanel {

    // Constructor
    public MenuPanel(Frame frame) {
        super();
        setLayout(null);
        setPreferredSize(new Dimension(1000, 80));
        setBackground(new Color(0x0e0e10));
        setForeground(new Color(0xa96cfa));
        setVisible(true);
        frame.add(this, BorderLayout.SOUTH);
        frame.pack();

        Font font = new Font("Roboto", Font.PLAIN, 20);

        // Channel Input
        RoundedTextField channelField = new RoundedTextField(1, "Channel");
        channelField.setBounds(10, 10, 180, 50);
        channelField.setFont(font);
        channelField.setHorizontalAlignment(JTextField.CENTER);
        channelField.setBackground(new Color(0x18181b));
        channelField.setBorder(new LineBorder(new Color(0x18181b), 5));
        add(channelField);

        // Text Input
        RoundedTextField textField = new RoundedTextField(1, "Message");
        textField.setBounds(200, 10, 650, 50);
        textField.setFont(font);
        textField.setBackground(new Color(0x18181b));
        textField.setBorder(new LineBorder(new Color(0x18181b), 5));
        add(textField);

        // Send Button
        RoundedButton sendButton = new RoundedButton("Send");
        sendButton.setBounds(frame.getWidth() - 155, 10, 130, 50);
        sendButton.setFont(font);
        sendButton.setBackground(new Color(0x771fe2));
        sendButton.setForeground(new Color(0xffffff));
        add(sendButton);

        frame.add(this);
        frame.pack();
    }
}
