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

package uk.org.ownage.dmdirc.ui.dialogs.actionseditor;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import uk.org.ownage.dmdirc.actions.Action;

import static uk.org.ownage.dmdirc.ui.UIUtilities.LARGE_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;
import uk.org.ownage.dmdirc.ui.messages.Formatter;

/**
 * Response tab panel, response and formatter editing for the actions editor
 * dialog.
 */
public final class ResponseTabPanel extends JPanel {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** The action. */
    private ActionsEditorDialog owner;
    /** Response field. */
    private JTextArea responses;
    /** Formatters combobox. */
    private JComboBox formatter;
    /** Formatter scrollpane. */
    private JScrollPane scrollPane;
    
    /**
     * Creates a new instance of ResponseTabPanel.
     *
     * @param action action to be edited
     */
    public ResponseTabPanel(final ActionsEditorDialog owner) {
        super();
        
        this.owner = owner;
        
        initComponents();
        addListeners();
        layoutComponents();
    }
    
    /** Initialises the components. */
    private void initComponents() {
        responses = new JTextArea();
        formatter = new JComboBox(new DefaultComboBoxModel());
        scrollPane = new JScrollPane(responses);
        
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(SMALL_BORDER, 0,
                LARGE_BORDER, 0), scrollPane.getBorder()
                ));
        
        responses.setRows(3);
        formatter.setPreferredSize(
                new Dimension(100, formatter.getFont().getSize()
                + LARGE_BORDER));
        
        if (owner.getAction() == null) {
            return;
        }
        
        for (String response : owner.getAction().getResponse()) {
            responses.setText(responses.getText() + '\n' + response);
        }
        
        try {
            responses.setText(responses.getText(1, responses.getText().length()));
        } catch (BadLocationException ex) {
            //Ignore
        }
        
        responses.setText(responses.getText().substring(0, responses.getText().length() - 1));
        
        ((DefaultComboBoxModel) formatter.getModel()).addElement("No change");
        ((DefaultComboBoxModel) formatter.getModel()).addElement("No response");
        
        for (String format : Formatter.getFormats()) {
            ((DefaultComboBoxModel) formatter.getModel()).addElement(format);
        }
        
        if (owner.getAction().getNewFormat() == null) {
            formatter.setSelectedIndex(0);
        } else if ("".equals(owner.getAction().getNewFormat())) {
            formatter.setSelectedIndex(1);
        } else {
            formatter.setSelectedItem(owner.getAction().getNewFormat());
        }
    }
    
    /** Adds listeners to the components. */
    private void addListeners() {
    }
    
    /** Lays out components. */
    private void layoutComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        
        this.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
        this.setLayout(new GridBagLayout());
        
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridwidth = 4;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(new JLabel("Execute the following commands: "), constraints);
        
        constraints.weighty = 1.0;
        constraints.gridy = 1;
        add(scrollPane, constraints);
        
        constraints.weighty = 0.0;
        constraints.weightx = 0.0;
        constraints.gridwidth = 1;
        constraints.gridy = 2;
        add(new JLabel("Formatter: "), constraints);
        
        constraints.weightx = 1.0;
        constraints.gridx = 1;
        constraints.gridwidth = 3;
        add(formatter, constraints);
    }
    
    /**
     * Returns the responses for this panel.
     *
     * @return Responses
     */
    public String getResponses() {
        return responses.getText();
    }
    
    /**
     * Returns the name for this panel.
     *
     * @return Action name
     */
    public String getFormatter() {
        String format = (String) formatter.getSelectedItem();
        if ("No response".equals(format)) {
            format = "";
        }
        if ("No change".equals(format)) {
            format = null;
        }
        return format;
    }
    
}
