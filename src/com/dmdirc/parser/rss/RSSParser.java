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

package com.dmdirc.parser.rss;

import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.LocalClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.irc.IRCStringConverter;
import com.dmdirc.parser.irc.RegexStringList;
import com.dmdirc.parser.irc.callbacks.CallbackManager;

import java.util.Collection;

/**
 *
 * @author chris
 */
public class RSSParser implements Parser {

    @Override
    public void disconnect(final String message) {
        // Do nothing
    }

    @Override
    public void joinChannel(String channel) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void joinChannel(String channel, String key) {
        // Do nothing
    }

    @Override
    public ChannelInfo getChannel(String channel) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends ChannelInfo> getChannels() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setBindIP(String ip) {
        // Do nothing
    }

    @Override
    public int getMaxLength(String type, String target) {
        return 0;
    }

    @Override
    public LocalClientInfo getLocalClient() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ClientInfo getClient(String details) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendRawMessage(String message) {
        // Do nothing
    }

    @Override
    public IRCStringConverter getStringConverter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isValidChannelName(String name) {
        return name != null && !name.isEmpty() && name.startsWith("#");
    }

    @Override
    public String getServerName() {
        return "RSS viewer";
    }

    @Override
    public String getNetworkName() {
        return ".RSS";
    }

    @Override
    public String getServerSoftware() {
        return "RSS";
    }

    @Override
    public String getServerSoftwareType() {
        return "RSS";
    }

    @Override
    public int getMaxTopicLength() {
        return 0;
    }

    @Override
    public String getBooleanChannelModes() {
        return "";
    }

    @Override
    public String getListChannelModes() {
        return "";
    }

    @Override
    public int getMaxListModes(char mode) {
        return 0;
    }

    @Override
    public boolean isUserSettable(char mode) {
        return false;
    }

    @Override
    public String getParameterChannelModes() {
        return "";
    }

    @Override
    public String getDoubleParameterChannelModes() {
        return "";
    }

    @Override
    public String getUserModes() {
        return "";
    }

    @Override
    public String getChannelUserModes() {
        return "";
    }

    @Override
    public CallbackManager getCallbackManager() {
        return new CallbackManager(null);
    }

    @Override
    public long getServerLatency() {
        return 0l;
    }

    @Override
    public void sendCTCP(String target, String type, String message) {
        // Do nothing
    }

    @Override
    public void sendCTCPReply(String target, String type, String message) {
        // Do nothing
    }

    @Override
    public void sendMessage(String target, String message) {
        // Do nothing
    }

    @Override
    public void sendNotice(String target, String message) {
        // Do nothing
    }

    @Override
    public void sendAction(String target, String message) {
        // Do nothing
    }

    @Override
    public String getLastLine() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setIgnoreList(RegexStringList ignoreList) {
        // Do nothing
    }

    @Override
    public RegexStringList getIgnoreList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] parseHostmask(String hostmask) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public long getPingTime() {
        return 1l;
    }

    @Override
    public void setPingTimerInterval(long newValue) {
        // Do nothing
    }

    @Override
    public long getPingTimerInterval() {
        return 1l;
    }

    @Override
    public void setPingTimerFraction(int newValue) {
        // Do nothing
    }

    @Override
    public int getPingTimerFraction() {
        return 1;
    }

    @Override
    public void run() {
        // Do nothing
    }

}
