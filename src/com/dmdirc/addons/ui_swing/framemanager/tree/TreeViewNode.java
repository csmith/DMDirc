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

import com.dmdirc.FrameContainer;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Tree view node. Encapsulates a frame container and a label in a node.
 */
public class TreeViewNode extends DefaultMutableTreeNode {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    /** Node's label. */
    private NodeLabel label;
    /** Node's frame container. */
    private FrameContainer frameContainer;
    
    /**
     * Creates a new treeview node.
     * @param label
     * @param frameContainer
     */
    public TreeViewNode(final NodeLabel label, final FrameContainer frameContainer) {
        super();
        
        this.label = label;
        this.frameContainer = frameContainer;
    }
    
    /**
     * Returns the label for this node.
     *
     * @return NodeLabel for the node
     */
    public NodeLabel getLabel() {
        return label;
    }

    /**
     * Returns the frame container for this node.
     *
     * @return FrameContainer for this node
     */
    public FrameContainer getFrameContainer() {
        return frameContainer;
    }

}
