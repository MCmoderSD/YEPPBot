package de.MCmoderSD.UI;

import javax.swing.*;
import java.awt.*;

public class Frame extends JFrame {

        // Constructor
        public Frame() {
            setTitle("MCmoderSD");
            setSize(800, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());
            setResizable(true);
            setVisible(true);

            // Add Panel

        }
}