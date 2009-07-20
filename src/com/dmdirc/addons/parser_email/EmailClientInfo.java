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

import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import java.util.HashMap;
import java.util.Map;
import javax.mail.internet.InternetAddress;

/**
 *
 * @author chris
 */
public class EmailClientInfo implements ClientInfo {

    private final EmailParser parser;

    private final InternetAddress address;

    private final Map<Object, Object> map = new HashMap<Object, Object>();

    public EmailClientInfo(final EmailParser parser, final InternetAddress address) {
        this.parser = parser;
        this.address = address;
    }

    @Override
    public String getNickname() {
        return address.getPersonal() == null ? getUsername() : address.getPersonal();
    }

    @Override
    public String getUsername() {
        return address.getAddress().split("@", 1)[0];
    }

    @Override
    public String getHostname() {
        return address.getAddress().split("@", 2)[0];
    }

    @Override
    public String getRealname() {
        return getNickname();
    }

    @Override
    public int getChannelCount() {
        return 0;
    }

    @Override
    public Map<Object, Object> getMap() {
        return map;
    }

    @Override
    public Parser getParser() {
        return parser;
    }

}
