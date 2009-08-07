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
 * Used for status messages.
 *
 * @author shane
 */
public class TwitterStatus implements Comparable<TwitterStatus> {
    /** The ID this message was in reply to. */
    private long replyID;

    /** The ID of this message */
    private final long id;

    /** The time of this message */
    private final long time;

    /** The user who owns this message. */
    private final String user;

    /** The contents of this message. */
    private final String message;

    /** API Object that owns this. */
    private final TwitterAPI myAPI;

    /**
     * Create a new TwitterStatus.
     * (Used for creating new status updates.)
     *
     * @param api API that owns this.
     * @param message Message for this status.
     * @param replyID Id to reply to (or -1);
     */
    protected TwitterStatus(final TwitterAPI api, final String message, final long replyID) {
        this(api, message, replyID, -1, null, System.currentTimeMillis());
    }

    /**
     * Create a new TwitterStatus.
     *
     * @param api API that owns this.
     * @param replyID ID this is in reply to
     * @param id ID of this message
     * @param user User for this message
     * @param message Message!
     * @param time
     */
    protected TwitterStatus(final TwitterAPI api, final String message, final long replyID, final long id, final String user, final long time) {
        this.myAPI = api;
        this.replyID = replyID;
        this.id = id;
        this.user = user;
        this.message = message;
        this.time = time;

        api.updateStatus(this);
    }

    /**
     * Create a twitter status from an element!
     *
     * @param api API that owns this.
     * @param node Node to use.
     */
    protected TwitterStatus(final TwitterAPI api, final Node node) {
        this(api, node, null);
    }

    /**
     * Create a twitter status from a node!
     *
     * @param api API that owns this.
     * @param node Node to use.
     * @param user User who this status belongs to.
     */
    protected TwitterStatus(final TwitterAPI api, final Node node, final String user) {
        if (!(node instanceof Element)) { throw new TwitterRuntimeException("Can only use Element type nodes for status creation."); }
        this.myAPI = api;
        final Element element = (Element) node;

        this.message = element.getElementsByTagName("text").item(0).getTextContent();

        final TwitterUser userUser;
        if (user == null) {
            final NodeList nodes = element.getElementsByTagName("user");
            if (nodes != null && nodes.getLength() > 0) {
                userUser = new TwitterUser(api, nodes.item(0), this);
                this.user = userUser.getScreenName();
                myAPI.updateUser(userUser);
            } else {
                userUser = new TwitterUser(api, "name", -1, "realname", false);
                this.user = userUser.getScreenName();
            }
        } else {
            this.user = user;
        }

        this.id = TwitterAPI.parseLong(element.getElementsByTagName("id").item(0).getTextContent(), -1);
        this.replyID = TwitterAPI.parseLong(element.getElementsByTagName("in_reply_to_status_id").item(0).getTextContent(), -1);
        
        final String timeString = element.getElementsByTagName("created_at").item(0).getTextContent();
        long parsedTime = System.currentTimeMillis();
        try {
            parsedTime = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy").parse(timeString).getTime();
        } catch (ParseException ex) { }
        this.time = parsedTime;

        api.updateStatus(this);
    }


    /**
     * Get the ID of this message
     *
     * @return ID of this message. (-1 if not known)
     */
    public long getID() {
        return this.id;
    }

    /**
     * Get the owner of this message
     *
     * @return owner of this message. (null if not known)
     */
    public TwitterUser getUser() {
        return myAPI.getUser(this.user);
    }

    /**
     * Get the contents of this message
     *
     * @return contents of this message.
     */
    public String getText() {
        return this.message;
    }

    /**
     * What message is this in reply to?
     *
     * @return reply ID or 01 if this message isn't replying to anything.
     */
    public long getReplyTo() {
        return this.replyID;
    }

    /**
     * What time was this message sent?
     *
     * @return Time this message was sent.
     */
    public long getTime() {
        return this.time;
    }

    /**
     * Is this equal to the given Status?
     * @param status
     * @return 
     */
    @Override
    public boolean equals(final Object status) {
        if (status instanceof TwitterStatus) {
            return ((TwitterStatus)status).getID() == this.id;
        } else {
            return false;
        }
    }

    /**
     * Generate hashCode for this object.
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    /**
     * Compare the given object to this one.
     *
     * @param arg0
     * @return Comparison
     */
    @Override
    public int compareTo(final TwitterStatus arg0) {
        if (this.time < arg0.getTime()) {
            return -1;
        } else if (this.time > arg0.getTime()) {
            return 1;
        } else {
            return 0;
        }
    }
}
