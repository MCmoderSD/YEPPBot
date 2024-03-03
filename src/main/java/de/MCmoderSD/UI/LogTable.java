package de.MCmoderSD.UI;

import de.MCmoderSD.utilities.database.MySQL;

import javax.swing.*;
import java.awt.*;

public class LogTable extends JPanel {

    // Constructor
    public LogTable(Frame frame, MySQL mySQL) {
        super();
        setLayout(null);
        setPreferredSize(new Dimension(1000, 600));
        setBackground(new Color(0x0e0e10));
        setForeground(new Color(0xa96cfa));
        setVisible(true);
        frame.add(this, BorderLayout.NORTH);
        frame.pack();

        // Table

    }

}
