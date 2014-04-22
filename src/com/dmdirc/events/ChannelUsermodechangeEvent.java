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

package com.dmdirc.events;

import com.dmdirc.Channel;
import com.dmdirc.parser.interfaces.ChannelClientInfo;

/**
 * Fired when a channel user mode change occurs.
 */
public class ChannelUsermodechangeEvent extends ChannelDisplayableEvent {

    private final ChannelClientInfo user;
    private final ChannelClientInfo victim;
    private final String modes;

    public ChannelUsermodechangeEvent(final long timestamp, final Channel channel,
            final ChannelClientInfo user, final ChannelClientInfo victim, final String modes) {
        super(timestamp, channel);
        this.user = user;
        this.victim = victim;
        this.modes = modes;
    }

    public ChannelUsermodechangeEvent(final Channel channel,
            final ChannelClientInfo user, final ChannelClientInfo victim, final String modes) {
        super(channel);
        this.user = user;
        this.victim = victim;
        this.modes = modes;
    }

    public ChannelClientInfo getUser() {
        return user;
    }

    public ChannelClientInfo getVictim() {
        return victim;
    }

    public String getModes() {
        return modes;
    }

}