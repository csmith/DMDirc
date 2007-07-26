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

package com.dmdirc.ui.interfaces;

import java.awt.Component;

import javax.swing.Icon;

/**
 * Status bar interface.
 */
public interface StatusBar {
    
    /**
     * sets the message in the status bar.
     *
     * @param newMessage Message to display
     */
    void setMessage(final String newMessage);
    
    /**
     * Sets the message in the status bar with a specified callback event
     * using the default timeout.
     *
     * @param newMessage Message to display
     * @param newNotifier status message notifier to be notified for events on
     * this message
     */
    void setMessage(final String newMessage, 
            final StatusMessageNotifier newNotifier);
    
    /**
     * Sets the message in the status bar with a specified callback event for
     * a specified time.
     *
     * @param newMessage Message to display
     * @param newNotifier status message notifier to be notified for events on
     * this message
     * @param timeout message timeout in seconds
     */
    void setMessage(final String newMessage,
            final StatusMessageNotifier newNotifier, final int timeout);
    
    /**
     * Removes the message from the status bar.
     */
    void clearMessage();
    
    /**
     * sets the icon in the status bar.
     *
     * @param newIcon Icon to display
     */
    void setError(final Icon newIcon);
    
    /**
     * sets the icon in the status bar with a specified callback event.
     *
     * @param newIcon Icon to display
     * @param newNotifier status error notifier to be notified for events on
     * this error
     */
    void setError(final Icon newIcon,
            final StatusErrorNotifier newNotifier);
    
    /**
     * Removes the error state from the status bar.
     */
    void clearError();
    
    /**
     * Adds a component to the status bar.
     *
     * @param component component to add
     */
    void addComponent(final Component component);
    
    /**
     * Removes a component to the status bar.
     *
     * @param component component to add
     */
    void removeComponent(final Component component);
    
}
