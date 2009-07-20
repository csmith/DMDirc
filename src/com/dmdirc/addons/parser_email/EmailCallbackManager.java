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

package com.dmdirc.addons.parser_email;

import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.common.CallbackObject;
import com.dmdirc.parser.common.CallbackObjectSpecific;
import com.dmdirc.parser.interfaces.callbacks.CallbackInterface;

/**
 *
 * @author chris
 */
public class EmailCallbackManager extends CallbackManager<EmailParser> {

    public EmailCallbackManager(final EmailParser parser) {
        super(parser);
    }

    @Override
    protected CallbackObject getCallbackObject(final EmailParser parser,
            final Class<?> type) {
        return new EmailCallbackObject(parser, this, type.asSubclass(CallbackInterface.class));
    }

    @Override
    protected CallbackObjectSpecific getSpecificCallbackObject(final EmailParser parser,
            final Class<?> type) {
        return new EmailSpecificCallbackObject(parser, this,
                type.asSubclass(CallbackInterface.class));
    }

}
