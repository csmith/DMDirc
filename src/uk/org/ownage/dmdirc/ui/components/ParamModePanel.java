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

package uk.org.ownage.dmdirc.ui.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import uk.org.ownage.dmdirc.Config;

/**
 * A component to encapsulate one parameter-requiring channel mode, displaying
 * the user a checkbox, the mode's name, and a text field.
 * @author chris
 */
public final class ParamModePanel extends JPanel implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** The checkbox used in this mode panel. */
    private JCheckBox checkBox;
    
    /** The textfield for the value of the mode. */
    private JTextField textField;
    
    
    /** the mode this component represents. */
    private String mode;
    
    /**
     * Creates a new instance of ParamModePanel.
     * @param thisMode The mode that this panel should deal with
     * @param state The current state of the mode
     * @param value The current value of the mode
     */
    public ParamModePanel(final String thisMode, final boolean state, final String value) {
        
        this.mode = thisMode;
        String text = "Mode " + mode;
        final GridBagConstraints constraints = new GridBagConstraints();
        
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(5, 0, 0, 0));
        
        if (Config.hasOption("server", "mode" + mode)) {
            text = Config.getOption("server", "mode" + mode);
        }
        
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.LINE_START;
        checkBox = new JCheckBox(text, state);
        add(checkBox, constraints);
        
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.LINE_END;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        textField = new JTextField(value);
        textField.setColumns(16);
        textField.setInputVerifier(new ModeParameterVerifier());
        add(textField, constraints);
        
        if (!state) {
            textField.setEnabled(false);
        }
        
        checkBox.addActionListener(this);
        checkBox.setBorder(new EmptyBorder(0, 0, 0, 0));
    }
    
    /**
     * Called when our checkbox is toggled.
     * @param actionEvent associated action event
     */
    public void actionPerformed(final ActionEvent actionEvent) {
        textField.setEnabled(checkBox.isSelected());
    }
    
    /**
     * returns the state of this component.
     * @return boolean state of mode
     */
    public boolean getState() {
        return checkBox.isSelected();
    }
    
    /**
     * returns the parameter of this mode if enabled, else returns an empty
     * string.
     * @return String mode parameter or "" if unset
     */
    public String getValue() {
        return textField.getText();
    }
    
    /**
     * Returns the name of the mode this component represents.
     * @return String name of the mode
     */
    public String getModeName() {
        return checkBox.getText();
    }
    
    /**
     * Returns the mode this component represents.
     * @return String mode
     */
    public String getMode() {
        return mode;
    }
    
}

/**
 * Verifies that the parameter has no spaces.
 */
class ModeParameterVerifier extends InputVerifier {
    
    /**
     * Creates a new instance of LimitVerifier.
     */
    public ModeParameterVerifier() {
        
    }
    
    /**
     * Verifies that the parameter contains no spaces.
     * @param jComponent The component to be tested
     * @return true iff the text contains no spaces, false otherwise
     */
    public boolean verify(final JComponent jComponent) {
        final JTextField textField = (JTextField) jComponent;
        return !textField.getText().contains(" ");
    }
    
}

