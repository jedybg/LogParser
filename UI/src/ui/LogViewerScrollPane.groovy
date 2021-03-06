package ui

import Log.Log

import javax.swing.*
import javax.swing.plaf.basic.BasicButtonUI
import javax.swing.text.DefaultCaret
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseListener

/**
 * Displays log file with scroll pane
 *
 * @author Leni Kirilov
 * @date 6/25/2014
 */
class LogViewerScrollPane extends JTabbedPane {

    private JScrollPane scrollPane;
    private Component parent;
    private Log logStream;
    private JTextArea textArea

    //condition for stopping the printer thread
    private running = true;

    LogViewerScrollPane(Component parent, Log log) {
        this.logStream = log
        this.parent = parent
        textArea = new JTextArea();
        textArea.setEditable false

        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        scrollPane = new JScrollPane(textArea)
        scrollPane.setHorizontalScrollBarPolicy JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        scrollPane.setVerticalScrollBarPolicy JScrollPane.VERTICAL_SCROLLBAR_ALWAYS

        TabButton closeButton = new TabButton();
        JLabel title = new JLabel(log.GetFileName())

        addTab "", scrollPane

        JPanel titlePanel = new JPanel();
        titlePanel.add title
        titlePanel.add closeButton
        setTabComponentAt(indexOfComponent(scrollPane), titlePanel)
        repaint()
        startPrintingData()
    }

    private void startPrintingData() {
        Thread.start {
            int indexTraversed = 0
            while (running) {
                def entities = logStream.GetEntities()
                if (indexTraversed <= entities.size() - 1) {
                    textArea.append(entities.get(indexTraversed++).GetContent() + "\n")
                    repaint()
                } else {
                    Thread.sleep(100)
                }
            }
        }
    }

    public void stopPrinting() {
        running = false;
    }

    private class TabButton extends JButton {
        public TabButton() {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("close this tab");
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    def container = LogViewerScrollPane.this
                    container.remove(scrollPane);
                    ((LogViewersPanel) container.parent).removeLogView LogViewerScrollPane.this
                }
            });
        }

        public void mouseClicked(MouseEvent e) {
            LogViewerScrollPane.this.remove scrollPane;
            ((LogViewersPanel) parent).removeLogView LogViewerScrollPane.this
        }

        //we don't want to update UI for this button
        public void updateUI() {}

        //paint the cross
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.BLACK);
            if (getModel().isRollover()) {
                g2.setColor(Color.MAGENTA);
            }
            int delta = 6;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }

    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };
}