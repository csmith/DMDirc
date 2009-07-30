/*
 * 
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.framemanager.tree;

import com.dmdirc.ui.messages.Styliser;
import java.awt.Font;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import net.miginfocom.swing.MigLayout;

/**
 * Formatted label.
 */
public class FormattedLabel extends JComponent {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Icon label. */
    private JLabel icon;
    /** Text field. */
    private JTextPane text;

    /**
     * Instantiates a new formatted label.
     *
     * @param icon Label icon
     * @param text Label text
     */
    public FormattedLabel(final Icon icon, final String text) {
        super();

        this.icon = new JLabel();
        this.text = new JTextPane(new DefaultStyledDocument());

        this.icon.setBorder(null);
        this.text.setBorder(null);

        setIcon(icon);
        setText(text);

        setLayout(new MigLayout("fill, ins 0, gap 0"));
        add(this.icon, "pad 0, gapright 5");
        add(this.text, "growx, pushx, pad 0");
    }

    /**
     * Sets this labels icon.
     *
     * @param icon New icon
     */
    public void setIcon(final Icon icon) {
        this.icon.setIcon(icon);
    }

    /**
     * Sets this labels text.
     *
     * @param text New text
     */
    public void setText(final String text) {
        try {
            ((DefaultStyledDocument) getDocument()).remove(0, getDocument().getLength());
        } catch (BadLocationException ex) {
            //Ignore
        }
        Styliser.addStyledString(getDocument(), new String[]{text,});
    }

    public void setFont(final Font font) {
        this.text.setFont(font);
    }

    public Font getFont() {
        return this.text.getFont();
    }

    public StyledDocument getDocument() {
        return (StyledDocument) this.text.getDocument();
    }
}
