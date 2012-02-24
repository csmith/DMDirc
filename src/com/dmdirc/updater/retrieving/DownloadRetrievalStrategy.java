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

package com.dmdirc.updater.retrieving;

import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.checking.DownloadableUpdate;
import com.dmdirc.util.io.DownloadListener;
import com.dmdirc.util.io.Downloader;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import lombok.ListenerSupport;
import lombok.extern.slf4j.Slf4j;

/**
 * An {@link UpdateRetrievalStategy} that downloads a file specified in a
 * {@link DownloadableUpdate}.
 */
@Slf4j
@ListenerSupport(UpdateRetrievalListener.class)
public class DownloadRetrievalStrategy extends TypeSensitiveRetrievalStrategy<DownloadableUpdate> {

    /** The directory to put temporary update files in. */
    private final String directory;

    /**
     * Creates a new {@link DownloadRetrievalStrategy} which will place its
     * temporary files in the given directory.
     *
     * @param directory The directory to use to download files to
     */
    public DownloadRetrievalStrategy(final String directory) {
        super(DownloadableUpdate.class);

        this.directory = directory;
    }

    /** {@inheritDoc} */
    @Override
    protected UpdateRetrievalResult retrieveImpl(final DownloadableUpdate checkResult) {
        try {
            final String file = getFileName();

            fireRetrievalProgressChanged(checkResult.getComponent(), 0);

            log.debug("Downloading file from {} to {}", checkResult.getUrl(), file);
            Downloader.downloadPage(checkResult.getUrl().toString(), file,
                    new DownloadProgressListener(checkResult.getComponent()));

            fireRetrievalCompleted(checkResult.getComponent());

            return new BaseSingleFileResult(checkResult, new File(file));
        } catch (IOException ex) {
            log.warn("I/O exception downloading update from {}", checkResult.getUrl(), ex);
            fireRetrievalFailed(checkResult.getComponent());
        }

        return new BaseRetrievalResult(checkResult, false);
    }

    /**
     * Creates a random local file name to download the remote file to.
     *
     * @return The full, local path to download the remote file to
     */
    private String getFileName() {
        return directory + File.separator + "update."
                    + Math.round(10000 * Math.random()) + ".tmp";
    }

    /**
     * A {@link DownloadListener} which proxies progress updates on to
     * this strategy's {@link UpdateRetrievalListener}s.
     */
    @AllArgsConstructor
    private class DownloadProgressListener implements DownloadListener {

        /** The component to fire updates for. */
        private final UpdateComponent component;

        /** {@inheritDoc} */
        @Override
        public void downloadProgress(final float percent) {
            fireRetrievalProgressChanged(component, percent);
        }

        /** {@inheritDoc} */
        @Override
        public void setIndeterminate(final boolean indeterminate) {
            // Do nothing
        }

    }

}