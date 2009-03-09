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

package com.dmdirc.plugins.services;

import com.dmdirc.plugins.ExportedService;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.Service;
import com.dmdirc.plugins.ServiceProvider;
import java.util.Arrays;
import java.util.List;

/**
 * Provides a fake service representing DMDirc.
 *
 * @since 0.6.3
 * @author chris
 */
public class DMDircService implements ServiceProvider {

    /** The plugin manager to use to construct services. */
    private final PluginManager manager;

    /** The name to use for the service. */
    private final String name;

    /**
     * Creates a new DMDirc service.
     *
     * @param manager The plugin manager to use to construct services
     * @param name The name to use for the service
     */
    public DMDircService(final PluginManager manager, final String name) {
        this.manager = manager;
        this.name = name;

        for (Service service : getServices()) {
            service.addProvider(this);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isActive() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void activateServices() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public List<Service> getServices() {
        return Arrays.asList(new Service[]{manager.getService("dmdirc", name, true)});
    }

    /** {@inheritDoc} */
    @Override
    public String getProviderName() {
        return "(pseudo service)";
    }

    /** {@inheritDoc} */
    @Override
    public ExportedService getExportedService(final String name) {
        return new ExportedService(null, null);
    }

}
