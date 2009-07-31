/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.addons.parser_twitter.api;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author shane
 */
public class TwitterUser {
    /** What is the screen name of this user? */
    private final String screenName;

    /** What is the user id of this user? */
    private final long userID;
    
    /** What is the real name of this user? */
    private final String realName;

    /** Are we following the user? */
    private final boolean following;

    /** Is the user following us? */
    private boolean followingUs = false;

    /** What was the last status of this user? */
    private final TwitterStatus lastStatus;

    /** API Object that owns this. */
    private final TwitterAPI myAPI;

    /**
     * Create a unknown TwitterUser
     *
     * @param api 
     * @param screenName Screen name for the user.
     */
    protected TwitterUser(final TwitterAPI api, final String screenName) {
        this(api, screenName, -1, "", false);
    }

    /**
     * Create a new TwitterUser
     *
     * @param api
     * @param screenName Screen name for the user.
     * @param userID User ID for the user.
     * @param realName Realname for the user.
     * @param following Are we following the user?
     */
    protected TwitterUser(final TwitterAPI api, final String screenName, final long userID, final String realName, final boolean following) {
        this.myAPI = api;
        this.screenName = screenName;
        this.userID = userID;
        this.realName = realName;
        this.following = following;
        this.lastStatus = null;
    }

    /**
     * Create a twitter user from a node!
     *
     * @param api
     * @param node Node to use.
     */
    protected TwitterUser(final TwitterAPI api, final Node node) {
        this(api, node, null);
    }

    /**
     * Create a twitter user from a node, with a pre-defined status.
     *
     * @param api
     * @param node Node to use.
     * @param status Status to use
     */
    protected TwitterUser(final TwitterAPI api, final Node node, final TwitterStatus status) {
        if (!(node instanceof Element)) { throw new TwitterRuntimeException("Can only use Element type nodes for user creation."); }

        final Element element = (Element) node;
        this.myAPI = api;
        this.realName = element.getElementsByTagName("name").item(0).getTextContent();
        this.screenName = element.getElementsByTagName("screen_name").item(0).getTextContent();
        
        this.userID = TwitterAPI.parseLong(element.getElementsByTagName("id").item(0).getTextContent(), -1);
        this.following = TwitterAPI.parseBoolean(element.getElementsByTagName("following").item(0).getTextContent());

        // Check to see if a cached user object for us exists that we can
        // take the status from.
        final TwitterUser oldUser = api.getCachedUser(this.screenName);

        if (status == null) {
            if (oldUser != null) {
                this.lastStatus = oldUser.getStatus();
            } else {
                final NodeList nodes = element.getElementsByTagName("status");
                if (nodes != null && nodes.getLength() > 0) {
                    this.lastStatus = new TwitterStatus(api, nodes.item(0), this.getScreenName());
                } else {
                    this.lastStatus = new TwitterStatus(api, "", -1, -1, this.getScreenName(), System.currentTimeMillis());
                }
            }
        } else {
            // Keep the status from the old version of this user if it has a
            // higher ID, regardless of the fact we were given a specific
            // status to use.
            if (oldUser != null && oldUser.getStatus() != null && oldUser.getStatus().getID() > status.getID()) {
                this.lastStatus = oldUser.getStatus();
            } else {
                this.lastStatus = status;
            }
        }
    }


    /**
     * Get the screen name for this user.
     *
     * @return this users screen name.
     */
    public String getScreenName() {
        return screenName;
    }
    
    /**
     * Get the id for this user.
     * 
     * @return this users id.
     */
    public long getID() {
        return userID;
    }

    /**
     * Get the real name for this user.
     * 
     * @return this users real name.
     */
    public String getRealName() {
        return realName;
    }

    /**
     * Are we following this user?
     * 
     * @return True if we are following this user, else false.
     */
    public boolean isFollowing() {
        return following;
    }

    /**
     * Get the last known status of this user
     *
     * @return Last known status.
     */
    public TwitterStatus getStatus() {
        return lastStatus;
    }

    /**
     * Are we being followed by this user?
     *
     * @return True if we are being followed by this user, else false.
     */
    public boolean isFollowingUs() {
        return followingUs;
    }

    /**
     * Change if this user is following us.
     *
     * @param followingUs The new value for this setting.
     */
    public void setFollowingUs(final boolean followingUs) {
        this.followingUs = followingUs;
    }


}
