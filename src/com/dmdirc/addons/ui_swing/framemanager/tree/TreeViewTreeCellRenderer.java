/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import java.awt.Component;

import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

/**
 * Displays a node in a tree according to its type.
 */
public class TreeViewTreeCellRenderer implements TreeCellRenderer {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 4;

    /**
     * Configures the renderer based on the passed parameters.
     *
     * @param tree JTree for this renderer.
     * @param value node to be renderered.
     * @param sel whether the node is selected.
     * @param expanded whether the node is expanded.
     * @param leaf whether the node is a leaf.
     * @param row the node's row.
     * @param hasFocus whether the node has focus.
     *
     * @return RendererComponent for this node.
     */
    @Override
    public final Component getTreeCellRendererComponent(final JTree tree,
            final Object value, final boolean sel, final boolean expanded,
            final boolean leaf, final int row, final boolean hasFocus) {

        if (value == null) {
            return new JLabel("Node == null");
        }
        final NodeLabel label = ((TreeViewNode) value).getLabel();
        if (label == null) {
            return new JLabel("Label == null");
        }

        return label;
    }
}
