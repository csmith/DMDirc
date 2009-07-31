/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.addons.parser_twitter.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.SignatureMethod;

import java.io.ByteArrayInputStream;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Implementation of the twitter API for DMDirc.
 * 
 * @author shane
 */
public class TwitterAPI {
    /** OAuth Consumer */
    private OAuthConsumer consumer = new DefaultOAuthConsumer("qftK3mAbLfbWWHf8shiyjw", "flPr2TJGp4795DeTu4VkUlNLX8g25SpXWXZ7SKW0Bg", SignatureMethod.HMAC_SHA1);

    /** OAuth Provider */
    private OAuthProvider provider = new DefaultOAuthProvider(consumer, "http://twitter.com/oauth/request_token", "http://twitter.com/oauth/access_token", "http://twitter.com/oauth/authorize");

    /** Have we signed anything yet? */
    private boolean hasSigned = false;

    /** Username for this twitter API. */
    private final String myUsername;

    /** Cache of users. */
    final static Map<String, TwitterUser> userCache = new HashMap<String, TwitterUser>();

    /** Cache of user IDs to screen names. */
    final static Map<Long, String> userIDMap = new HashMap<Long, String>();

    /** Cache of statuses. */
    final static Map<Long, TwitterStatus> statusCache = new HashMap<Long, TwitterStatus>();

    /** API Allowed status */
    private APIAllowed allowed = APIAllowed.UNKNOWN;
    
    /** How many API calls have we made since the last reset? */
    private int usedCalls = 0;
    
    /** API reset time. */
    private long resetTime = 0;

    /** Twitter Token */
    private String token = "";

    /** Twitter Token Secret */
    private String tokenSecret = "";

    /**
     * Create a new Twitter API for the given user.
     *
     * @param myUsername
     */
    public TwitterAPI(final String myUsername) {
        this.myUsername = myUsername;

        // if we are allowed, isAllowed will automatically call getUser() to
        // update the cache with our own user object.
        if (!isAllowed()) {
            // If not, add a temporary one.
            // It will be replaced as soon as the allowed status is changed to
            // true by isAlowed().
            updateUser(new TwitterUser(this, myUsername));
        }
    }

    /**
     * Gets the twitter access token if known.
     *
     * @return Access Token
     */
    public String getToken() {
        return token;
    }

    /**
     * Gets the twitter access token secret if known.
     *
     * @return Access Token Secret
     */
    public String getTokenSecret() {
        return tokenSecret;
    }

    /**
     * Set the twitter access token.
     *
     * @param token new Access Token
     */
    public void setToken(final String token) {
        this.token = token;
    }

    /**
     * Set the twitter access token secret.
     *
     * @param tokenSecret new Access Token Secret
     */
    public void setTokenSecret(final String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    /**
     * Attempt to sign the given connection.
     *
     * @param connection Connection to sign.
     */
    private void signURL(final HttpURLConnection connection) {
        if (!hasSigned) {
            if (getToken().isEmpty() || getTokenSecret().isEmpty()) {
                return;
            }
            consumer.setTokenWithSecret(getToken(), getTokenSecret());
            hasSigned = true;
        }
        try {
            consumer.sign(connection);
        } catch (OAuthMessageSignerException ex) {
            ex.printStackTrace();
        } catch (OAuthExpectationFailedException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Parse the given string to a long without throwing an exception.
     * If an exception was raised, default will be used.
     *
     * @param string String to parse.
     * @param fallback Default on failure.
     * @return Long from string
     */
    public static Long parseLong(final String string, final long fallback) {
        long result = fallback;
        try {
            result = Long.parseLong(string);
        } catch (NumberFormatException nfe) { }

        return result;
    }

    /**
     * Parse the given string to a boolean, returns true for "true", "yes" or "1"
     *
     * @param string String to parse.
     * @return Boolean from string
     */
    public static boolean parseBoolean(final String string) {
        return string.equalsIgnoreCase("true") || string.equalsIgnoreCase("yes") || string.equalsIgnoreCase("1");
    }

    /**
     * Get the XML for the given address.
     *
     * @param address Address to get XML for.
     * @return Document object for this xml.
     */
    private Document getXML(final String address) {
        try {
            final URL url = new URL(address);
            return getXML((HttpURLConnection) url.openConnection());
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Get the XML for the given address, using a POST request.
     *
     * @param address Address to get XML for.
     * @param params Params to post.
     * @return Document object for this xml.
     */
    private Document postXML(final String address, final String params) {
        try {
            final URL url = new URL(address);
            return postXML((HttpURLConnection) url.openConnection(), params);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Get the XML for the given UNSIGNED HttpURLConnection object, using a
     * POST request.
     *
     * @param request HttpURLConnection to get XML for.
     * @param params Params to post.
     * @return Document object for this xml.
     */
    private Document postXML(final HttpURLConnection request, final String params) {
        try {
            request.setRequestMethod("POST");

            request.setRequestProperty("Content-Type",  "application/x-www-form-urlencoded");

            request.setRequestProperty("Content-Length", "" +  Integer.toString(params.getBytes().length));
            request.setRequestProperty("Content-Language", "en-US");

            request.setUseCaches(false);
            request.setDoInput(true);
            request.setDoOutput(true);
        } catch (ProtocolException ex) {
            ex.printStackTrace();
        }
        return getXML(request, params);
    }


    /**
     * Get the XML for the given UNSIGNED HttpURLConnection object.
     *
     * @param request HttpURLConnection to get XML for.
     * @return Document object for this xml.
     */
    private Document getXML(final HttpURLConnection request) {
        return getXML(request, null);
    }

    /**
     * Get the XML for the given UNSIGNED HttpURLConnection object.
     *
     * @param request HttpURLConnection to get XML for.
     * @param params Any params for the data type if needed, else null.
     * @return Document object for this xml.
     */
    private Document getXML(final HttpURLConnection request, final String params) {
        if (resetTime > 0 && resetTime <= System.currentTimeMillis()) {
            usedCalls = 0;
            resetTime = System.currentTimeMillis() + 3600000;
        }
        usedCalls++;
        
        BufferedReader in = null;
        boolean dumpOutput = false;
        try {
            signURL(request);

            if (params != null) {
                try {
                    final DataOutputStream out = new DataOutputStream(request.getOutputStream());
                    out.writeBytes(params);
                    out.flush();
                    out.close();
                } catch (IOException ex) { ex.printStackTrace(); }
            }
            
            request.connect();
            in = new BufferedReader(new InputStreamReader(request.getInputStream()));
        } catch (IOException ex) {
            ex.printStackTrace();
            dumpOutput = true;
            if (request.getErrorStream() != null) {
                in = new BufferedReader(new InputStreamReader(request.getErrorStream()));
            } else {
                return null;
            }
        }

        final StringBuilder xml = new StringBuilder();
        String line;

        try {
            do {
                line = in.readLine();
                if (line != null) { xml.append(line); }
                if (dumpOutput) { System.out.println(line); }
            } while (line != null);
        } catch (IOException ex) {
        } finally {
            try { in.close(); } catch (IOException ex) { }
        }
        

        try {
            final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            
            return db.parse(new ByteArrayInputStream(xml.toString().getBytes()));
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }



    /**
     * Remove the cache of the user object for the given user.
     *
     * @param user
     */
    protected void uncacheUser(final TwitterUser user) {
        if (user == null) { return; }
        synchronized (userCache) {
            userCache.remove(user.getScreenName().toLowerCase());
            userIDMap.remove(user.getID());
        }
    }

    /**
     * Update the user object for the given user, if the user ins't know already
     * this will add them to the cache.
     *
     * @param user
     */
    protected void updateUser(final TwitterUser user) {
        updateUser(user.getScreenName(), user);
    }

    /**
     * Update the user object for the given user, if the user ins't know already
     * this will add them to the cache.
     *
     * If the username doesn't match the screen name for the user, then the
     * cached object for "username" will be deleted and a new cached object
     * will be added from the users screen name.
     *
     * @param username
     * @param user
     */
    protected void updateUser(final String username, final TwitterUser user) {
        if (user == null) { return; }
        synchronized (userCache) {
            if (!username.equalsIgnoreCase(user.getScreenName())) {
                userCache.remove(username.toLowerCase());
            }

            userCache.put(user.getScreenName().toLowerCase(), user);
            userIDMap.put(user.getID(), user.getScreenName().toLowerCase());
        }

        // TODO: TwitterStatus and TwitterMessage objects should be informed
        // about updates.
    }

    /**
     * Get a user object for the given user.
     *
     * @param username
     * @return User object for the requested user.
     */
    public TwitterUser getUser(final String username) {
        return getUser(username, false);
    }

    /**
     * Get a cached user object for the given user.
     *
     * @param username
     * @return User object for the requested user.
     */
    public TwitterUser getCachedUser(final String username) {
        synchronized (userCache) {
            if (userCache.containsKey(username.toLowerCase())) {
                return userCache.get(username.toLowerCase());
            } else {
                return null;
            }
        }
    }

    /**
     * Get a user object for the given user.
     *
     * @param username
     * @param force Force an update of the cache?
     * @return User object for the requested user.
     */
    public TwitterUser getUser(final String username, final boolean force) {
        TwitterUser user = getCachedUser(username);
        if (user == null || force) {
            if (username.equalsIgnoreCase(myUsername) && !isAllowed()) {
                 user = new TwitterUser(this, myUsername, -1, "", true);
            } else {
                final Document doc = getXML("http://twitter.com/users/show.xml?screen_name="+username);

                if (doc != null) {
                    user = new TwitterUser(this, doc.getDocumentElement());
                } else {
                    user = null;
                }
            }

            updateUser(user);
        }

        return user;
    }

    /**
     * Update the status object for the given status, if the status isn't known
     * already this will add them to the cache.
     *
     * @param status
     */
    protected void updateStatus(final TwitterStatus status) {
        if (status == null) { return; }
        synchronized (statusCache) {
            statusCache.put(status.getID(), status);
        }
    }

    /**
     * Get a status object for the given id.
     *
     * @param id
     * @return Status object for the requested id.
     */
    public TwitterStatus getStatus(final long id) {
        return getStatus(id, false);
    }

    /**
     * Get a cached status object for the given id.
     *
     * @param id
     * @return status object for the requested id.
     */
    public TwitterStatus getCachedStatus(final long id) {
        synchronized (statusCache) {
            if (statusCache.containsKey(id)) {
                return statusCache.get(id);
            } else {
                return null;
            }
        }
    }

    /**
     * Get a status object for the given id.
     *
     * @param id
     * @param force Force an update of the cache?
     * @return status object for the requested id.
     */
    public TwitterStatus getStatus(final long id, final boolean force) {
        TwitterStatus status = getCachedStatus(id);
        if (status == null || force) {
            final Document doc = getXML("http://twitter.com/statuses/show/"+id+".xml");

            if (doc != null) {
                status = new TwitterStatus(this, doc.getDocumentElement());
            } else {
                status = null;
            }

            updateStatus(status);
        }

        return status;
    }

    /**
     * Prune the status cache of statuses older than the given time.
     * This should be done periodically depending on how many statuses you see.
     *
     * @param time
     */
    public void pruneStatusCache(final long time) {
        synchronized (statusCache) {
            final Map<Long, TwitterStatus> current = new HashMap<Long, TwitterStatus>(statusCache);

            for (Long item : current.keySet()) {
                if (current.get(item).getTime() < time) {
                    statusCache.remove(item);
                }
            }
        }
    }

    /**
     * Send a direct message to the given user
     *
     * @param target Target user.
     * @param message Message to send.
     */
    public void newDirectMessage(final String target, final String message) {
        try {
            postXML("http://twitter.com/direct_messages/new.xml", "screen_name=" + target + "&text=" + URLEncoder.encode(message, "utf-8"));
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * What port are we using?
     *
     * @return port for api connections.
     */
    public int getPort() {
        return 80;
    }

    /**
     * Get a List of TwitterStatus Objects for this user.
     *
     * @param lastUserTimelineId
     * @return a List of TwitterStatus Objects for this user.
     */
    public List<TwitterStatus> getUserTimeline(final long lastUserTimelineId) {
        final List<TwitterStatus> result = new ArrayList<TwitterStatus>();

        final Document doc = getXML("http://twitter.com/statuses/user_timeline.xml?since_id="+lastUserTimelineId+"&count=20");
        if (doc != null) {
            final NodeList nodes = doc.getElementsByTagName("status");
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(new TwitterStatus(this, nodes.item(i)));
            }
        }

        return result;
    }

    /**
     * Get a list of TwitterUsers who we are following.
     *
     * @return A list of TwitterUsers who we are following.
     */
    public List<TwitterUser> getFriends() {
        final List<TwitterUser> result = new ArrayList<TwitterUser>();

        // TODO: support more than 100 friends.
        final Document doc = getXML("http://twitter.com/statuses/friends.xml");
        if (doc != null) {
            final NodeList nodes = doc.getElementsByTagName("user");
            for (int i = 0; i < nodes.getLength(); i++) {
                final TwitterUser user = new TwitterUser(this, nodes.item(i));
                updateUser(user);
                result.add(user);
            }
        }

        return result;
    }

    /**
     * Get a list of IDs of people who are following us.
     *
     * @return A list of IDs of people who are following us.
     */
    public List<Long> getFollowers() {
        final List<Long> result = new ArrayList<Long>();

        // TODO: support more than 100 friends.
        final Document doc = getXML("http://twitter.com/followers/ids.xml");
        if (doc != null) {
            final NodeList nodes = doc.getElementsByTagName("id");
            for (int i = 0; i < nodes.getLength(); i++) {
                final Element element = (Element)nodes.item(i);
                final Long id = parseLong(element.getTextContent(), -1);
                result.add(id);

                if (userIDMap.containsKey(id)) {
                    final TwitterUser user = getCachedUser(userIDMap.get(id));
                    user.setFollowingUs(true);
                }
            }
        }

        return result;
    }

    /**
     * Get the messages sent for us that are later than the given ID.
     *
     * @param lastReplyId Last reply we know of.
     * @return The messages sent for us that are later than the given ID.
     */
    public List<TwitterStatus> getReplies(final long lastReplyId) {
        return getReplies(lastReplyId, 20);
    }

    /**
     * Get the messages sent for us that are later than the given ID.
     *
     * @param lastReplyId Last reply we know of.
     * @param count How many replies to get
     * @return The messages sent for us that are later than the given ID.
     */
    public List<TwitterStatus> getReplies(final long lastReplyId, final int count) {
        final List<TwitterStatus> result = new ArrayList<TwitterStatus>();

        final Document doc = getXML("http://twitter.com/statuses/mentions.xml?since_id="+lastReplyId+"&count="+count);
        if (doc != null) {
            final NodeList nodes = doc.getElementsByTagName("status");

            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(new TwitterStatus(this, nodes.item(i)));
            }
        }

        return result;
    }

    /**
     * Get the messages sent by friends that are later than the given ID.
     *
     * @param lastTimelineId Last reply we know of.
     * @return The messages sent by friends that are later than the given ID.
     */
    public List<TwitterStatus> getFriendsTimeline(final long lastTimelineId) {
        return getFriendsTimeline(lastTimelineId, 20);
    }
    
    /**
     * Get the messages sent by friends that are later than the given ID.
     *
     * @param lastTimelineId Last reply we know of.
     * @param count How many statuses to get
     * @return The messages sent by friends that are later than the given ID.
     */
    public List<TwitterStatus> getFriendsTimeline(final long lastTimelineId, final int count) {
        final List<TwitterStatus> result = new ArrayList<TwitterStatus>();

        final Document doc = getXML("http://twitter.com/statuses/friends_timeline.xml?since_id="+lastTimelineId+"&count="+count);
        if (doc != null) {
            final NodeList nodes = doc.getElementsByTagName("status");
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(new TwitterStatus(this, nodes.item(i)));
            }
        }

        return result;
    }

    /**
     * Get the direct messages sent to us that are later than the given ID.
     *
     * @param lastDirectMessageId Last reply we know of.
     * @return The direct messages sent to us that are later than the given ID.
     */
    public List<TwitterMessage> getDirectMessages(final long lastDirectMessageId) {
        return getDirectMessages(lastDirectMessageId, 20);
    }

    /**
     * Get the direct messages sent to us that are later than the given ID.
     *
     * @param lastDirectMessageId Last reply we know of.
     * @param count How many messages to request at a time
     * @return The direct messages sent to us that are later than the given ID.
     */
    public List<TwitterMessage> getDirectMessages(final long lastDirectMessageId, final int count) {
        final List<TwitterMessage> result = new ArrayList<TwitterMessage>();

        final Document doc = getXML("http://twitter.com/direct_messages.xml?since_id="+lastDirectMessageId+"&count="+count);
        if (doc != null) {
            final NodeList nodes = doc.getElementsByTagName("direct_message");
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(new TwitterMessage(this, nodes.item(i)));
            }
        }

        return result;
    }

    /**
     * Set your status to the given TwitterStatus
     *
     * @param status Status to send
     * @param id id to reply to or -1
     * @return True if status was updated ok.
     */
    public boolean setStatus(final String status, final Long id) {
        try {
            final StringBuilder address = new StringBuilder("status=");
            address.append(URLEncoder.encode(status, "utf-8"));
            if (id >= 0) {
                address.append("&in_reply_to_status_id="+Long.toString(id));
            }

            final URL url = new URL("http://twitter.com/statuses/update.xml");
            final HttpURLConnection request = (HttpURLConnection) url.openConnection();
            final Document doc = postXML(request, address.toString());
            if (request.getResponseCode() == 200) {
                if (doc != null) {
                    new TwitterStatus(this, doc.getDocumentElement());
                }
                return true;
            } else {
                System.out.println("Error from twitter: ("+request.getResponseCode()+") "+request.getResponseMessage());
            }
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    /**
     * Get the number of api calls remaining.
     *
     * @return Long[4] containting API calls limit information.
     *          - 0 is remaning calls
     *          - 1 is total calls per hour
     *          - 2 is the time (in milliseconds) untill reset.
     *          - 3 is the estimated number of api calls we have made since
     *            the last reset.
     */
    public Long[] getRemainingApiCalls() {
        final Document doc = getXML("http://twitter.com/account/rate_limit_status.xml");
        // The call we just made doesn't count, so remove it from the count.
        usedCalls--;
        if (doc != null) {
            final Element element = doc.getDocumentElement();

            final long remaining = parseLong(element.getElementsByTagName("remaining-hits").item(0).getTextContent(), -1);
            final long total = parseLong(element.getElementsByTagName("hourly-limit").item(0).getTextContent(), -1);
            resetTime = 1000 * parseLong(element.getElementsByTagName("reset-time-in-seconds").item(0).getTextContent(), -1);

            return new Long[]{remaining, total, resetTime, (long)usedCalls};
        } else {
            return new Long[]{0L, 0L, System.currentTimeMillis(), (long)usedCalls};
        }
    }

    /**
     * How many calls have we used since reset?
     *
     * @return calls used since reset.
     */
    public int getUsedCalls() {
        return usedCalls;
    }

    /**
     * Get the URL the user must visit in order to authorize DMDirc.
     * 
     * @return the URL the user must visit in order to authorize DMDirc.
     * @throws TwitterRuntimeException  if there is a problem with OAuth.*
     */
    public String getOAuthURL() throws TwitterRuntimeException {
        try {
            return provider.retrieveRequestToken(OAuth.OUT_OF_BAND);
        } catch (OAuthMessageSignerException ex) {
            throw new TwitterRuntimeException(ex.getMessage(), ex);
        } catch (OAuthNotAuthorizedException ex) {
            throw new TwitterRuntimeException(ex.getMessage(), ex);
        } catch (OAuthExpectationFailedException ex) {
            throw new TwitterRuntimeException(ex.getMessage(), ex);
        } catch (OAuthCommunicationException ex) {
            throw new TwitterRuntimeException(ex.getMessage(), ex);
        }
    }

    /**
     * Get the URL the user must visit in order to authorize DMDirc.
     *
     * @param pin Pin for OAuth
     * @throws TwitterException  if there is a problem with OAuth.
     */
    public void setAccessPin(final String pin) throws TwitterException {
        try {
            provider.retrieveAccessToken(pin);
            token = consumer.getToken();
            tokenSecret = consumer.getTokenSecret();
        } catch (OAuthMessageSignerException ex) {
            throw new TwitterException(ex.getMessage(), ex);
        } catch (OAuthNotAuthorizedException ex) {
            throw new TwitterException(ex.getMessage(), ex);
        } catch (OAuthExpectationFailedException ex) {
            throw new TwitterException(ex.getMessage(), ex);
        } catch (OAuthCommunicationException ex) {
            throw new TwitterException(ex.getMessage(), ex);
        }
    }

    /**
     * Get the username for this Twitter API
     *
     * @return Username for this twitter API
     */
    public String getUsername() {
        return myUsername;
    }

    /**
     * Have we been authorised to use this account?
     *
     * @return true if we have been authorised, else false.
     */
    public boolean isAllowed() {
        return isAllowed(false);
    }

    /**
     * Have we been authorised to use this account?
     * Forcing a recheck may use up an API call.
     *
     * @param forceRecheck force a recheck to see if we are allowed.
     * @return true if we have been authorised, else false.
     */
    public boolean isAllowed(final boolean forceRecheck) {
        if (myUsername.isEmpty() || getToken().isEmpty() || getTokenSecret().isEmpty()) {
            return false;
        }
        if (allowed == allowed.UNKNOWN || forceRecheck) {
            try {
                final URL url = new URL("http://twitter.com/account/verify_credentials.xml");
                final HttpURLConnection request = (HttpURLConnection) url.openConnection();
                final Document doc = getXML(request);
                allowed = (request.getResponseCode() == 200) ? allowed.TRUE : allowed.FALSE;

                if (doc != null && allowed.getBooleanValue()) {
                    final TwitterUser user = new TwitterUser(this, doc.getDocumentElement());
                    updateUser(user);
                    getRemainingApiCalls();
                }
            } catch (IOException ex) {
                allowed = allowed.FALSE;
            }
        }

        return allowed.getBooleanValue();
    }

    /**
     * Add the user with the given screen name as a friend.
     *
     * @param name name to add
     * @return The user just added
     */
    public TwitterUser addFriend(final String name) {
        try {
            final Document doc = postXML("http://twitter.com/friendships/create.xml", "screen_name=" + URLEncoder.encode(name, "utf-8"));
            if (doc != null) {
                final TwitterUser user = new TwitterUser(this, doc.getDocumentElement());
                updateUser(user);
                return user;
            }
        } catch (UnsupportedEncodingException ex) { }

        return null;
    }

    /**
     * Remove the user with the given screen name as a friend.
     *
     * @param name name to remove
     * @return The user just deleted
     */
    public TwitterUser delFriend(final String name) {
        try {
            final Document doc = postXML("http://twitter.com/friendships/destroy.xml", "screen_name=" + URLEncoder.encode(name, "utf-8"));
            if (doc != null) {
                final TwitterUser user = new TwitterUser(this, doc.getDocumentElement());
                uncacheUser(user);

                return user;
            }
        } catch (UnsupportedEncodingException ex) { }

        return null;
    }

}
