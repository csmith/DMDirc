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

import com.dmdirc.parser.common.ChannelListModeItem;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * ChannelInfo class for the Twitter plugin.
 *
 * @author shane
 */
public class TwitterChannelInfo implements ChannelInfo {
    /** Name of this channel. */
    private final String myName;

    /** The Parser that owns this object. */
    private final Twitter myParser;
    
    /** Topic of this channel. */
    private String myTopic = "";

    /** When was the topic set? */
    private long topicTime = 0;

    /** Who set the topic? */
    private String topicSetter = "";

    /** Known clients of this channel. */
    private Map<String, TwitterChannelClientInfo> channelClients = new HashMap<String, TwitterChannelClientInfo>();

    /**
     * Create a new TwitterChannelInfo.
     *
     * @param myName Name of this channel
     * @param myParser parser that owns this TwitterChannelInfo
     */
    public TwitterChannelInfo(final String myName, final Twitter myParser) {
        this.myName = myName;
        this.myParser = myParser;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return myName;
    }

    /** {@inheritDoc} */
    @Override
    public void setTopic(final String topic) {
        myParser.setStatus(topic);
    }

    /** {@inheritDoc} */
    @Override
    public String getTopic() {
        return myTopic;
    }

    /** {@inheritDoc} */
    @Override
    public long getTopicTime() {
        return topicTime;
    }

    /** {@inheritDoc} */
    @Override
    public String getTopicSetter() {
        return topicSetter;
    }

    /** {@inheritDoc} */
    @Override
    public String getModes() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public String getMode(final char mode) {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public void sendMessage(final String message) {
        myParser.sendMessage(myName, message);
    }

    /** {@inheritDoc} */
    @Override
    public void sendAction(final String action) {
        myParser.sendAction(myName, action);
    }

    /** {@inheritDoc} */
    @Override
    public void part(final String reason) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void sendWho() {
        return;
    }

    /** {@inheritDoc} */
    @Override
    public void alterMode(final boolean add, final Character mode, final String parameter) {
        return;
    }

    /** {@inheritDoc} */
    @Override
    public void flushModes() {
        return;
    }

    /** {@inheritDoc} */
    @Override
    public ChannelClientInfo getChannelClient(final ClientInfo client) {
        return getChannelClient(client.getNickname());
    }

    /** {@inheritDoc} */
    @Override
    public ChannelClientInfo getChannelClient(final String client) {
        return channelClients.containsKey(client.toLowerCase()) ? channelClients.get(client.toLowerCase()) : null;
    }

    /** {@inheritDoc} */
    @Override
    public ChannelClientInfo getChannelClient(final String client, final boolean create) {
        ChannelClientInfo cci = getChannelClient(client);
        if (create && cci == null) {
            cci = new TwitterChannelClientInfo(this, (TwitterClientInfo)myParser.getClient(client));
        }
        return cci;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<ChannelClientInfo> getChannelClients() {
        return new ArrayList<ChannelClientInfo>(channelClients.values());
    }

    /** {@inheritDoc} */
    @Override
    public int getChannelClientCount() {
        return channelClients.size();
    }

    /** {@inheritDoc} */
    @Override
    public Parser getParser() {
        return myParser;
    }

    /**
     * Add a channel client to this channel.
     * 
     * @param cci Channel Client to add.
     */
    public void addChannelClient(final TwitterChannelClientInfo cci) {
        channelClients.put(cci.getClient().getNickname().toLowerCase(), cci);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<ChannelListModeItem> getListMode(final char mode) {
        return new ArrayList<ChannelListModeItem>();
    }

    /**
     * Set the topic for this channel.
     *
     * @param string Topic to set.
     */
    void setLocalTopic(final String string) {
        myTopic = string;
    }

    /**
     * Set the topic time for this channel.
     *
     * @param newValue New Time
     */
    public void setTopicTime(final long newValue) {
        topicTime = newValue;
    }

    /**
     * Set the topic setter for this channel.
     *
     * @param newValue New Setter
     */
    public void setTopicSetter(final String newValue) {
        topicSetter = newValue;
    }
}
