package de.MCmoderSD.UI;

import de.MCmoderSD.utilities.Calculate;

import javax.swing.*;
import java.awt.*;

import static de.MCmoderSD.utilities.Calculate.centerJFrame;

public class Frame extends JFrame {

        // Constructor
        public Frame() {
            setTitle("MCmoderSD");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());
            setResizable(true);
            setBackground(new Color(0x0e0e10));
            setForeground(new Color(0xa96cfa));

            // Add Panel
            MenuPanel menuPanel = new MenuPanel(this);
            LogTable logTable = new LogTable(this, null);

            // Set Visible
            pack();
            setLocation(centerJFrame(this));
            setVisible(true);
        }

    public static void main(String[] args) {
        new Frame();
    }
}