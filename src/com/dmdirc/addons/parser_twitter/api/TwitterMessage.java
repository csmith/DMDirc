/*
 *  Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.dmdirc.addons.parser_twitter.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Used for direct messages.
 *
 * @author shane
 */
public class TwitterMessage {
    /** ID of this message. */
    final long id;

    /** Contents of this message. */
    final String message;

    /** Owner of this message. */
    final String sender;

    /** API Object that owns this. */
    private final TwitterAPI myAPI;

    /** Time this message was sent. */
    private final long time;

    /**
     * Create a new TwitterMessage
     *
     * @param api
     * @param message Message contents
     */
    protected TwitterMessage(final TwitterAPI api, final String message) {
        this(api, message, -1, null, System.currentTimeMillis());
    }

    /**
     * Create a new TwitterMessage
     *
     * @param api
     * @param message Message contents
     * @param id ID of message
     * @param sender User who send this.
     * @param time Time this message was sent
     */
    protected TwitterMessage(final TwitterAPI api, final String message, final long id, final String sender, final Long time) {
        this.myAPI = api;
        this.id = id;
        this.message = message;
        this.sender = sender;
        this.time = time;
    }

    /**
     * Create a twitter status from an element!
     *
     * @param api API that owns this.
     * @param node Node to use.
     */
    protected TwitterMessage(final TwitterAPI api, final Node node) {
        this(api, node, null);
    }

    /**
     * Create a twitter status from a node!
     *
     * @param api API that owns this.
     * @param node Node to use.
     * @param user User who this status belongs to.
     */
    protected TwitterMessage(final TwitterAPI api, final Node node, final String user) {
        if (!(node instanceof Element)) { throw new TwitterRuntimeException("Can only use Element type nodes for message creation."); }
        this.myAPI = api;
        final Element element = (Element) node;

        this.message = element.getElementsByTagName("text").item(0).getTextContent();

        final TwitterUser senderUser;
        if (user == null) {
            final NodeList nodes = element.getElementsByTagName("sender");
            if (nodes != null && nodes.getLength() > 0) {
                senderUser = new TwitterUser(api, nodes.item(0), null);
                this.sender = senderUser.getScreenName();
                myAPI.updateUser(senderUser);
            } else {
                senderUser = new TwitterUser(api, "unknown", -1, "realname", false);
                this.sender = senderUser.getScreenName();
            }

        } else {
            this.sender = user;
        }

        this.id = TwitterAPI.parseLong(element.getElementsByTagName("id").item(0).getTextContent(), -1);

        final String timeString = element.getElementsByTagName("created_at").item(0).getTextContent();
        long parsedTime = System.currentTimeMillis();
        try {
            parsedTime = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy").parse(timeString).getTime();
        } catch (ParseException ex) { }
        this.time = parsedTime;
    }

    /**
     * Get the screen name of the user who sent this message.
     *
     * @return Screen name of the user who sent this message.
     */
    public String getSenderScreenName() {
        return sender;
    }
    
    /**
     * Get the user who sent this message.
     *
     * @return The user who sent this message.
     */
    public TwitterUser getSender() {
        return myAPI.getCachedUser(sender);
    }
    
    /**
     * Get the ID of this message.
     *
     * @return ID of this message.
     */
    public long getID() {
        return id;
    }

    /**
     * Get the contents of this message.
     *
     * @return contents of this message.
     */
    public String getText() {
        return message;
    }
}
