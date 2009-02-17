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

import com.dmdirc.plugins.PluginInfo;

/**
 * Represents an installed plugin.
 *
 * @since 0.6.3
 * @author chris
 */
public class PluginAddon extends Addon {

    /** The theme that's being represented. */
    private final PluginInfo plugin;

    /**
     * Creates a new PluginAddon for the specified {@link PluginInfo}.
     *
     * @param plugin The plugin to represent.
     */
    public PluginAddon(final PluginInfo plugin) {
        this.plugin = plugin;
    }

    /** {@inheritDoc} */
    @Override
    public AddonType getType() {
        return AddonType.PLUGIN;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return plugin.getName();
    }

    /** {@inheritDoc} */
    @Override
    public String getVersion() {
        return plugin.getFriendlyVersion();
    }

    /** {@inheritDoc} */
    @Override
    public AddonStatus getStatus() {
        return plugin.isLoaded() ? AddonStatus.ENABLED :
            AddonStatus.DISABLED;
    }

    /** {@inheritDoc} */
    @Override
    public String getStatusText() {
        return plugin.getLastError();
    }

    /** {@inheritDoc} */
    @Override
    public String getAuthor() {
        return plugin.getAuthor();
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return plugin.getDescription();
    }

    /** {@inheritDoc} */
    @Override
    public void setUpdateState(final boolean check) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public AddonStatus setStatus(final AddonStatus newStatus) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean canSetStatus(final AddonStatus newStatus) {
        if (newStatus == AddonStatus.ERROR) {
            return false;
        }

        if (newStatus == AddonStatus.DISABLED) {
            return plugin.isUnloadable();
        }

        return true;
    }

}
