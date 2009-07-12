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
    public TwitterChannelInfo(String myName, Twitter myParser) {
        this.myName = myName;
        this.myParser = myParser;
    }

    @Override
    public String getName() {
        return myName;
    }

    @Override
    public void setTopic(String topic) {
        // Todo: This should tell the main twitter parser that we have changed
        // the topic, which may or may not do something.
    }

    @Override
    public String getTopic() {
        return myTopic;
    }

    @Override
    public long getTopicTime() {
        return topicTime;
    }

    @Override
    public String getTopicSetter() {
        return topicSetter;
    }

    @Override
    public String getModes() {
        return "";
    }

    @Override
    public String getMode(char mode) {
        return "";
    }

    @Override
    public void sendMessage(String message) {
        myParser.sendMessage(myName, message);
    }

    @Override
    public void sendAction(String action) {
        myParser.sendAction(myName, action);
    }

    @Override
    public void part(String reason) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendWho() {
        return;
    }

    @Override
    public void alterMode(boolean add, Character mode, String parameter) {
        return;
    }

    @Override
    public void flushModes() {
        return;
    }

    @Override
    public ChannelClientInfo getChannelClient(ClientInfo client) {
        return getChannelClient(client.getNickname());
    }

    @Override
    public ChannelClientInfo getChannelClient(String client) {
        return channelClients.containsKey(client.toLowerCase()) ? channelClients.get(client.toLowerCase()) : null;
    }

    @Override
    public ChannelClientInfo getChannelClient(String client, boolean create) {
        ChannelClientInfo cci = getChannelClient(client);
        if (create && cci == null) {
            cci = new TwitterChannelClientInfo(this, (TwitterClientInfo)myParser.getClient(client));
        }
        return cci;
    }

    @Override
    public Collection<ChannelClientInfo> getChannelClients() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getChannelClientCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Parser getParser() {
        return myParser;
    }

    void addChannelClient(TwitterChannelClientInfo cci) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Collection<ChannelListModeItem> getListMode(char mode) {
        return new ArrayList<ChannelListModeItem>();
    }

    void setLocalTopic(String string) {
        myTopic = string;
    }
}
