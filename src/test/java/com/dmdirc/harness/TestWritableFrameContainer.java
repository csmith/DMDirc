/*
 * Copyright (c) 2006-2017 DMDirc Developers
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

package com.dmdirc.harness;

import com.dmdirc.DefaultInputModel;
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.ui.input.TabCompleterImpl;
import com.dmdirc.ui.messages.BackBufferFactory;

import java.util.Collections;
import java.util.Optional;

public class TestWritableFrameContainer extends FrameContainer {

    public TestWritableFrameContainer(final int lineLength,
            final AggregateConfigProvider cm, final CommandManager commandManager,
            final EventBus eventBus,
            final BackBufferFactory backBufferFactory) {
        super("raw", "Raw", "(Raw)", cm, backBufferFactory,
                eventBus,
                Collections.<String>emptySet());

        setInputModel(
                new DefaultInputModel(
                        line -> {},
                        new GlobalCommandParser(cm, commandManager, eventBus),
                        new TabCompleterImpl(cm),
                        () -> lineLength));
    }

    @Override
    public Optional<Connection> getConnection() {
        return Optional.empty();
    }

}
