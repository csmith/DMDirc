/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

package com.dmdirc.logger;

import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.ui.FatalErrorDialog;
import com.dmdirc.util.collections.ListenerList;

import java.awt.GraphicsEnvironment;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import net.kencochrane.raven.DefaultRavenFactory;
import net.kencochrane.raven.RavenFactory;

/**
 * Error manager.
 */
public class ErrorManager implements ConfigChangeListener {

    /** Previously instantiated instance of ErrorManager. */
    private static ErrorManager me;
    /** A list of exceptions which we don't consider bugs and thus don't report. */
    private static final Class<?>[] BANNED_EXCEPTIONS = new Class<?>[]{
        NoSuchMethodError.class, NoClassDefFoundError.class,
        UnsatisfiedLinkError.class, AbstractMethodError.class,
        IllegalAccessError.class, OutOfMemoryError.class,
        NoSuchFieldError.class,};
    /** Whether or not to send error reports. */
    private boolean sendReports;
    /** Whether or not to log error reports. */
    private boolean logReports;
    /** Queue of errors to be reported. */
    private final BlockingQueue<ProgramError> reportQueue = new LinkedBlockingQueue<>();
    /** Thread used for sending errors. */
    private volatile Thread reportThread;
    /** Error list. */
    private final List<ProgramError> errors;
    /** Listener list. */
    private final ListenerList errorListeners = new ListenerList();
    /** Next error ID. */
    private final AtomicLong nextErrorID;
    /** Config to read settings from. */
    private AggregateConfigProvider config;
    /** Directory to store errors in. */
    private String errorsDirectory;

    /** Creates a new instance of ErrorListDialog. */
    public ErrorManager() {
        errors = new LinkedList<>();
        nextErrorID = new AtomicLong();
    }

    /**
     * Initialiases the error manager.
     *
     * @param globalConfig The configuration to read settings from.
     * @param directory    The directory to store errors in, if enabled.
     */
    public void initialise(final AggregateConfigProvider globalConfig, final String directory) {
        RavenFactory.registerFactory(new DefaultRavenFactory());

        config = globalConfig;
        config.addChangeListener("general", "logerrors", this);
        config.addChangeListener("general", "submitErrors", this);
        config.addChangeListener("temp", "noerrorreporting", this);
        updateSettings();

        errorsDirectory = directory;

        // Loop through any existing errors and send/save them per the config.
        for (ProgramError error : errors) {
            if (sendReports && error.getReportStatus() == ErrorReportStatus.WAITING) {
                sendError(error);
            }

            if (logReports) {
                error.save(errorsDirectory);
            }
        }
    }

    /**
     * Returns the instance of ErrorManager.
     *
     * @return Instance of ErrorManager
     */
    public static synchronized ErrorManager getErrorManager() {
        if (me == null) {
            me = new ErrorManager();
        }
        return me;
    }

    /**
     * Sets the singleton instance of the error manager.
     *
     * @param errorManager The error manager to use.
     */
    public static void setErrorManager(final ErrorManager errorManager) {
        me = errorManager;
    }

    /**
     * Adds a new error to the manager with the specified details. It is assumed that errors without
     * exceptions or details are not application errors.
     *
     * @param level   The severity of the error
     * @param message The error message
     *
     * @since 0.6.3m1
     */
    protected void addError(final ErrorLevel level, final String message) {
        addError(level, message, (String) null, false);
    }

    /**
     * Adds a new error to the manager with the specified details.
     *
     * @param level     The severity of the error
     * @param message   The error message
     * @param exception The exception that caused this error
     * @param appError  Whether or not this is an application error
     *
     * @since 0.6.3m1
     */
    protected void addError(final ErrorLevel level, final String message,
            final Throwable exception, final boolean appError) {
        addError(level, message, exception, null, appError, isValidError(exception));
    }

    /**
     * Adds a new error to the manager with the specified details.
     *
     * @param level    The severity of the error
     * @param message  The error message
     * @param details  The details of the exception
     * @param appError Whether or not this is an application error
     *
     * @since 0.6.3m1
     */
    protected void addError(final ErrorLevel level, final String message, final String details,
            final boolean appError) {
        addError(level, message, null, details, appError, true);
    }

    /**
     * Adds a new error to the manager with the specified details.
     *
     * @param level     The severity of the error
     * @param message   The error message
     * @param exception The exception that caused the error, if any.
     * @param details   The details of the exception, if any.
     * @param appError  Whether or not this is an application error
     * @param canReport Whether or not this error can be reported
     *
     * @since 0.6.3m1
     */
    protected void addError(final ErrorLevel level, final String message,
            final Throwable exception, final String details, final boolean appError,
            final boolean canReport) {
        addError(getError(level, message, exception, details), appError, canReport);
    }

    protected void addError(
            final ProgramError error,
            final boolean appError,
            final boolean canReport) {
        final boolean dupe = addError(error);
        if (error.getLevel().equals(ErrorLevel.FATAL)) {
            if (dupe) {
                error.setReportStatus(ErrorReportStatus.NOT_APPLICABLE);
                error.setFixedStatus(ErrorFixedStatus.DUPLICATE);
            }
        } else if (!canReport || (appError && !error.isValidSource())) {
            error.setReportStatus(ErrorReportStatus.NOT_APPLICABLE);
            error.setFixedStatus(ErrorFixedStatus.INVALID);
        } else if (!appError) {
            error.setReportStatus(ErrorReportStatus.NOT_APPLICABLE);
            error.setFixedStatus(ErrorFixedStatus.UNREPORTED);
        } else if (dupe) {
            error.setReportStatus(ErrorReportStatus.NOT_APPLICABLE);
            error.setFixedStatus(ErrorFixedStatus.DUPLICATE);
        } else if (sendReports) {
            sendError(error);
        }

        if (logReports) {
            error.save(errorsDirectory);
        }

        if (!dupe) {
            if (error.getLevel() == ErrorLevel.FATAL) {
                fireFatalError(error);
            } else {
                fireErrorAdded(error);
            }
        }
    }

    /**
     * Adds the specified error to the list of known errors and determines if it was previously
     * added.
     *
     * @param error The error to be added
     *
     * @return True if a duplicate error has already been registered, false otherwise
     */
    protected boolean addError(final ProgramError error) {
        int index;

        synchronized (errors) {
            index = errors.indexOf(error);
            if (index == -1) {
                errors.add(error);
            } else {
                errors.get(index).updateLastDate();
            }
        }

        return index > -1;
    }

    /**
     * Retrieves a {@link ProgramError} that represents the specified details.
     *
     * @param level     The severity of the error
     * @param message   The error message
     * @param exception The exception that caused the error.
     * @param details   The details of the exception
     *
     * @since 0.6.3m1
     * @return A corresponding ProgramError
     */
    protected ProgramError getError(final ErrorLevel level, final String message,
            final Throwable exception, final String details) {
        return new ProgramError(nextErrorID.getAndIncrement(), level, message, exception,
                details, new Date());
    }

    /**
     * Determines whether or not the specified exception is one that we are willing to report.
     *
     * @param exception The exception to test
     *
     * @since 0.6.3m1
     * @return True if the exception may be reported, false otherwise
     */
    protected boolean isValidError(final Throwable exception) {
        Throwable target = exception;

        while (target != null) {
            for (Class<?> bad : BANNED_EXCEPTIONS) {
                if (bad.equals(target.getClass())) {
                    return false;
                }
            }

            target = target.getCause();
        }

        return true;
    }

    /**
     * Sends an error to the developers.
     *
     * @param error error to be sent
     */
    public void sendError(final ProgramError error) {
        if (error.getReportStatus() != ErrorReportStatus.ERROR
                && error.getReportStatus() != ErrorReportStatus.WAITING) {
            return;
        }

        error.setReportStatus(ErrorReportStatus.QUEUED);

        reportQueue.add(error);

        if (reportThread == null || !reportThread.isAlive()) {
            reportThread = new ErrorReportingThread(reportQueue);
            reportThread.start();
        }
    }

    /**
     * Called when an error needs to be deleted from the list.
     *
     * @param error ProgramError that changed
     */
    public void deleteError(final ProgramError error) {
        synchronized (errors) {
            errors.remove(error);
        }

        fireErrorDeleted(error);
    }

    /**
     * Deletes all errors from the manager.
     *
     * @since 0.6.3m1
     */
    public void deleteAll() {
        synchronized (errors) {
            for (ProgramError error : errors) {
                fireErrorDeleted(error);
            }

            errors.clear();
        }
    }

    /**
     * Returns the number of errors.
     *
     * @return Number of ProgramErrors
     */
    public int getErrorCount() {
        return errors.size();
    }

    /**
     * Returns the list of program errors.
     *
     * @return Program error list
     */
    public List<ProgramError> getErrors() {
        synchronized (errors) {
            return new LinkedList<>(errors);
        }
    }

    /**
     * Adds an ErrorListener to the listener list.
     *
     * @param listener Listener to add
     */
    public void addErrorListener(final ErrorListener listener) {
        if (listener == null) {
            return;
        }

        errorListeners.add(ErrorListener.class, listener);
    }

    /**
     * Removes an ErrorListener from the listener list.
     *
     * @param listener Listener to remove
     */
    public void removeErrorListener(final ErrorListener listener) {
        errorListeners.remove(ErrorListener.class, listener);
    }

    /**
     * Fired when the program encounters an error.
     *
     * @param error Error that occurred
     */
    protected void fireErrorAdded(final ProgramError error) {
        int firedListeners = 0;

        for (ErrorListener listener : errorListeners.get(ErrorListener.class)) {
            if (listener.isReady()) {
                listener.errorAdded(error);
                firedListeners++;
            }
        }

        if (firedListeners == 0) {
            System.err.println("An error has occurred: " + error.getLevel()
                    + ": " + error.getMessage());

            for (String line : error.getTrace()) {
                System.err.println("\t" + line);
            }
        }
    }

    /**
     * Fired when the program encounters a fatal error.
     *
     * @param error Error that occurred
     */
    protected void fireFatalError(final ProgramError error) {
        final boolean restart;
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("A fatal error has occurred: " + error.getMessage());
            for (String line : error.getTrace()) {
                System.err.println("\t" + line);
            }
            restart = false;
        } else {
            final FatalErrorDialog fed = new FatalErrorDialog(error);
            fed.setVisible(true);
            try {
                synchronized (fed) {
                    while (fed.isWaiting()) {
                        fed.wait();
                    }
                }
            } catch (InterruptedException ex) {
                //Oh well, carry on
            }
            restart = fed.getRestart();
        }

        try {
            synchronized (error) {
                while (!error.getReportStatus().isTerminal()) {
                    error.wait();
                }
            }
        } catch (InterruptedException ex) {
            // Do nothing
        }

        if (restart) {
            System.exit(42);
        } else {
            System.exit(1);
        }
    }

    /**
     * Fired when an error is deleted.
     *
     * @param error Error that has been deleted
     */
    protected void fireErrorDeleted(final ProgramError error) {
        for (ErrorListener listener : errorListeners.get(ErrorListener.class)) {
            listener.errorDeleted(error);
        }
    }

    /**
     * Fired when an error's status is changed.
     *
     * @param error Error that has been altered
     */
    protected void fireErrorStatusChanged(final ProgramError error) {
        for (ErrorListener listener : errorListeners.get(ErrorListener.class)) {
            listener.errorStatusChanged(error);
        }
    }

    @Override
    public void configChanged(final String domain, final String key) {
        updateSettings();
    }

    /** Updates the settings used by this error manager. */
    protected void updateSettings() {
        try {
            sendReports = config.getOptionBool("general", "submitErrors")
                    && !config.getOptionBool("temp", "noerrorreporting");
            logReports = config.getOptionBool("general", "logerrors");
        } catch (IllegalArgumentException ex) {
            sendReports = false;
            logReports = true;
        }
    }

}
