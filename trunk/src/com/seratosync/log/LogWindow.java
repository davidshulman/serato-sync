package com.seratosync.log;

import com.seratosync.Main;
import com.seratosync.config.RuleFile;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Alekseenkov
 */
public class LogWindow extends JFrame {

    private JTextArea loggingArea;
    private Map<String, JLabel> ruleFileLabels = new HashMap<String, JLabel>();

    public LogWindow(String title, int width, int height) {
        super(title);
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);

        // set flexible grid bag layout
        getContentPane().setLayout(new GridBagLayout());

        // add logging component
        {
            loggingArea = new JTextArea();
            loggingArea.setSize(width, height);

            JScrollPane pane = new JScrollPane(loggingArea);
            pane.setAutoscrolls(true);
            pane.setSize(width, height);

            GridBagConstraints c = new GridBagConstraints();
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;

            getContentPane().add(pane, c);
        }

        setVisible(true);
    }

    /**
     * This method adds a rule file entry to the window
     *
     * @param fileName Rule file name
     * @param ruleFileState Rule file state
     */
    public void processRuleFileEvent(String fileName, int ruleFileState) {

        // in progress?
        if (ruleFileState == RuleFile.STATE_WAITING) {
            // add rule file label with 'in progress' icon
            GridBagConstraints c = new GridBagConstraints();
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.ipady = 15;

            JLabel label = new JLabel(fileName, loadImage("/images/waiting.gif", "Waiting to be processed"), JLabel.LEFT);
            getContentPane().add(label, c);

            ruleFileLabels.put(fileName, label);
        } else {

            JLabel label = ruleFileLabels.get(fileName);
            if (ruleFileState == RuleFile.STATE_IN_PROGRESS) {
                label.setIcon( loadImage("/images/in_progress.gif", "In progress...") );
            } else if (ruleFileState == RuleFile.STATE_COMPLETED) {
                label.setIcon( loadImage("/images/completed.png", "Successfully completed!") );
            } else {
                label.setIcon( loadImage("/images/failed.png", "Processing failed!") );
            }

        }
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     * Used to load icons for rule file processing (in progress, completed, failed)
     *
     * @param path Image path
     * @param description Image description
     * @return ImageIcon, or null if the path was invalid
     */
    private ImageIcon loadImage(String path, String description) {
        URL url = getClass().getResource(path);
        if (url != null) {
            return new ImageIcon(url, description);
        }
        return null;
    }

    /**
     * This method appends the data to the text area.
     *
     * @param data the Logging information data
     */
    public void showInfo(String data) {
        loggingArea.append(data);
        loggingArea.setCaretPosition(loggingArea.getText().length());
        getContentPane().validate();
    }
}

/**
 * @author Roman Alekseenkov
 */
class WindowHandler {

    // the window to which the logging is done
    private LogWindow window;

    // the singleton instance
    private static WindowHandler handler;

    /**
     * private constructor, preventing initialization
     */
    private WindowHandler() {
        window = new LogWindow("serato-sync version " + Main.VERSION, 650, 450);
    }

    /**
     * The getInstance method returns the singleton instance of the
     * WindowHandler object It is synchronized to prevent two threads trying to
     * create an instance simultaneously. @ return WindowHandler object
     *
     * @return window handler
     */
    public static synchronized WindowHandler getInstance() {
        if (handler == null) {
            handler = new WindowHandler();
        }
        return handler;
    }

    /**
     * This method writes the logging information to the associated
     * Java window. This method is synchronized to make it thread-safe.
     *
     * @param message The message to display
     */
    public synchronized void publish(String message) {
        window.showInfo(message + "\n");
    }

    /**
     * This method adds the rule file to the associated
     * Java window. This method is synchronized to make it thread-safe.
     *
     * @param fileName Rule file name
     * @param ruleFileState Rule file state
     */
    public synchronized void processRuleFileEvent(String fileName, int ruleFileState) {
        window.processRuleFileEvent(fileName, ruleFileState);
    }

    /**
     * Reports a fatal error
     */
    public void fatalError() {
        JOptionPane.showMessageDialog(window,
                "Error occured. Please inspect the main window with logs for details.",
                "Failure", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Reports success
     */
    public void success() {
        JOptionPane.showMessageDialog(window,
                "Sync process is completed successfully!",
                "Success!", JOptionPane.INFORMATION_MESSAGE);
    }

}