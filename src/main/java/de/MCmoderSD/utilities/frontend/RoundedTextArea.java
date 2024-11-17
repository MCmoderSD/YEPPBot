package de.MCmoderSD.utilities.frontend;

import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.BorderFactory;
import javax.swing.JScrollBar;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.geom.RoundRectangle2D;

import static de.MCmoderSD.utilities.other.Format.*;

public class RoundedTextArea extends JScrollPane {

    // Attributes
    private final JTextArea textArea;

    // Variables
    private boolean autoScroll = true;

    // Constructor
    public RoundedTextArea() {
        
        // Init JTextArea
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Add padding

        // Set the font and color of the JTextArea
        textArea.setFont(new Font("Roboto", Font.PLAIN, 20));
        textArea.setForeground(WHITE);

        // Make the JTextArea non-editable
        textArea.setEditable(false);

        setViewportView(textArea);
        setOpaque(false);
        getViewport().setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());

        // Disable visible scrollbars
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Get the vertical scrollbar
        JScrollBar verticalScrollBar = getVerticalScrollBar();

        // Minimize the size
        verticalScrollBar.setPreferredSize(new Dimension(0, 0));

        // Make the scrollbar invisible
        verticalScrollBar.setUI(new BasicScrollBarUI() {

            @Override
            protected void configureScrollBarColors() {

                // Make scrollbar invisible
                this.thumbColor = new Color(0, 0, 0, 0);
                this.trackColor = new Color(0, 0, 0, 0);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createInvisibleButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createInvisibleButton();
            }

            private JButton createInvisibleButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });

        // Listen to document changes to trigger auto-scroll
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (autoScroll) scrollToBottom();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (autoScroll) scrollToBottom();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (autoScroll) scrollToBottom();
            }
        });

        // Listen to manual scroll actions to disable auto-scroll
        verticalScrollBar.addAdjustmentListener(new AdjustmentListener() {

            // Variables
            private int previousValue = -1;

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {

                // Check if the scrollbar is at the bottom
                var value = verticalScrollBar.getValue();
                var max = verticalScrollBar.getMaximum() - verticalScrollBar.getVisibleAmount();

                if (value == max) autoScroll = true; // Re-enable auto-scroll when fully scrolled down
                else if (value != previousValue) autoScroll = false; // Disable auto-scroll on manual interaction
                
                previousValue = value;
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
        super.paintComponent(g2);
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
        g2.dispose();
    }

    // Setter
    public void appendText(String text) {
        textArea.append(text + BREAK);
        if (autoScroll) scrollToBottom();
    }

    public void scrollToBottom() {
        JScrollBar vertical = getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    public void clear() {
        textArea.setText("");
    }
}