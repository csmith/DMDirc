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

import com.dmdirc.parser.interfaces.LocalClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import java.util.HashMap;
import java.util.Map;
import net.unto.twitter.TwitterProtos.User;

/**
 *
 * @author shane
 */
public class TwitterClientInfo implements LocalClientInfo {
    /** This Clients User */
    private User myUser;

    /** My Parser */
    private Twitter myParser;

    /** Map of random objects. */
    final Map<Object, Object> myMap = new HashMap<Object, Object>();

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

    static String parseHost(String hostname) {
        return parseHostFull(hostname)[0];
    }

    public TwitterClientInfo(final User user, final Twitter parser) {
        this.myUser = user;
        this.myParser = parser;
    }

    @Override
    public void setNickname(String name) {
        if (this == myParser.getLocalClient()) {
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            throw new UnsupportedOperationException("Can not set nickname on non-local clients");
        }
    }

    public User getUser() {
        return myUser;
    }

    @Override
    public String getModes() {
        return "";
    }

    @Override
    public void setAway(String reason) {
        return;
    }

    @Override
    public void setBack() {
        return;
    }

    @Override
    public void alterMode(boolean add, Character mode) {
        return;
    }

    @Override
    public void flushModes() {
        return;
    }

    @Override
    public String getNickname() {
        return myUser.getScreenName();
    }

    @Override
    public String getUsername() {
        return "user";
    }

    @Override
    public String getHostname() {
        return "twitter.com";
    }

    @Override
    public String getRealname() {
        return String.format("%s - http://%s/%s", myUser.getName(), getHostname(), getNickname());
    }

    @Override
    public int getChannelCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<Object, Object> getMap() {
        return myMap;
    }

    @Override
    public Parser getParser() {
        return myParser;
    }

}
