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

package com.dmdirc.ui.swing;

import com.dmdirc.ui.swing.actions.RedoAction;
import com.dmdirc.ui.swing.actions.UndoAction;
import com.dmdirc.ui.swing.components.DMDircUndoableEditListener;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.KeyStroke;

import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

/**
 * UI constants.
 */
public final class UIUtilities {
    
    /** Size of a large border. */
    public static final int LARGE_BORDER = 10;
    
    /** Size of a small border. */
    public static final int SMALL_BORDER = 5;
    
    /** Standard button size. */
    public static final Dimension BUTTON_SIZE = new Dimension(100, 25);
    
    /** Not intended to be instatiated. */
    private UIUtilities() {
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
    public static void layoutGrid(final Container parent, final int rows,
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
    private static SpringLayout.Constraints getConstraintsForCell(final int row,
            final int column, final Container parent, final int columns) {
        final SpringLayout layout = (SpringLayout) parent.getLayout();
        final Component constraints = parent.getComponent(row * columns + column);
        return layout.getConstraints(constraints);
    }
    
    public static void addUndoManager(final JTextComponent component) {
        final UndoManager undoManager = new UndoManager();
        
        // Listen for undo and redo events
        component.getDocument().addUndoableEditListener(
                new DMDircUndoableEditListener(undoManager));
        
        // Create an undo action and add it to the text component
        component.getActionMap().put("Undo", new UndoAction(undoManager));
        
        // Bind the undo action to ctl-Z
        component.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
        
        // Create a redo action and add it to the text component
        component.getActionMap().put("Redo", new RedoAction(undoManager));
        
        // Bind the redo action to ctl-Y
        component.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
    }
    
    /**
     * Initialises any settings required by this UI (this is always called
     * before any aspect of the UI is instansiated).
     *
     * @throws InstantiationException
     * @throws ClassNotFoundException
     * @throws UnsupportedLookAndFeelException
     * @throws IllegalAccessException
     */
    public static void initUISettings() throws InstantiationException,
            ClassNotFoundException, UnsupportedLookAndFeelException,
            IllegalAccessException {
        
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        
        final FontUIResource font = new FontUIResource("Dialog", Font.PLAIN , 12);
        
        UIManager.put("TableHeader.font", font);
        UIManager.put("ToggleButton.font", font);
        UIManager.put("ScrollPane.font", font);
        UIManager.put("Spinner.font", font);
        UIManager.put("RadioButtonMenuItem.font", font);
        UIManager.put("Slider.font", font);
        UIManager.put("OptionPane.font", font);
        UIManager.put("ToolBar.font", font);
        UIManager.put("CheckBoxMenuItem.font", font);
        UIManager.put("Table.font", font);
        UIManager.put("MenuBar.font", font);
        UIManager.put("PopupMenu.font", font);
        UIManager.put("DesktopIcon.font", font);
        UIManager.put("TextPane.font", font);
        UIManager.put("ProgressBar.font", font);
        UIManager.put("FormattedTextField.font", font);
        UIManager.put("ColorChooser.font", font);
        UIManager.put("Viewport.font", font);
        UIManager.put("ToolTip.font", font);
        UIManager.put("Label.font", font);
        UIManager.put("TextField.font", font);
        UIManager.put("PasswordField.font", font);
        UIManager.put("Button.font", font);
        UIManager.put("RadioButton.font", font);
        UIManager.put("CheckBox.font", font);
        UIManager.put("ComboBox.font", font);
        UIManager.put("Menu.font", font);
        UIManager.put("List.font", font);
        UIManager.put("MenuItem.font", font);
        UIManager.put("Panel.font", font);
        UIManager.put("TitledBorder.font", font);
        UIManager.put("TabbedPane.font", font);
        UIManager.put("Tree.font", font);
        UIManager.put("InternalFrame.titleFont", font);
        UIManager.put("EditorPane.font", font);
        UIManager.put("TextArea.font", font);
        
        UIManager.put("CheckBoxMenuItem.acceleratorFont",
                font.deriveFont((float) font.getSize() - 2));
        UIManager.put("Menu.acceleratorFont",
                font.deriveFont((float) font.getSize() - 2));
        UIManager.put("MenuItem.acceleratorFont",
                font.deriveFont((float) font.getSize() - 2));
        
        UIManager.put("swing.boldMetal", false);
        UIManager.put("InternalFrame.useTaskBar", false);
        UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(1, 1, 1, 1));
        UIManager.put("Tree.scrollsOnExpand", true);
        UIManager.put("Tree.scrollsHorizontallyAndVertically", true);
        
        UIManager.put("Tree.dropCellBackground", Color.WHITE);
        UIManager.put("Tree.selectionBackground", Color.WHITE);
        UIManager.put("Tree.textBackground", Color.WHITE);
        UIManager.put("Tree.selectionBorderColor", Color.WHITE);
        UIManager.put("Tree.drawsFocusBorder", false);
        UIManager.put("Tree.drawHorizontalLines", true);
        UIManager.put("Tree.drawVerticalLines", true);
        UIManager.put("Tree.background", Color.WHITE);
    }
    
    /**
     * Returns the class name of the look and feel from its display name.
     *
     * @param displayName Look and feel display name
     *
     * @return Look and feel class name or a zero length string
     */
    public static String getLookAndFeel(final String displayName) {
        final StringBuilder classNameBuilder = new StringBuilder();
        
        if (displayName != null && !displayName.isEmpty()) {
            for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
                if (laf.getName().equals(displayName)) {
                    classNameBuilder.setLength(0);
                    classNameBuilder.append(laf.getClassName());
                    break;
                }
            }
        }
        return classNameBuilder.toString();
    }
}
