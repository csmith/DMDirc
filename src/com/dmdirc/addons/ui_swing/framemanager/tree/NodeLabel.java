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

import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.interfaces.FrameInfoListener;
import com.dmdirc.interfaces.NotificationListener;
import com.dmdirc.interfaces.SelectionListener;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.interfaces.Window;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import net.miginfocom.layout.PlatformDefaults;

/**
 * Node label.
 */
public class NodeLabel extends FormattedLabel implements SelectionListener,
        NotificationListener, FrameInfoListener, ConfigChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Node window. */
    private final Window window;
    /** Rollover colours. */
    private boolean rollover;
    /** notification colour */
    private Color notificationColour;
    /** Selected. */
    private boolean selected;
    /** Bold on active? */
    private boolean activeBold;
    /** Active background. */
    private Color activeBackground;
    /** Active foreground. */
    private Color activeForeground;
    /** Background. */
    private Color background;
    /** Foreground. */
    private Color foreground;
    /** Rollover colours. */
    private Color rolloverColour;
    /** Config. */
    private ConfigManager config;

    /** 
     * Instantiates a new node label.
     * 
     * @param window Window for this node
     */
    public NodeLabel(final Window window) {
        super(IconManager.getIconManager().getIcon(window.getContainer().
                getIcon()), window.getContainer().toString());
        config = IdentityManager.getGlobalConfig();

        this.window = window;

        init();
    }

    /**
     * Initialises the label.
     */
    private void init() {
        if (window == null) {
            return;
        }

        setOpaque(true);
        setToolTipText(null);
        setBorder(BorderFactory.createEmptyBorder(1, 0, 2, 0));

        setPreferredSize(new Dimension(100000, getFont().getSize() +
                (int) PlatformDefaults.getUnitValueX("related").
                getValue()));
        notificationColour = null;
        selected = false;
        setConfigColours();
        config.addChangeListener("ui", this);
        config.addChangeListener("treeview", this);
    }

    /** {@inheritDoc} */
    @Override
    public void selectionChanged(final Window window) {
        selected = equals(window);
        setColours();
    }

    /** {@inheritDoc} */
    @Override
    public void notificationSet(final Window window, final Color colour) {
        if (equals(window)) {
            notificationColour = colour;
            setColours();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notificationCleared(final Window window) {
        if (equals(window)) {
            notificationColour = null;
            setColours();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void iconChanged(final Window window, final String icon) {
        if (equals(window)) {
            setIcon(IconManager.getIconManager().getIcon(icon));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void nameChanged(final Window window, final String name) {
        if (equals(window)) {
            setText(name);
        }
    }

    /** 
     * Sets the rollover state for the node.
     * 
     * @param rollover rollover state
     */
    public void setRollover(final boolean rollover) {
        this.rollover = rollover;
        setColours();
    }

    /**
     * Is this node a rollover node?
     * 
     * @return true iff this node is a rollover node
     */
    public boolean isRollover() {
        return rollover;
    }

    /**
     * Is this node a selected node?
     * 
     * @return true iff this node is a selected node
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Returns the notification colour for this node.
     * 
     * @return notification colour or null if non set
     */
    public Color getNotificationColour() {
        return notificationColour;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (window == null) {
            return false;
        }

        return window.equals(obj);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        if (window == null) {
            return super.hashCode();
        }

        return window.hashCode();
    }

    private void setConfigColours() {
        activeBold = config.getOptionBool("ui", "treeviewActiveBold");
        activeBackground = config.getOptionColour(
                "ui", "treeviewActiveBackground",
                "treeview", "backgroundcolour",
                "ui", "backgroundcolour");
        activeForeground = config.getOptionColour(
                "ui", "treeviewActiveForeground",
                "treeview", "foregroundcolour",
                "ui", "foregroundcolour");
        background = config.getOptionColour(
                "treeview", "backgroundcolour",
                "ui", "backgroundcolour");
        foreground = config.getOptionColour(
                "treeview", "foregroundcolour",
                "ui", "foregroundcolour");
        rolloverColour = config.getOptionColour(
                "ui", "treeviewRolloverColour");
        setColours();
    }

    private void setColours() {
        final SimpleAttributeSet as = new SimpleAttributeSet();
        StyleConstants.setBackground(as, background);
        StyleConstants.setForeground(as, foreground);
        if (isRollover() && rolloverColour != null) {
            StyleConstants.setForeground(as, rolloverColour);
        }
        if (getNotificationColour() != null) {
            StyleConstants.setForeground(as, getNotificationColour());
        }
        if (isSelected()) {
            if (activeBold) {
                StyleConstants.setBold(as, true);
            }
            StyleConstants.setBackground(as, activeBackground);
            StyleConstants.setForeground(as, activeForeground);
        } else {
            StyleConstants.setBold(as, false);
        }
        getDocument().setCharacterAttributes(0, getDocument().getLength(), as,
                true);
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(String domain, String key) {
        if (("ui".equals(domain) || "treeview".equals(domain)) &&
                ("treeviewRolloverColour".equals(key) ||
                "treeviewActiveBackground".equals(key) ||
                "treeviewActiveForeground".equals(key) ||
                "treeviewActiveBold".equals(key) ||
                "backgroundcolour".equals(key) ||
                "foregroundcolour".equals(key))) {
            setConfigColours();
        }
    }
}
