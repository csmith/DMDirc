/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.addons.parser_twitter.api;

import com.dmdirc.config.IdentityManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
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

    /** API Allowed status */
    private APIAllowed allowed = APIAllowed.UNKNOWN;

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
    private String getToken() {
        final String domain = "plugin-Twitter";
        if (IdentityManager.getGlobalConfig().hasOptionString(domain, "token-"+myUsername)) {
            return IdentityManager.getGlobalConfig().getOption(domain, "token-"+myUsername);
        } else {
            return "";
        }
    }
    
    /**
     * Gets the twitter access token secret if known.
     * 
     * @return Access Token Secret
     */
    private String getTokenSecret() {
        final String domain = "plugin-Twitter";
        if (IdentityManager.getGlobalConfig().hasOptionString(domain, "tokenSecret-"+myUsername)) {
            return IdentityManager.getGlobalConfig().getOption(domain, "tokenSecret-"+myUsername);
        } else {
            return "";
        }
    }

    /**
     * Attempt to sign the given connection.
     *
     * @param connection Connection to sign.
     */
    private void signURL(final HttpURLConnection connection) {
        if (!hasSigned) {
            if (getToken().isEmpty() || getTokenSecret().isEmpty()) {
                throw new TwitterException("Unable to sign URLs, no tokens known.");
            }
            consumer.setTokenWithSecret(getToken(), getTokenSecret());
            hasSigned = true;
        }
        try {
            consumer.sign(connection);
        } catch (OAuthMessageSignerException ex) {

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
            System.out.println("getXML: " + address);
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
     * Get the XML for the given UNSIGNED HttpURLConnection object.
     *
     * @param request HttpURLConnection to get XML for.
     * @return Document object for this xml.
     */
    private Document getXML(final HttpURLConnection request) {
        try {
            signURL(request);
            request.connect();

            final BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
            final StringBuilder xml = new StringBuilder();
            String line;

            do {
                line = in.readLine();
                if (line != null) { xml.append(line); }
            } while (line != null);
            in.close();

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
        if (!username.equalsIgnoreCase(user.getScreenName())) {
            userCache.remove(username.toLowerCase());
        }

        userCache.put(user.getScreenName().toLowerCase(), user);

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
        if (userCache.containsKey(username.toLowerCase())) {
            return userCache.get(username.toLowerCase());
        } else {
            return null;
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

            userCache.put(username.toLowerCase(), user);
        }

        return user;
    }

    /**
     * End the twitter session.
     */
    public void endSession() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Send a direct message to the given user
     *
     * @param target Target user.
     * @param message Message to send.
     */
    public void newDirectMessage(final String target, final String message) {
        throw new UnsupportedOperationException("Not yet implemented");
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
     * Get the messages sent for us that are later than the given ID.
     *
     * @param lastReplyId Last reply we know of.
     * @return The messages sent for us that are later than the given ID.
     */
    public List<TwitterStatus> getReplies(final long lastReplyId) {
        final List<TwitterStatus> result = new ArrayList<TwitterStatus>();

        final Document doc = getXML("http://twitter.com/statuses/mentions.xml?since_id="+lastReplyId+"&count=20");
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
        final List<TwitterStatus> result = new ArrayList<TwitterStatus>();

        final Document doc = getXML("http://twitter.com/statuses/friends_timeline.xml?since_id="+lastTimelineId+"&count=20");
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
        final List<TwitterMessage> result = new ArrayList<TwitterMessage>();

        final Document doc = getXML("http://twitter.com/direct_messages.xml?since_id="+lastDirectMessageId+"&count=20");
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
            final StringBuilder address = new StringBuilder("http://twitter.com/statuses/update.xml?status=");
            address.append(URLEncoder.encode(status, "utf-8"));
            if (id >= 0) {
                address.append("&in_reply_to_status_id="+Long.toString(id));
            }

            final URL url = new URL(address.toString());
            final HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.setRequestMethod("POST");
            final Document doc = getXML(request);
            if (request.getResponseCode() == 200) {
                new TwitterStatus(this, doc.getDocumentElement());
                return true;
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
     * @return Number of api calls remaining.
     */
    public long getRemainingApiCalls() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Remove the friendship between you and the given user
     *
     * @param user To remove friendship from.
     */
    public void destroyFriendship(final String user) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Get the URL the user must visit in order to authorize DMDirc.
     * 
     * @return the URL the user must visit in order to authorize DMDirc.
     * @throws TwitterException  if there is a problem with OAuth.*
     */
    public String getOAuthURL() throws TwitterException {
        try {
            return provider.retrieveRequestToken(OAuth.OUT_OF_BAND);
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
     * Get the URL the user must visit in order to authorize DMDirc.
     *
     * @param pin Pin for OAuth
     * @throws TwitterException  if there is a problem with OAuth.
     */
    public void setAccessPin(final String pin) throws TwitterException {
        final String domain = "plugin-Twitter";
        try {
            provider.retrieveAccessToken(pin);
            IdentityManager.getConfigIdentity().setOption(domain, "token-"+myUsername, consumer.getToken());
            IdentityManager.getConfigIdentity().setOption(domain, "tokenSecret-"+myUsername, consumer.getTokenSecret());
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
        if (getToken().isEmpty() || getTokenSecret().isEmpty()) {
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
                }
            } catch (IOException ex) {
                allowed = allowed.FALSE;
            }
        }

        return allowed.getBooleanValue();
    }

}
