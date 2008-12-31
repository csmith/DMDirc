/*
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

package com.dmdirc.ui.core.dialogs.prefs.addons;

/**
 * Represents one known addon of any type.
 *
 * @since 0.6.3
 * @author chris
 */
public abstract class Addon {

    /**
     * Retrieves the type of this addon.
     *
     * @return This addon's type
     */
    public abstract AddonType getType();

    /**
     * Retrieves the name of this addon.
     *
     * @return This addon's name
     */
    public abstract String getName();

    /**
     * Retrieves the user-friendly version of this addon.
     *
     * @return This addon's friendly version
     */
    public abstract String getVersion();

    /**
     * Retrieves the status of this addon.
     *
     * @return This addon's current status
     */
    public abstract AddonStatus getStatus();

    /**
     * Retrieves more detailed information about this addon's status.
     *
     * @return The status text for this addon
     */
    public abstract String getStatusText();

    /**
     * Retrieves a description (name and e-mail address usually) of the addon's
     * author.
     *
     * @return This addon's author
     */
    public abstract String getAuthor();

    /**
     * Retrieves a short description of this addon.
     *
     * @return This addon's description
     */
    public abstract String getDescription();

    /**
     * Enables the addon, if it wasn't previously enabled.
     *
     * @return True if the addon is now enabled, false if it is disabled
     */
    protected abstract boolean enable();

    /**
     * Disables the addon, if it wasn't previously disabled.
     *
     * @return True if the addon is now enabled, false if it is disabled
     */
    protected abstract boolean disable();

    /**
     * Installs the addon.
     */
    protected abstract void install();

    /**
     * Uninstalls the addon.
     */
    protected abstract void uninstall();

    
    protected abstract void setUpdateState(final boolean check);

}
