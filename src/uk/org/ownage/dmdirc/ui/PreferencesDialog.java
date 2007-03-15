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

package uk.org.ownage.dmdirc.ui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import uk.org.ownage.dmdirc.Config;

/**
 * Allows the user to modify global client preferences.
 */
public final class PreferencesDialog extends StandardDialog
        implements ActionListener, ListSelectionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    
    /** Size of the large borders in the dialog. */
    private static final int LARGE_BORDER = 10;
    
    /** Size of the small borders in the dialog. */
    private static final int SMALL_BORDER = 5;
    
    private static enum optionType { TEXTFIELD, CHECKBOX, COMBOBOX, };
    
    private Hashtable<String, JTextField> textFields;
    
    private Hashtable<String, JCheckBox> checkBoxes;
    
    private Hashtable<String, JComboBox> comboBoxes;
    
    private JList tabList;
    
    private CardLayout cardLayout;
    
    private JPanel mainPanel;
    
    /**
     * Creates a new instance of PreferencesDialog.
     * @param parent The frame that owns this dialog
     * @param modal Whether to show modally or not
     */
    public PreferencesDialog(final Frame parent, final boolean modal) {
        super(parent, modal);
        
        textFields = new Hashtable<String, JTextField>();
        checkBoxes = new Hashtable<String, JCheckBox>();
        comboBoxes = new Hashtable<String, JComboBox>();
        
        initComponents();
    }
    
    /**
     * Initialises GUI components.
     */
    private void initComponents() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        final GridBagConstraints constraints = new GridBagConstraints();
        final JButton button1 = new JButton();
        final JButton button2 = new JButton();
        tabList = new JList(new DefaultListModel());
        tabList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabList.addListSelectionListener(this);
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());
        setTitle("Preferences");
        setResizable(true);
        
        
        button1.setPreferredSize(new Dimension(100, 25));
        button2.setPreferredSize(new Dimension(100, 25));
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridheight = 2;
        constraints.insets = new Insets(SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER);
        getContentPane().add(tabList, constraints);
        
        constraints.gridheight = 1;
        constraints.gridwidth = 2;
        constraints.gridx = 1;
        getContentPane().add(mainPanel, constraints);
        
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        getContentPane().add(Box.createHorizontalGlue(), constraints);

        constraints.weightx = 0.0;
        constraints.insets.set(0, 0, LARGE_BORDER, LARGE_BORDER);
        constraints.gridx = 2;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        getContentPane().add(button1, constraints);
        
        constraints.gridx = 3;
        getContentPane().add(button2, constraints);
        
        mainPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                SMALL_BORDER, LARGE_BORDER));
        
        orderButtons(button1, button2);
        
        initGeneralTab(mainPanel);
        
        initUITab(mainPanel);
        
        initTreeViewTab(mainPanel);
        
        initNotificationsTab(mainPanel);
        
        initInputTab(mainPanel);
        
        initLoggingTab(mainPanel);
        
        initIdentitiesTab(mainPanel);
        
        initAdvancedTab(mainPanel);
        
        initListeners();
        
        setPreferredSize(new Dimension(620,400));
        
        pack();
    }
    
    /**
     * Adds an option of the specified type to the specified panel.
     */
    private void addComponent(final JPanel parent, final String optionName,
            final String title, final optionType type) {
        final String[] windowOptions
                = new String[] {"all", "active", "server", };
        JComponent option;
        final String[] configArgs = optionName.split("\\.");
        final String configValue =
                Config.getOption(configArgs[0], configArgs[1]);
        JLabel label = new JLabel(title, JLabel.TRAILING);
        
        parent.add(label);
        switch (type) {
            case TEXTFIELD:
                option = new JTextField();
                ((JTextField) option).setText(configValue);
                textFields.put(optionName, (JTextField) option);
                break;
            case CHECKBOX:
                option = new JCheckBox();
                ((JCheckBox) option).
                        setSelected(Boolean.parseBoolean(configValue));
                checkBoxes.put(optionName, (JCheckBox) option);
                break;
            case COMBOBOX:
                option = new JComboBox(windowOptions);
                ((JComboBox) option).setSelectedItem(configValue);
                comboBoxes.put(optionName, (JComboBox) option);
                break;
            default:
                throw new IllegalArgumentException(type
                        + " is not a valid option");
        }
        label.setLabelFor(option);
        parent.add(option);
    }
    
    /**
     * Initialises the preferences tab.
     *
     * @param tabbedPane parent pane
     */
    private void initGeneralTab(final JPanel cardLayoutPanel) {
        final JPanel generalPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        final JPanel panel;
        
        generalPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
        
        cardLayoutPanel.add(generalPanel, "General");
        ((DefaultListModel) tabList.getModel()).addElement("General");
        
        panel = new JPanel(new SpringLayout());
        
        addComponent(panel, "general.closemessage", "Close message: ",
                optionType.TEXTFIELD);
        addComponent(panel, "general.partmessage", "Part message: ",
                optionType.TEXTFIELD);
        addComponent(panel, "general.quitmessage", "Quit message: ",
                optionType.TEXTFIELD);
        addComponent(panel, "general.cyclemessage", "Cycle message: ",
                optionType.TEXTFIELD);
        
        layoutGrid(panel, 4, 2, SMALL_BORDER, SMALL_BORDER,
                LARGE_BORDER, LARGE_BORDER);
        
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        generalPanel.add(panel, constraints);
    }
    
    /**
     * Initialises the UI tab.
     *
     * @param tabbedPane parent pane
     */
    private void initUITab(final JPanel cardLayoutPanel) {
        final JPanel uiPanel = new JPanel();
        final GridBagConstraints constraints = new GridBagConstraints();
        JPanel panel;
        JLabel label;
        JTextField textField;
        JCheckBox checkBox;
        JComboBox comboBox;
        
        cardLayoutPanel.add(uiPanel, "GUI");
        ((DefaultListModel) tabList.getModel()).addElement("GUI");
        
        uiPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
        
        panel = new JPanel(new SpringLayout());
        
        addComponent(panel, "ui.showversion", "Show version: ",
                optionType.CHECKBOX);
        addComponent(panel, "ui.inputbuffersize", "Input bufer size: ",
                optionType.TEXTFIELD);
        addComponent(panel, "ui.maximisewindows", "Auto-Maximise windows: ",
                optionType.CHECKBOX);
        addComponent(panel, "ui.backgroundcolour", "Window background colour: ",
                optionType.TEXTFIELD);
        addComponent(panel, "ui.foregroundcolour", "Window foreground colour: ",
                optionType.TEXTFIELD);
        addComponent(panel, "ui.sortByMode", "Nicklist sort by mode: ",
                optionType.CHECKBOX);
        addComponent(panel, "ui.sortByCase", "Nicklist sort by case: ",
                optionType.CHECKBOX);
        addComponent(panel, "channel.splitusermodes", "Split user modes: ",
                optionType.CHECKBOX);
        
        layoutGrid(panel, 8, 2, SMALL_BORDER, SMALL_BORDER,
                LARGE_BORDER, LARGE_BORDER);
        
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        uiPanel.add(panel, constraints);
    }
    
    /**
     * Initialises the TreeView tab.
     *
     * @param tabbedPane parent pane
     */
    private void initTreeViewTab(final JPanel cardLayoutPanel) {
        final JPanel treeViewPanel = new JPanel();
        final GridBagConstraints constraints = new GridBagConstraints();
        JPanel panel;
        
        cardLayoutPanel.add(treeViewPanel, "Treeview");
        ((DefaultListModel) tabList.getModel()).addElement("Treeview");
        
        treeViewPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
        
        panel = new JPanel(new SpringLayout());
        
        addComponent(panel, "ui.rolloverEnabled", "Rollover enabled: ",
                optionType.CHECKBOX);
        addComponent(panel, "ui.rolloverColour", "Rollover colour: ",
                optionType.TEXTFIELD);
        addComponent(panel, "ui.sortwindows", "Sort windows: ",
                optionType.CHECKBOX);
        addComponent(panel, "ui.sortservers", "Sort servers: ",
                optionType.CHECKBOX);
        
        layoutGrid(panel, 4, 2, SMALL_BORDER, SMALL_BORDER,
                LARGE_BORDER, LARGE_BORDER);
        
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        treeViewPanel.add(panel, constraints);
        
    }
    
    /**
     * Initialises the Notifications tab.
     *
     * @param tabbedPane parent pane
     */
    private void initNotificationsTab(final JPanel cardLayoutPanel) {
        final JPanel notificationsPanel = new JPanel();
        final GridBagConstraints constraints = new GridBagConstraints();
        JPanel panel;
        
        cardLayoutPanel.add(notificationsPanel, "Notifications");
        ((DefaultListModel) tabList.getModel()).addElement("Notifications");
        
        notificationsPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
        
        panel = new JPanel(new SpringLayout());
        
        addComponent(panel, "notifications.socketClosed", "Socket closed: ",
                optionType.COMBOBOX);
        addComponent(panel, "notifications.privateNotice", "Private notice: ",
                optionType.COMBOBOX);
        addComponent(panel, "notifications.privateCTCP", "CTCP request: ",
                optionType.COMBOBOX);
        addComponent(panel, "notifications.privateCTCPreply", "CTCP reply: ",
                optionType.COMBOBOX);
        
        layoutGrid(panel, 4, 2, SMALL_BORDER, SMALL_BORDER,
                LARGE_BORDER, LARGE_BORDER);
        
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        notificationsPanel.add(panel, constraints);
    }
    
    /**
     * Initialises the input tab.
     *
     * @param tabbedPane parent pane
     */
    private void initInputTab(final JPanel cardLayoutPanel) {
        final JPanel inputPanel = new JPanel();
        final GridBagConstraints constraints = new GridBagConstraints();
        JPanel panel;
        
        cardLayoutPanel.add(inputPanel, "Input");
        ((DefaultListModel) tabList.getModel()).addElement("Input");
        
        inputPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
        
        panel = new JPanel(new SpringLayout());
        
        addComponent(panel, "general.commandchar", "Command character: ",
                optionType.TEXTFIELD);
        addComponent(panel, "tabcompletion.casesensitive",
                "Case-sensitive tab completion: ", optionType.CHECKBOX);
        
        layoutGrid(panel, 2, 2, SMALL_BORDER, SMALL_BORDER,
                LARGE_BORDER, LARGE_BORDER);
        
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        inputPanel.add(panel, constraints);
    }
    
    /**
     * Initialises the logging tab.
     *
     * @param tabbedPane parent pane
     */
    private void initLoggingTab(final JPanel cardLayoutPanel) {
        final JPanel loggingPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        JPanel panel;
        
        cardLayoutPanel.add(loggingPanel, "Logging");
        ((DefaultListModel) tabList.getModel()).addElement("Logging");
        
        loggingPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
        
        panel = new JPanel(new SpringLayout());
        
        addComponent(panel, "logging.dateFormat", "Date format: ",
                optionType.TEXTFIELD);
        addComponent(panel, "logging.programLogging", "Program logs: ",
                optionType.CHECKBOX);
        addComponent(panel, "logging.debugLogging", "Debug logs: ",
                optionType.CHECKBOX);
        addComponent(panel, "logging.debugLoggingSysOut", "Debug console output: ",
                optionType.CHECKBOX);
        
        layoutGrid(panel, 4, 2, SMALL_BORDER, SMALL_BORDER,
                LARGE_BORDER, LARGE_BORDER);
        
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        loggingPanel.add(panel, constraints);
    }
    
    /**
     * Initialises the identities tab.
     *
     * @param tabbedPane parent pane
     */
    private void initIdentitiesTab(final JPanel cardLayoutPanel) {
        final JPanel identitiesPanel = new JPanel(new GridBagLayout());
        
        cardLayoutPanel.add(identitiesPanel, "Identities");
        ((DefaultListModel) tabList.getModel()).addElement("Identities");
        
        identitiesPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
    }
    
    /**
     * Initialises the advanced tab.
     *
     * @param tabbedPane parent pane
     */
    private void initAdvancedTab(final JPanel cardLayoutPanel) {
        final JPanel advancedPanel = new JPanel(new GridBagLayout());
        
        cardLayoutPanel.add(advancedPanel, "Advanced");
        ((DefaultListModel) tabList.getModel()).addElement("Advanced");
        
        advancedPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
    }
    
    /**
     * Initialises listeners for this dialog.
     */
    private void initListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
    }
    
    /**
     * Handles the actions for the dialog.
     *
     * @param actionEvent Action event
     */
    public void actionPerformed(final ActionEvent actionEvent) {
        if (getOkButton().equals(actionEvent.getSource())) {
            //TODO apply settings
            setVisible(false);
        } else if (getCancelButton().equals(actionEvent.getSource())) {
            setVisible(false);
        }
    }
    
    /**
     * Aligns the components in a container horizontally and adds springs
     * vertically.
     *
     * @param parent parent container
     * @param rows number of rows
     * @param columns number of columns
     * @param initialXPadding initial x padding
     * @param initialYPadding initial y padding
     * @param xPadding x padding
     * @param yPadding y padding
     */
    public void layoutGrid(final Container parent, final int rows,
            final int columns, final int initialXPadding,
            final int initialYPadding, final int xPadding, final int yPadding) {
        final SpringLayout layout = (SpringLayout) parent.getLayout();
        
        Spring x = Spring.constant(initialXPadding);
        Spring y = Spring.constant(initialYPadding);
        SpringLayout.Constraints constraints;
        
        for (int c = 0; c < columns; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width,
                        getConstraintsForCell(r, c, parent, columns).
                        getWidth());
            }
            for (int r = 0; r < rows; r++) {
                constraints = getConstraintsForCell(r, c, parent, columns);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPadding)));
        }
        
        for (int r = 0; r < rows; r++) {
            int height = 0;
            for (int c = 0; c < columns; c++) {
                height +=
                        getConstraintsForCell(r, c, parent, columns).
                        getHeight().getValue();
            }
            for (int c = 0; c < columns; c++) {
                constraints = getConstraintsForCell(r, c, parent, columns);
                constraints.setY(y);
                constraints.setHeight(Spring.constant(height));
            }
            y = Spring.sum(y, Spring.sum(Spring.constant(height),
                    Spring.constant(yPadding)));
        }
        
        final SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }
    
    /**
     * Returns the constraints for a specific cell.
     *
     * @param row Row of cell
     * @param column Column of cell
     * @param parent parent container
     * @param columns number of columns
     *
     * @return Constraits for a specific cell
     */
    private SpringLayout.Constraints getConstraintsForCell(final int row,
            final int column, final Container parent, final int columns) {
        final SpringLayout layout = (SpringLayout) parent.getLayout();
        final Component c = parent.getComponent(row * columns + column);
        return layout.getConstraints(c);
    }
    
    /**
     * Called when the selection in the list changes.
     *
     * @param selectionEvent list selection event
     */
    public void valueChanged(ListSelectionEvent selectionEvent) {
        if (!selectionEvent.getValueIsAdjusting()) {
            String selectedItem = (String) ((JList) selectionEvent.getSource()).
                    getSelectedValue();
            System.out.println(selectedItem);
            cardLayout.show(mainPanel, selectedItem);
        }
    }
}
