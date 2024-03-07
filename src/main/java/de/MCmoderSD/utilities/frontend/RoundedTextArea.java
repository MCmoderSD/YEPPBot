package de.MCmoderSD.utilities.frontend;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static de.MCmoderSD.utilities.other.Calculate.BREAK;

public class RoundedTextArea extends JScrollPane {

    // Attributes
    private final JTextArea textArea;

    // Constructor
    public RoundedTextArea() {
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Add padding

        // Set the font and color of the JTextArea
        textArea.setFont(new Font("Roboto", Font.PLAIN, 20));
        textArea.setForeground(Color.WHITE);

        // Make the JTextArea non-editable
        textArea.setEditable(false);

        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        setViewportView(textArea);
        setOpaque(false);
        getViewport().setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());

        // Make the scroll bar invisible
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                scrollToBottom();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                scrollToBottom();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                scrollToBottom();
            }

            private void scrollToBottom() {
                JScrollBar vertical = getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }

    public void appendText(String text) {
        textArea.append(text + BREAK);
        scrollToBottom();
    }

    private void scrollToBottom() {
        JScrollBar vertical = getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
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
}