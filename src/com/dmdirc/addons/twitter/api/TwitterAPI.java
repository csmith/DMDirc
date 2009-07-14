/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.addons.twitter.api;

import com.dmdirc.config.IdentityManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
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

    /**
     * Create a new Twitter API for the given user.
     *
     * @param myUsername
     */
    public TwitterAPI(String myUsername) {
        this.myUsername = myUsername;
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
     *
     * @param username
     * @return
     */
    public TwitterUser getUser(final String username) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     *
     */
    public void endSession() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     *
     * @param target
     * @param message
     */
    public void newDirectMessage(final String target, final String message) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * 
     * @return
     */
    public int getPort() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     *
     * @return
     */
    public List<TwitterStatus> getUserTimeline() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     *
     * @return
     */
    public List<TwitterUser> getFriends() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     *
     * @param lastReplyId
     * @return
     */
    public List<TwitterStatus> getReplies(long lastReplyId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     *
     * @param lastTimelineId
     * @return
     */
    public List<TwitterStatus> getFriendsTimeline(long lastTimelineId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     *
     * @param lastDirectMessageId
     * @return
     */
    public List<TwitterMessage> getDirectMessages(long lastDirectMessageId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     *
     * @param status
     * @return
     */
    public boolean setStatus(TwitterStatus status) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     *
     * @param user
     */
    public void destroyFriendship(TwitterUser user) {
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
        try {
            provider.retrieveAccessToken(pin);
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
        if (getToken().isEmpty() || getTokenSecret().isEmpty()) {
            return false;
        } else {
            try {
                final URL url = new URL("http://twitter.com/statuses/mentions.xml");
                final HttpURLConnection request = (HttpURLConnection) url.openConnection();
                signURL(request);
                request.connect();
                return request.getResponseCode() != 200;
            } catch (IOException ex) {
                return false;
            }
        }
    }

}
