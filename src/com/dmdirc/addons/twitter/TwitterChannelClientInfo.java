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
package com.dmdirc.addons.twitter;

import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import java.util.Map;
import java.util.HashMap;

/**
 * ChannelClientInfo class for the Twitter plugin.
 *
 * @author shane
 */
public class TwitterChannelClientInfo implements ChannelClientInfo {
    /** My ChannelInfo.  */
    private TwitterChannelInfo myChannel;
    
    /** My ClientInfo. */
    private TwitterClientInfo myClient;

    /** Map of random objects. */
    final Map<Object, Object> myMap = new HashMap<Object, Object>();

    /**
     * Create a new TwitterChannelClientInfo
     *
     * @param channel Channel that this client is in.
     * @param ci Client that we represent.
     */
    TwitterChannelClientInfo(TwitterChannelInfo channel, TwitterClientInfo ci) {
        this.myChannel = channel;
        this.myClient = ci;
    }

    /** {@inheritDoc} */
    @Override
    public ClientInfo getClient() {
        return myClient;
    }

    /** {@inheritDoc} */
    @Override
    public ChannelInfo getChannel() {
        return myChannel;
    }

    /** {@inheritDoc} */
    @Override
    public String getImportantModePrefix() {
        return myClient.getUser().getFollowing() ? "+" : "";
    }

    /** {@inheritDoc} */
    @Override
    public String getImportantMode() {
        return myClient.getUser().getFollowing() ? "v" : "";
    }

    public int getImportantModeValue() {
        return myClient.getUser().getFollowing() ? 1 : 0;
    }

    /** {@inheritDoc} */
    @Override
    public String getAllModes() {
        return myClient.getUser().getFollowing() ? "v" : "";
    }

    /** {@inheritDoc} */
    @Override
    public Map<Object, Object> getMap() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void kick(String message) {
        ((Twitter)myClient.getParser()).getApi().destroyFriendship(myClient.getUser().getScreenName()).build().post();
    }

    /**
     * Compare this TwitterChannelClientInfo to another.
     *
     * @param arg0
     * @return
     */
    @Override
    public int compareTo(final ChannelClientInfo arg0) {
        if (arg0 instanceof TwitterChannelClientInfo) {
            final TwitterChannelClientInfo other = (TwitterChannelClientInfo) arg0;
            return (this.getImportantModeValue() - other.getImportantModeValue());
        }

        return 0;
    }
}
