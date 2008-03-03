/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.userlevel;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.parser.ChannelClientInfo;
import com.dmdirc.parser.ClientInfo;
import com.dmdirc.plugins.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows the client to assign user levels to users (based on hostname matches),
 * and for actions/plugins to check those levels.
 * 
 * @author chris
 */
public class UserLevelPlugin extends Plugin implements ActionListener {
    
    /** A map of hostmasks to associated level numbers. */
    private static Map<String, Integer> levels = new HashMap<String, Integer>();

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        ActionManager.addListener(this, CoreActionType.CHANNEL_JOIN);
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        ActionManager.removeListener(this);
    }

    /** {@inheritDoc} */
    public void processEvent(final ActionType type, final StringBuffer format,
                             final Object... arguments) {
        switch ((CoreActionType) type) {
            case CHANNEL_JOIN:
                doChannelLevel((ChannelClientInfo) arguments[1]);
                break;
        }
    }
    
    /**
     * Updates the specified channel client's channel user level.
     * 
     * @param client The client whose user level is to be updated
     */
    protected static void doChannelLevel(final ChannelClientInfo client) {
        doGlobalLevel(client.getClient());
    }
    
    /**
     * Updates the specified client's global user level.
     * 
     * @param client The client whose user level is to be updated
     */
    @SuppressWarnings("unchecked")
    protected static void doGlobalLevel(final ClientInfo client) {
        final String host = client.getNickname() + "!" + client.getIdent()
                + "@" + client.getHost();
        
        int level = 0;
        
        synchronized(levels) {
            for (Map.Entry<String, Integer> entry : levels.entrySet()) {
                if (host.matches(entry.getKey())) {
                    level = Math.max(level, entry.getValue());
                }
            }
        }
        
        client.getMap().put("level", level);
    }

}
