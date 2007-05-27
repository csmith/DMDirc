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

package com.dmdirc.addons.osdplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

/**
 * The OSD Window is an always-on-top window designed to convey information
 * about events to the user.
 * @author chris
 */
public class OsdWindow extends JFrame implements MouseListener {
    
    /**
     * Creates a new instance of OsdWindow.
     * @param text The text to be displayed in the OSD window
     */
    public OsdWindow(final String text) {
        super();
        
        setAlwaysOnTop(true);
        setSize(500, 50);
        setResizable(false);
        setUndecorated(true);
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        setLocation(20, 20);
        
        final JPanel panel = new JPanel();
        panel.setBorder(new LineBorder(Color.BLACK));
        panel.setBackground(Color.decode("#2222aa"));
        
        setContentPane(panel);
        setLayout(new BorderLayout());
        
        final JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(label.getFont().deriveFont((float) 20));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label);
        
        addMouseListener(this);
        
        setVisible(true);
        
        new Timer().schedule(new TimerTask() {
            public void run() {
                setVisible(false);
            }
        }, 15000);
    }

    /** {@inheritDoc} */
    public void mouseClicked(final MouseEvent e) {
        setVisible(false);
    }

    /** {@inheritDoc} */
    public void mousePressed(final MouseEvent e) {
        // Do nothing
    }

    /** {@inheritDoc} */
    public void mouseReleased(final MouseEvent e) {
        // Do nothing
    }

    /** {@inheritDoc} */
    public void mouseEntered(final MouseEvent e) {
        // Do nothing
    }

    /** {@inheritDoc} */
    public void mouseExited(final MouseEvent e) {
        // Do nothing
    }
    
}
