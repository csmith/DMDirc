/*
 * Copyright (c) 2006-2012 DMDirc Developers
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

package com.dmdirc.updater.manager;

import com.dmdirc.config.ConfigBinding;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.updater.UpdateChannel;
import com.dmdirc.updater.checking.DMDircCheckStrategy;
import com.dmdirc.updater.checking.NaiveConsolidator;
import com.dmdirc.updater.components.ClientComponent;
import com.dmdirc.updater.components.DefaultsComponent;
import com.dmdirc.updater.components.ModeAliasesComponent;
import com.dmdirc.updater.installing.LegacyInstallationStrategy;
import com.dmdirc.updater.retrieving.DownloadRetrievalStrategy;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * Specialised update manager for DMDirc.
 */
@Slf4j
public class DMDircUpdateManager extends CachingUpdateManagerImpl {

    /** The default check strategy we use. */
    private final DMDircCheckStrategy checkStrategy;

    /**
     * Creates a new instance of the update manager.
     *
     * @param configManager The config manager to use to retrieve channel info
     * @param tempFolder Temporary folder to use for downloading updates
     * @param threads The number of threads to use for updating
     */
    public DMDircUpdateManager(final ConfigManager configManager,
            final String tempFolder, final int threads) {
        super(new ThreadPoolExecutor(threads, threads, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory()),
                new NaiveConsolidator(),
                new ConfigComponentPolicy(configManager));

        addComponent(new ClientComponent());
        addComponent(new ModeAliasesComponent());
        addComponent(new DefaultsComponent());

        checkStrategy = new DMDircCheckStrategy(UpdateChannel.STABLE);

        configManager.getBinder().bind(this, DMDircUpdateManager.class);

        addCheckStrategy(checkStrategy);
        addRetrievalStrategy(new DownloadRetrievalStrategy(tempFolder));
        addInstallationStrategy(new LegacyInstallationStrategy());
    }

    /**
     * Sets the channel which will be used by the {@link DMDircCheckStrategy}.
     *
     * @param channel The new channel to use
     */
    @ConfigBinding(domain="updater", key="channel")
    public void setChannel(final String channel) {
        log.info("Changing channel to {}", channel);

        try {
            checkStrategy.setChannel(UpdateChannel.valueOf(channel.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown channel {}", channel, ex);
        }
    }

    /**
     * Thread factory which returns named threads.
     */
    private static class NamedThreadFactory implements ThreadFactory {

        /** {@inheritDoc} */
        @Override
        public Thread newThread(final Runnable r) {
            return new Thread(r, "Updater thread");
        }

    }

}