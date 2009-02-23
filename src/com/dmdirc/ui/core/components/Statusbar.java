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

package com.dmdirc.ui.core.components;

import com.dmdirc.FrameContainer;
import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.parser.irc.ClientInfo;
import com.dmdirc.ui.interfaces.StatusBar;
import com.dmdirc.ui.interfaces.StatusMessageNotifier;

/**
 * Listens for relevant actions and shows status bar messages informing the
 * user of events.
 *
 * @author chris
 */
public class Statusbar implements ActionListener {

    private final StatusBar statusbar;

    public Statusbar(final StatusBar statusbar) {
        this.statusbar = statusbar;
        
        ActionManager.addListener(this,
                CoreActionType.SERVER_CONNECTED,
                CoreActionType.SERVER_INVITERECEIVED,
                CoreActionType.CLIENT_OPENED);
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        switch ((CoreActionType) type) {
            case SERVER_CONNECTED:
                statusbar.setMessage("server",
                        "Connected to " + ((Server) arguments[0]).getName(),
                        new WindowActivationNotifier(((Server) arguments[0])));
                break;
            case SERVER_INVITERECEIVED:
                statusbar.setMessage("invite",
                        ((ClientInfo) arguments[1]).getNickname()
                        + " has invited you to " + arguments[2],
                        new JoinChannelNotifier(((Server) arguments[0]), (String) arguments[2]));
                break;
            case CLIENT_OPENED:
                statusbar.setMessage("icon", "Welcome to DMDirc " + Main.VERSION);
                break;
        }
    }

    protected class WindowActivationNotifier implements StatusMessageNotifier {

        private final FrameContainer target;

        public WindowActivationNotifier(final FrameContainer target) {
            this.target = target;
        }

        /** {@inheritDoc} */
        @Override
        public void clickReceived(final int mousebutton, final int clickCount) {
            target.activateFrame();
            statusbar.clearMessage();
        }

    }

    protected class JoinChannelNotifier implements StatusMessageNotifier {

        private final Server server;
        private final String channel;

        public JoinChannelNotifier(final Server server, final String channel) {
            this.server = server;
            this.channel = channel;
        }

        /** {@inheritDoc} */
        @Override
        public void clickReceived(final int mousebutton, final int clickCount) {
            server.join(channel);
            statusbar.clearMessage();
        }

    }

}
