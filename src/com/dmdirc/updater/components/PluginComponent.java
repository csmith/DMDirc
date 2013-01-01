/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.updater.components;

import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.Version;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import lombok.RequiredArgsConstructor;

/**
 * An update component for plugins.
 */
@RequiredArgsConstructor
public class PluginComponent implements UpdateComponent {

    /** The config to use. */
    private static final ConfigManager CONFIG = IdentityManager.getIdentityManager().getGlobalConfiguration();

    /** The plugin this component is for. */
    private final PluginInfo plugin;

    /** {@inheritDoc} */
    @Override
    public String getName() {
        if (plugin.getMetaData().getUpdaterId() > 0) {
            return "addon-" + plugin.getMetaData().getUpdaterId();
        } else {
            return "addon-" + CONFIG.getOption("plugin-addonid", plugin.getMetaData().getName());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getFriendlyName() {
        return plugin.getMetaData().getFriendlyName();
    }

    /** {@inheritDoc} */
    @Override
    public String getFriendlyVersion() {
        return plugin.getMetaData().getFriendlyVersion();
    }

    /** {@inheritDoc} */
    @Override
    public Version getVersion() {
        return plugin.getMetaData().getVersion();
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresRestart() {
        return plugin.isLoaded();
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresManualInstall() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String getManualInstructions(final String path) {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public boolean doInstall(final String path) {
        final File target = new File(plugin.getMetaData().getPluginUrl().getPath());

        boolean returnCode = false;
        final boolean wasLoaded = plugin.isLoaded();

        if (!wasLoaded && target.exists()) {
            target.delete();
        }

        // Try and move the downloaded plugin to the new location.
        // If it doesn't work then keep the plugin in a .update file untill
        // the next restart.
        // If it does, update the metadata.
        final File newPlugin = new File(path);
        if (!isValid(newPlugin)) {
            return false;
        }
        if (requiresRestart() || !newPlugin.renameTo(target)) {
            // Windows rocks!
            final File newTarget = new File(plugin.getMetaData().getPluginUrl().getPath() + ".update");

            if (newTarget.exists()) {
                newTarget.delete();
            }

            new File(path).renameTo(newTarget);
            returnCode = true;
        } else {
            plugin.pluginUpdated();
        }

        return returnCode;
    }

    /**
     * Test is a file is a valid zip file.
     *
     * @param file Zip file
     *
     * @return true if the file is valid
     */
    private boolean isValid(final File file) {
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(file);
            return true;
        } catch (ZipException e) {
            return false;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (zipfile != null) {
                    zipfile.close();
                    zipfile = null;
                }
            } catch (IOException e) {
            }
        }
    }
}
