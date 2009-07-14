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

import com.dmdirc.addons.twitter.api.TwitterUser;
import com.dmdirc.parser.interfaces.LocalClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.ChannelNickChangeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelUserModeChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClientInfo class for the Twitter plugin.
 *
 * @author shane
 */
public class TwitterClientInfo implements LocalClientInfo {
    /** This Clients User */
    private TwitterUser myUser;

    /** My Parser */
    private Twitter myParser;

    /** Map of random objects. */
    final Map<Object, Object> myMap = new HashMap<Object, Object>();

    /** List of channels I am in. */
    final List<TwitterChannelInfo> channels = new ArrayList<TwitterChannelInfo>();

    /**
     * Parse an IRC Hostname into its separate parts.
     *
     * @param hostname Hostname to parse
     * @return String array of nick, ident and host.
     */
    static String[] parseHostFull(String hostname) {
        String[] temp = null;
        final String[] result = new String[3];
        if (!hostname.isEmpty() && hostname.charAt(0) == ':') { hostname = hostname.substring(1); }
        temp = hostname.split("@", 2);
        if (temp.length == 1) { result[2] = ""; } else { result[2] = temp[1]; }
        temp = temp[0].split("!", 2);
        if (temp.length == 1) { result[1] = ""; } else { result[1] = temp[1]; }
        result[0] = temp[0];

        return result;
    }

    /**
     * Return the nickname from an irc hostname.
     *
     * @param hostname host to parse
     * @return nickname
     */
    static String parseHost(final String hostname) {
        return parseHostFull(hostname)[0];
    }

    /**
     * Create a new TwitterClientInfo
     *
     * @param user User object for this client.
     * @param parser Parser that owns this client.
     */
    public TwitterClientInfo(final TwitterUser user, final Twitter parser) {
        this.myUser = user;
        this.myParser = parser;
    }

    /** {@inheritDoc} */
    @Override
    public void setNickname(final String name) {
        if (this == myParser.getLocalClient()) {
            // TODO: throw new UnsupportedOperationException("Not supported yet.");
        } else {
            // TODO: throw new UnsupportedOperationException("Can not set nickname on non-local clients");
        }
    }

    /**
     * Get the user object for this client.
     *
     * @return User object for this client.
     */
    public TwitterUser getUser() {
        return myUser;
    }

    /**
     * Change the user object for this client.
     *
     * @param newUser new User object for this client.
     */
    public void setUser(final TwitterUser newUser) {
        final TwitterUser oldUser = myUser;
        myUser = newUser;

        // Check if user nickname changed.
        if (!newUser.getScreenName().equalsIgnoreCase(oldUser.getScreenName())) {
            myParser.renameClient(this, newUser.getScreenName());
            for (TwitterChannelInfo ci : channels) {
                myParser.getCallbackManager().getCallbackType(ChannelNickChangeListener.class).call(ci, ci.getChannelClient(this), oldUser.getScreenName());
            }
        }
        // Check if friendship status changed.
        if (newUser.isFollowing() != myUser.isFollowing()) {
            for (TwitterChannelInfo ci : channels) {
                final char type = newUser.isFollowing() ? '+' : '-';
                myParser.getCallbackManager().getCallbackType(ChannelUserModeChangeListener.class).call(ci, ci.getChannelClient(this), null, "twitter.com", type+"v");
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getModes() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public void setAway(final String reason) {
        return;
    }

    /** {@inheritDoc} */
    @Override
    public void setBack() {
        return;
    }

    /** {@inheritDoc} */
    @Override
    public void alterMode(final boolean add, final Character mode) {
        return;
    }

    /** {@inheritDoc} */
    @Override
    public void flushModes() {
        return;
    }

    /** {@inheritDoc} */
    @Override
    public String getNickname() {
        return myUser.getScreenName();
    }

    /** {@inheritDoc} */
    @Override
    public String getUsername() {
        return "user";
    }

    /** {@inheritDoc} */
    @Override
    public String getHostname() {
        return "twitter.com";
    }

    /** {@inheritDoc} */
    @Override
    public String getRealname() {
        return String.format("%s - http://%s/%s", myUser.getRealName(), getHostname(), getNickname());
    }

    /** {@inheritDoc} */
    @Override
    public int getChannelCount() {
        synchronized (channels) {
            return channels.size();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Map<Object, Object> getMap() {
        return myMap;
    }

    /** {@inheritDoc} */
    @Override
    public Parser getParser() {
        return myParser;
    }

    /**
     * Add this client to the given channel.
     * 
     * @param channel channel to add us to.
     */
    public void addChannel(final TwitterChannelInfo channel) {
        synchronized (channels) {
            if (!channels.contains(channel)) {
                channels.add(channel);
            }
        }
    }

    /**
     * Remove this client from the given channel.
     *
     * @param channel channel to remove us from.
     */
    public void delChannel(final TwitterChannelInfo channel) {
        synchronized (channels) {
            if (channels.contains(channel)) {
                channels.remove(channel);
            }
        }
    }

}
