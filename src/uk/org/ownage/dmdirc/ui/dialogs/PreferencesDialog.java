/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package uk.org.ownage.dmdirc.ui.dialogs;

import java.util.Properties;
import javax.swing.JOptionPane;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.components.PreferencesInterface;
import uk.org.ownage.dmdirc.ui.components.PreferencesPanel;

/**
 * Allows the user to modify global client preferences.
 */
public final class PreferencesDialog implements PreferencesInterface {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    
    /** preferences panel. */
    private PreferencesPanel preferencesPanel;
    
    /**
     * Creates a new instance of PreferencesDialog.
     */
    public PreferencesDialog() {
        preferencesPanel = new PreferencesPanel(this);
        
        initComponents();
    }
    
    /**
     * Initialises GUI components.
     */
    private void initComponents() {
        
        initGeneralTab();
        
        initUITab();
        
        initTreeViewTab();
        
        initNotificationsTab();
        
        initInputTab();
        
        initLoggingTab();
        
        initAdvancedTab();
        
        preferencesPanel.display();
    }
    
    /**
     * Initialises the preferences tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initGeneralTab() {
        final String tabName = "General";
        preferencesPanel.addCategory(tabName);
        
        preferencesPanel.addOption(tabName, "general.closemessage", "Close message: ",
                PreferencesPanel.OptionType.TEXTFIELD, Config.getOption("general", "closemessage"));
        preferencesPanel.addOption(tabName, "general.partmessage", "Part message: ",
                PreferencesPanel.OptionType.TEXTFIELD, Config.getOption("general", "partmessage"));
        preferencesPanel.addOption(tabName, "general.quitmessage", "Quit message: ",
                PreferencesPanel.OptionType.TEXTFIELD, Config.getOption("general", "quitmessage"));
        preferencesPanel.addOption(tabName, "general.cyclemessage", "Cycle message: ",
                PreferencesPanel.OptionType.TEXTFIELD, Config.getOption("general", "cyclemessage"));
        preferencesPanel.addOption(tabName, "general.kickmessage", "Kick message: ",
                PreferencesPanel.OptionType.TEXTFIELD, Config.getOption("general", "kickmessage"));
    }
    
    /**
     * Initialises the UI tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initUITab() {
        final String tabName = "GUI";
        preferencesPanel.addCategory(tabName);
        
        preferencesPanel.addOption(tabName, "ui.maximisewindows", "Auto-Maximise windows: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("ui", "maximisewindows")));
        preferencesPanel.addOption(tabName, "ui.backgroundcolour", "Window background colour: ",
                PreferencesPanel.OptionType.COLOUR, Config.getOption("ui", "backgroundcolour"), true, false);
        preferencesPanel.addOption(tabName, "ui.foregroundcolour", "Window foreground colour: ",
                PreferencesPanel.OptionType.COLOUR, Config.getOption("ui", "foregroundcolour"), true, false);
        preferencesPanel.addOption(tabName, "ui.sortByMode", "Nicklist sort by mode: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("ui", "sortByMode")));
        preferencesPanel.addOption(tabName, "ui.sortByCase", "Nicklist sort by case: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("ui", "sortByCase")));
        preferencesPanel.addOption(tabName, "channel.splitusermodes", "Split user modes: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("channel", "splitusermodes")));
        preferencesPanel.addOption(tabName, "ui.quickCopy", "Quick Copy: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("ui", "quickCopy")));
        preferencesPanel.addOption(tabName, "ui.pasteProtectionLimit", "Paste protection trigger: ",
                PreferencesPanel.OptionType.SPINNER, Integer.parseInt(Config.getOption("ui", "pasteProtectionLimit")));
        preferencesPanel.addOption(tabName, "ui.awayindicator", "Away indicator: ", "Shows an away indicator in the input field.",
                PreferencesPanel.OptionType.CHECKBOX, Boolean.parseBoolean(Config.getOption("ui", "awayindicator")));
    }
    
    /**
     * Initialises the TreeView tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initTreeViewTab() {
        final String tabName = "Treeview";
        preferencesPanel.addCategory(tabName);
        
        preferencesPanel.addOption(tabName, "ui.rolloverEnabled", "Rollover enabled: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("ui", "rolloverEnabled")));
        preferencesPanel.addOption(tabName, "ui.rolloverColour", "Rollover colour: ",
                PreferencesPanel.OptionType.COLOUR, Config.getOption("ui", "rolloverColour"), true, true);
        preferencesPanel.addOption(tabName, "ui.sortwindows", "Sort windows: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("ui", "sortwindows")));
        preferencesPanel.addOption(tabName, "ui.sortservers", "Sort servers: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("ui", "sortservers")));
    }
    
    /**
     * Initialises the Notifications tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initNotificationsTab() {
        final String tabName = "Notifications";
        preferencesPanel.addCategory(tabName);
        final String[] windowOptions
                = new String[] {"all", "active", "server", };
        
        preferencesPanel.addOption(tabName, "notifications.socketClosed", "Socket closed: ",
                PreferencesPanel.OptionType.COMBOBOX, windowOptions, Config.getOption("notifications", "socketClosed"), false);
        preferencesPanel.addOption(tabName, "notifications.privateNotice", "Private notice: ",
                PreferencesPanel.OptionType.COMBOBOX, windowOptions, Config.getOption("notifications", "privateNotice"), false);
        preferencesPanel.addOption(tabName, "notifications.privateCTCP", "CTCP request: ",
                PreferencesPanel.OptionType.COMBOBOX, windowOptions, Config.getOption("notifications", "privateCTCP"), false);
        preferencesPanel.addOption(tabName, "notifications.privateCTCPreply", "CTCP reply: ",
                PreferencesPanel.OptionType.COMBOBOX, windowOptions, Config.getOption("notifications", "privateCTCPreply"), false);
    }
    
    /**
     * Initialises the input tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initInputTab() {
        final String tabName = "Input";
        preferencesPanel.addCategory(tabName);
        
        preferencesPanel.addOption(tabName, "general.commandchar", "Command character: ",
                PreferencesPanel.OptionType.TEXTFIELD, Config.getOption("general", "commandchar"));
        preferencesPanel.addOption(tabName, "tabcompletion.casesensitive",
                "Case-sensitive tab completion: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("tabcompletion", "casesensitive")));
    }
    
    /**
     * Initialises the logging tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initLoggingTab() {
        final String tabName = "Error Handling";
        preferencesPanel.addCategory(tabName);
        
        preferencesPanel.addOption(tabName, "general.autoSubmitErrors", "Automatically submit errors: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("general", "autoSubmitErrors")));
        preferencesPanel.addOption(tabName, "logging.dateFormat", "Date format: ",
                PreferencesPanel.OptionType.COMBOBOX, new String[]
        {"EEE, d MMM yyyy HH:mm:ss Z", "d MMM yyyy HH:mm:ss", }, Config.getOption("logging", "dateFormat"), true);
        preferencesPanel.addOption(tabName, "logging.programLogging", "Program logs: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("logging", "programLogging")));
        preferencesPanel.addOption(tabName, "logging.debugLogging", "Debug logs: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("logging", "debugLogging")));
        preferencesPanel.addOption(tabName, "logging.debugLoggingSysOut",
                "Debug console output: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("logging", "debugLoggingSysOut")));
    }
    
    /**
     * Initialises the advanced tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initAdvancedTab() {
        final String tabName = "Advanced";
        preferencesPanel.addCategory(tabName);
        
        final LookAndFeelInfo[] plaf = UIManager.getInstalledLookAndFeels();
        final String[] lafs = new String[plaf.length];
        int i = 0;
        for (LookAndFeelInfo laf : plaf) {
            lafs[i++] = laf.getName();
        }
        
        preferencesPanel.addOption(tabName, "ui.lookandfeel", "Look and feel: ",
                PreferencesPanel.OptionType.COMBOBOX, lafs, Config.getOption("ui", "lookandfeel"), false);
        preferencesPanel.addOption(tabName, "ui.showversion", "Show version: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("ui", "showversion")));
        preferencesPanel.addOption(tabName, "ui.inputbuffersize", "Input bufer size (lines): ",
                PreferencesPanel.OptionType.SPINNER,
                Integer.parseInt(Config.getOption("ui", "inputbuffersize")));
        preferencesPanel.addOption(tabName, "ui.frameBufferSize", "Frame buffer size (characters): ",
                PreferencesPanel.OptionType.SPINNER,
                Integer.parseInt(Config.getOption("ui", "frameBufferSize")));
        preferencesPanel.addOption(tabName, "general.browser", "Browser: ",
                PreferencesPanel.OptionType.TEXTFIELD,
                Config.getOption("general", "browser"));
    }
    
    /** {@inheritDoc}. */
    public void configClosed(final Properties properties) {
        /*if (properties.getProperty("ui.lookandfeel") != null
                && !Config.getOption("ui", "lookandfeel")
                    .equals(properties.getProperty("ui.lookandfeel)"))) {
            JOptionPane.showMessageDialog(MainFrame.getMainFrame(),
                    "The look and feel will not be changed until DMDirc is "
                    + "restarted", "Look and Feel", JOptionPane.OK_OPTION);
        }*/
        for (Object configOption : properties.keySet()) {
            String[] args = ((String) configOption).split("\\.");
            Config.setOption(args[0], args[1], (String) properties.get(configOption));
        }
    }
}
