/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.ui.core.errors;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.NonFatalProgramErrorEvent;
import com.dmdirc.events.ProgramErrorDeletedEvent;
import com.dmdirc.events.ProgramErrorStatusEvent;
import com.dmdirc.interfaces.ui.ErrorsDialogModel;
import com.dmdirc.interfaces.ui.ErrorsDialogModelListener;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.logger.ErrorReportStatus;
import com.dmdirc.logger.ProgramError;
import com.dmdirc.util.collections.ListenerList;

import com.google.common.base.Throwables;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import net.engio.mbassy.listener.Handler;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Basic implementation for a {@link ErrorsDialogModel}.
 */
public class CoreErrorsDialogModel implements ErrorsDialogModel {

    private final ListenerList listenerList;
    private final ErrorManager errorManager;
    private final DMDircMBassador eventBus;
    private Optional<DisplayableError> selectedError;

    @Inject
    public CoreErrorsDialogModel(final ErrorManager errorManager,
            final DMDircMBassador eventBus) {
        this.listenerList = new ListenerList();
        this.errorManager = errorManager;
        this.eventBus = eventBus;
        selectedError = Optional.empty();
    }

    @Override
    public void load() {
        eventBus.subscribe(this);
    }

    @Override
    public void unload() {
        eventBus.unsubscribe(this);
    }

    @Override
    public Set<DisplayableError> getErrors() {
        final Set<DisplayableError> errors = new HashSet<>();
        errorManager.getErrors().forEach(e -> errors.add(getDisplayableError(e)));
        return errors;
    }

    @Override
    public Optional<DisplayableError> getSelectedError() {
        return selectedError;
    }

    @Override
    public void setSelectedError(final Optional<DisplayableError> selectedError) {
        checkNotNull(selectedError);
        this.selectedError = selectedError;
        listenerList.getCallable(ErrorsDialogModelListener.class).selectedErrorChanged(selectedError);
    }

    @Override
    public void deleteSelectedError() {
        selectedError.ifPresent(e -> {
            errorManager.deleteError(e.getProgramError());
            setSelectedError(Optional.empty());
        });
    }

    @Override
    public void deleteAllErrors() {
        errorManager.deleteAll();
    }

    @Override
    public void sendSelectedError() {
        selectedError.map(DisplayableError::getProgramError).ifPresent(errorManager::sendError);
    }

    @Override
    public boolean isDeletedAllowed() {
        return selectedError.isPresent();
    }

    @Override
    public boolean isDeleteAllAllowed() {
        return !errorManager.getErrors().isEmpty();
    }

    @Override
    public boolean isSendAllowed() {
        final ErrorReportStatus status = selectedError.map(DisplayableError::getReportStatus)
                .orElse(ErrorReportStatus.NOT_APPLICABLE);
        return status == ErrorReportStatus.WAITING || status == ErrorReportStatus.ERROR;
    }

    @Override
    public void addListener(final ErrorsDialogModelListener listener) {
        checkNotNull(listener);
        listenerList.add(ErrorsDialogModelListener.class, listener);
    }

    @Override
    public void removeListener(final ErrorsDialogModelListener listener) {
        checkNotNull(listener);
        listenerList.remove(ErrorsDialogModelListener.class, listener);
    }

    @Handler
    public void handleErrorStatusChanged(final ProgramErrorStatusEvent event) {
        listenerList.getCallable(ErrorsDialogModelListener.class)
                .errorStatusChanged(getDisplayableError(event.getError()));
    }

    @Handler
    public void handleErrorDeleted(final ProgramErrorDeletedEvent event) {
        listenerList.getCallable(ErrorsDialogModelListener.class)
                .errorDeleted(getDisplayableError(event.getError()));
    }

    @Handler
    public void handleErrorAdded(final NonFatalProgramErrorEvent event) {
        listenerList.getCallable(ErrorsDialogModelListener.class)
                .errorAdded(getDisplayableError(event.getError()));
    }

    private DisplayableError getDisplayableError(final ProgramError error) {
        final String details;
        if (error.getDetails().isEmpty()) {
            details = error.getMessage()
                    + '\n' + getThrowableAsString(error.getThrowable());
        } else {
            details = error.getMessage()
                    + '\n' + error.getDetails()
                    + '\n' + getThrowableAsString(error.getThrowable());
        }
        return DisplayableError.create(error.getDate(), error.getMessage(), details,
                error.getLevel(), error.getReportStatus(), error);
    }

    private String getThrowableAsString(final Throwable throwable) {
        if (throwable == null) {
            return "";
        } else {
            return Throwables.getStackTraceAsString(throwable);
        }
    }
}
