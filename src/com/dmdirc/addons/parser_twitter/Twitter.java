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
package com.dmdirc.addons.parser_twitter;

import com.dmdirc.addons.parser_twitter.api.TwitterAPI;
import com.dmdirc.addons.parser_twitter.api.TwitterMessage;
import com.dmdirc.addons.parser_twitter.api.TwitterStatus;
import com.dmdirc.addons.parser_twitter.api.TwitterUser;
import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.common.DefaultStringConverter;
import com.dmdirc.parser.common.IgnoreList;
import com.dmdirc.parser.common.MyInfo;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.LocalClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.ChannelMessageListener;
import com.dmdirc.parser.interfaces.StringConverter;
import com.dmdirc.parser.interfaces.callbacks.AuthNoticeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelModeChangeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelNamesListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelSelfJoinListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelTopicListener;
import com.dmdirc.parser.interfaces.callbacks.MotdEndListener;
import com.dmdirc.parser.interfaces.callbacks.MotdLineListener;
import com.dmdirc.parser.interfaces.callbacks.MotdStartListener;
import com.dmdirc.parser.interfaces.callbacks.NetworkDetectedListener;
import com.dmdirc.parser.interfaces.callbacks.NickChangeListener;
import com.dmdirc.parser.interfaces.callbacks.NumericListener;
import com.dmdirc.parser.interfaces.callbacks.Post005Listener;
import com.dmdirc.parser.interfaces.callbacks.PrivateMessageListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateNoticeListener;
import com.dmdirc.parser.interfaces.callbacks.ServerReadyListener;
import com.dmdirc.parser.interfaces.callbacks.UserModeDiscoveryListener;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.util.IrcAddress;
import java.util.ArrayList;
import java.util.Collection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Twitter Parser for DMDirc.
 *
 * @author shane
 */
public class Twitter implements Parser {
    /** Are we connected? */
    private boolean connected = false;

    /** Twitter API. */
    private TwitterAPI api = new TwitterAPI("");

    /** Channels we are in. */
    private final Map<String, TwitterChannelInfo> channels = new HashMap<String, TwitterChannelInfo>();

    /** Clients we know. */
    private final Map<String, TwitterClientInfo> clients = new HashMap<String, TwitterClientInfo>();

    /**
     * How many API calls should we make in an hour?
     *
     * Default 60
     */
    private long apiLimit = 60;

// TODO: Not yet supported by library.
//    /** How many statuses to request at a time? (Max 200, Default 20)*/
//    private long statusRequests = 20;

    /** When did we last query the API? */
    private long lastQueryTime = 0;

    /** Username for twitter. */
    private String myUsername;

    /** Password for twitter. */
    private String myPassword;

    /** Callback Manager for Twitter. */
    private CallbackManager<Twitter> myCallbackManager = new TwitterCallbackManager(this);

    /** String Convertor. */
    private DefaultStringConverter myStringConverter = new DefaultStringConverter();

    /** Ignore list (unused). */
    private IgnoreList myIgnoreList = new IgnoreList();

    /** Myself. */
    private TwitterClientInfo myself = null;

    /** List of currently active twitter parsers. */
    protected static List<Twitter> currentParsers = new ArrayList<Twitter>();

    /**
     * Create a new Twitter Parser!
     *
     * @param myInfo The client information to use
     * @param address The address of the server to connect to
     */
    protected Twitter(final MyInfo myInfo, final IrcAddress address) {
        // TODO: irc address should allow usernames shortly.
        final String[] bits = address.getPassword().split(":");
        this.myUsername = bits[0];
        this.myPassword = (bits.length > 1) ? bits[1] : "";
    }

    /** {@inheritDoc} */
		@Override
		public void disconnect(final String message) {
        connected = false;
        currentParsers.remove(this);
        api = new TwitterAPI("");
		}

    /** {@inheritDoc} */
		@Override
		public void joinChannel(final String channel) {
        joinChannel(channel, "");
		}

    /** {@inheritDoc} */
		@Override
		public void joinChannel(final String channel, final String key) {
        // getCallbackManager().getCallbackType(NumericListener.class).call(474, new String[]{":/me builds twitter pwitter.com", "474", myself.getNickname(), channel, "Cannot join channel, not yet implemented."});
        if (isValidChannelName(channel) && getChannel(channel) == null && !channel.equalsIgnoreCase("&twitter")) {
            final TwitterChannelInfo newChannel = new TwitterChannelInfo(channel, this);
            newChannel.addChannelClient(new TwitterChannelClientInfo(newChannel, myself));
            if (channel.matches("^&[0-9]+$")) {
                try {
                    long id = Long.parseLong(channel.substring(1));
                    final TwitterStatus status = api.getStatus(id);
                    if (status != null) {
                        if (status.getReplyTo() > 0) {
                            newChannel.setLocalTopic(status.getText()+" [Reply to: &"+status.getReplyTo()+"]");
                        } else {
                            newChannel.setLocalTopic(status.getText());
                        }
                        newChannel.setTopicSetter(status.getUser().getScreenName());
                        newChannel.setTopicTime(status.getTime());
                        final TwitterClientInfo client = (TwitterClientInfo) getClient(status.getUser().getScreenName());
                        if (client.isFake()) {
                            client.setFake(false);
                            clients.put(client.getNickname().toLowerCase(), client);
                        }
                        newChannel.addChannelClient(new TwitterChannelClientInfo(newChannel, client));
                    } else {
                        newChannel.setLocalTopic("Unknown status, or you do not have access to see it.");
                    }
                } catch (NumberFormatException nfe) { }
            }
            doJoinChannel(newChannel);
        } else {
            getCallbackManager().getCallbackType(NumericListener.class).call(474, new String[]{":twitter.com", "474", myself.getNickname(), channel, "Cannot join channel - name is not valid, or you are already there."});
        }
		}

    /** {@inheritDoc} */
		@Override
		public ChannelInfo getChannel(final String channel) {
				return channels.containsKey(channel.toLowerCase()) ? channels.get(channel.toLowerCase()) : null;
		}

    /** {@inheritDoc} */
		@Override
		public Collection<? extends ChannelInfo> getChannels() {
				return new ArrayList<TwitterChannelInfo>(channels.values());
		}

    /** {@inheritDoc} */
		@Override
		public void setBindIP(final String ip) {
				return;
		}

    /** {@inheritDoc} */
		@Override
		public int getMaxLength(final String type, final String target) {
				return 140;
		}

    /** {@inheritDoc} */
		@Override
		public LocalClientInfo getLocalClient() {
				return myself;
		}

    /** {@inheritDoc} */
		@Override
		public ClientInfo getClient(final String details) {
        final String client = TwitterClientInfo.parseHost(details);
				return clients.containsKey(client.toLowerCase()) ? clients.get(client.toLowerCase()) : new TwitterClientInfo(details, this).setFake(true);
		}

    /**
    * Tokenise a line.
    * splits by " " up to the first " :" everything after this is a single token
    *
    * @param line Line to tokenise
    * @return Array of tokens
    */
    public static String[] tokeniseLine(final String line) {
        if (line == null) {
            return new String[]{"", }; // Return empty string[]
        }

        final int lastarg = line.indexOf(" :");
        String[] tokens;

        if (lastarg > -1) {
            final String[] temp = line.substring(0, lastarg).split(" ");
            tokens = new String[temp.length + 1];
            System.arraycopy(temp, 0, tokens, 0, temp.length);
            tokens[temp.length] = line.substring(lastarg + 2);
        } else {
            tokens = line.split(" ");
        }

        return tokens;
    }

    /** {@inheritDoc} */
		@Override
		public void sendRawMessage(final String message) {
        // TODO: Parse some lines in order to fake IRC.
        final String[] bits = tokeniseLine(message);

        if (bits[0].equalsIgnoreCase("JOIN") && bits.length > 1) {
            joinChannel(bits[1]);
        } else {
            getCallbackManager().getCallbackType(NumericListener.class).call(474, new String[]{":twitter.com", "421", myself.getNickname(), bits[0], "Unknown Command - "+message});
        }
		}

    /** {@inheritDoc} */
		@Override
		public boolean isValidChannelName(final String name) {
				return (name.matches("^&[0-9]+$") || name.equalsIgnoreCase("&twitter") || name.startsWith("#"));
		}

    /** {@inheritDoc} */
		@Override
		public String getServerName() {
				return "twitter.com";
		}

    /** {@inheritDoc} */
		@Override
		public String getNetworkName() {
				return "twitter-"+myself.getNickname();
		}

    /** {@inheritDoc} */
		@Override
		public String getServerSoftware() {
				return "twitter";
		}

    /** {@inheritDoc} */
		@Override
		public String getServerSoftwareType() {
				return "twitter";
		}

    /** {@inheritDoc} */
		@Override
		public int getMaxTopicLength() {
				return 140;
		}

    /** {@inheritDoc} */
		@Override
		public String getBooleanChannelModes() {
				return "";
		}

    /** {@inheritDoc} */
		@Override
		public String getListChannelModes() {
				return "b";
		}

    /** {@inheritDoc} */
		@Override
		public int getMaxListModes(final char mode) {
				return 0;
		}

    /** {@inheritDoc} */
		@Override
		public boolean isUserSettable(final char mode) {
				switch (mode) {
            case 'b':
                return true;
            default:
                return false;
        }
		}

    /** {@inheritDoc} */
		@Override
		public String getParameterChannelModes() {
				return "";
		}

    /** {@inheritDoc} */
		@Override
		public String getDoubleParameterChannelModes() {
				return "b";
		}

    /** {@inheritDoc} */
		@Override
		public String getUserModes() {
				return "";
		}

    /** {@inheritDoc} */
		@Override
		public String getChannelUserModes() {
				return "ov";
		}

    /** {@inheritDoc} */
		@Override
		public CallbackManager<? extends Parser> getCallbackManager() {
				return myCallbackManager;
		}

    /** {@inheritDoc} */
		@Override
		public long getServerLatency() {
				return 0;
		}

    /** {@inheritDoc} */
		@Override
		public void sendCTCP(final String target, final String type, final String message) {
				getCallbackManager().getCallbackType(PrivateNoticeListener.class).call("This parser does not support CTCPs.", "twitter.com");
		}

    /** {@inheritDoc} */
		@Override
		public void sendCTCPReply(final String target, final String type, final String message) {
				getCallbackManager().getCallbackType(PrivateNoticeListener.class).call("This parser does not support CTCP replies.", "twitter.com");
		}

    /** {@inheritDoc} */
		@Override
		public void sendMessage(final String target, final String message) {
        final TwitterChannelInfo channel = (TwitterChannelInfo) this.getChannel(target);
        if (target.equalsIgnoreCase("&twitter")) {
            if (!api.isAllowed()) {
                final String[] bits = message.split(" ");
                api.setAccessPin(bits[0]);
                if (api.isAllowed(true)) {
                    getCallbackManager().getCallbackType(ChannelMessageListener.class).call(channel, null, "Thank you for authorising DMDirc.", "twitter.com");
                    updateTwitterChannel();
                } else {
                    getCallbackManager().getCallbackType(ChannelMessageListener.class).call(channel, null, "Authorising DMDirc failed, please try again: "+api.getOAuthURL(), "twitter.com");
                }
            } else {
                if (setStatus(message)) {
                    getCallbackManager().getCallbackType(PrivateNoticeListener.class).call("Setting status ok.", "twitter.com");
                } else {
                    getCallbackManager().getCallbackType(PrivateNoticeListener.class).call("Setting status failed.", "twitter.com");
                }
            }
        } else if (target.matches("^&[0-9]+$")) {
            try {
                long id = Long.parseLong(target.substring(1));
                if (setStatus(message, id)) {
                    getCallbackManager().getCallbackType(PrivateNoticeListener.class).call("Setting status ok.", "twitter.com");
                } else {
                    getCallbackManager().getCallbackType(PrivateNoticeListener.class).call("Setting status failed.", "twitter.com");
                }
            } catch (NumberFormatException nfe) { }
        } else if (!target.matches("^#.+$")) {
            api.newDirectMessage(target, message);
        } else {
            getCallbackManager().getCallbackType(PrivateNoticeListener.class).call("Messages to '"+target+"' are not currently supported.", "twitter.com");
        }
		}

    /** {@inheritDoc} */
		@Override
		public void sendNotice(final String target, final String message) {
				getCallbackManager().getCallbackType(PrivateNoticeListener.class).call("This parser does not support notices.", "twitter.com");
		}

    /** {@inheritDoc} */
		@Override
		public void sendAction(final String target, final String message) {
        getCallbackManager().getCallbackType(PrivateNoticeListener.class).call("This parser does not support CTCPs.", "twitter.com");
		}

    /** {@inheritDoc} */
		@Override
		public String getLastLine() {
        return "";
		}

    /** {@inheritDoc} */
		@Override
		public String[] parseHostmask(final String hostmask) {
        return TwitterClientInfo.parseHostFull(hostmask);
    }

    /** {@inheritDoc} */
		@Override
		public int getLocalPort() {
				return api.getPort();
		}

    /** {@inheritDoc} */
		@Override
		public long getPingTime() {
				return System.currentTimeMillis() - lastQueryTime;
		}

    /** {@inheritDoc} */
		@Override
		public void setPingTimerInterval(final long newValue) { /* Do Nothing. */ }

    /** {@inheritDoc} */
		@Override
		public long getPingTimerInterval() {
				return -1;
		}

    /** {@inheritDoc} */
		@Override
		public void setPingTimerFraction(final int newValue) { /* Do Nothing. */ }

    /** {@inheritDoc} */
		@Override
		public int getPingTimerFraction() {
        return -1;
		}

    /**
     * Run the twitter parser.
     */
    @Override
		public void run() {
        resetState();
        api = new TwitterAPI(myUsername);
        currentParsers.add(this);
        connected = true;
        
        final TwitterChannelInfo channel = new TwitterChannelInfo("&twitter", this);
        channels.put("&twitter", channel);
        channel.addChannelClient(new TwitterChannelClientInfo(channel, myself));

        // Fake 001
        getCallbackManager().getCallbackType(ServerReadyListener.class).call();
        // Fake 005
        getCallbackManager().getCallbackType(NetworkDetectedListener.class).call(this.getNetworkName(), this.getServerSoftware(), this.getServerSoftwareType());
        getCallbackManager().getCallbackType(Post005Listener.class).call();
        // Fake MOTD
        getCallbackManager().getCallbackType(AuthNoticeListener.class).call("Welcome to twitter.");
        getCallbackManager().getCallbackType(MotdStartListener.class).call("- twitter.com Message of the Day -");
        getCallbackManager().getCallbackType(MotdLineListener.class).call("- This is an experimental parser, to allow DMDirc to use twitter");
        getCallbackManager().getCallbackType(MotdLineListener.class).call("- ");
        getCallbackManager().getCallbackType(MotdLineListener.class).call("- Your timeline appears in &twitter (topic is your last status)");
        getCallbackManager().getCallbackType(MotdLineListener.class).call("- All messages sent to this channel (or topics set) will cause the status to be set.");
        getCallbackManager().getCallbackType(MotdLineListener.class).call("- ");
        getCallbackManager().getCallbackType(MotdLineListener.class).call("- Messages can be replied to using /msg &<messageid> <reply>");
        getCallbackManager().getCallbackType(MotdLineListener.class).call("- ");
        getCallbackManager().getCallbackType(MotdEndListener.class).call(false, "End of /MOTD command");
        // Fake some more on-connect crap
        getCallbackManager().getCallbackType(UserModeDiscoveryListener.class).call(myself, "");

        channel.setLocalTopic("No status known.");
        this.doJoinChannel(channel);

        if (!api.isAllowed()) {
            final String hostname = "twitter.com";
            final List<String> welcomeMessage = new ArrayList<String>();
            
            welcomeMessage.add("DMDirc has not been authorised to use the account "+api.getUsername());
            welcomeMessage.add("");
            welcomeMessage.add("Before you can use DMDirc with twitter you need to authorise it.");
            welcomeMessage.add("");
            welcomeMessage.add("To do this, please visit: "+api.getOAuthURL());
            welcomeMessage.add("and then type the pin number here.");

            for (String line : welcomeMessage) {
                getCallbackManager().getCallbackType(ChannelMessageListener.class).call(channel, null, line, hostname);
            }
        } else {
            getCallbackManager().getCallbackType(ChannelMessageListener.class).call(channel, null, "DMDirc has been authorised to use the account "+api.getUsername(), "twitter.com");
            updateTwitterChannel();
        }

        long lastReplyId = -1;
        long lastTimelineId = -1;
        long lastDirectMessageId = -1;
        int count = 0;
        final long pruneCount = 20; // Every 20 loops, clear the status cache of
        final long pruneTime = 3600 * 1000 ; // anything older than 1 hour.
				while (connected) {
            final int startCalls = api.getUsedCalls();
            if (api.isAllowed()) {
                lastQueryTime = System.currentTimeMillis();

                final List<TwitterStatus> statuses = new ArrayList<TwitterStatus>();
                for (TwitterStatus status : api.getReplies(lastReplyId)) {
                    statuses.add(status);
                    if (status.getID() > lastReplyId) { lastReplyId = status.getID(); }
                }

                for (TwitterStatus status : api.getFriendsTimeline(lastTimelineId)) {
                    if (!statuses.contains(status)) {
                        statuses.add(status);
                    }
                    if (status.getID() > lastTimelineId) { lastTimelineId = status.getID(); }
                }

                Collections.sort(statuses);

                for (TwitterStatus status : statuses) {
                    final ChannelClientInfo cci = channel.getChannelClient(status.getUser().getScreenName());
                    final String message;
                    if (status.getReplyTo() > 0) {
                        message = String.format("%s %c15&%d in reply to &%d", status.getText(), Styliser.CODE_COLOUR, status.getID(), status.getReplyTo());
                    } else {
                        message = String.format("%s %c15&%d", status.getText(), Styliser.CODE_COLOUR, status.getID());
                    }
                    final String hostname = status.getUser().getScreenName();
                    System.out.println("<"+hostname+"> "+message);
                    getCallbackManager().getCallbackType(ChannelMessageListener.class).call(channel, cci, message, hostname);
                }

                for (TwitterMessage directMessage : api.getDirectMessages(lastDirectMessageId)) {
                    final String message = directMessage.getText();
                    final String hostname = directMessage.getSenderScreenName();

                    getCallbackManager().getCallbackType(PrivateMessageListener.class).call(message, hostname);
                    lastDirectMessageId = directMessage.getID();
                }

                checkTopic(channel);
            }

            final Long[] apiCalls = api.getRemainingApiCalls();
            final Long timeLeft = apiCalls[2] - System.currentTimeMillis();
            final long sleepTime;
            if (!api.isAllowed()) {
                // If we aren't authenticated, then sleep for 1 minute.
                sleepTime = 60 * 1000;
            } else if (apiCalls[3] > apiLimit) {
                // Sleep for the rest of the hour, we have done too much!
                sleepTime = timeLeft;
            } else {
                // Else divide the time left by the number of calls we make each
                // time.
                sleepTime = timeLeft / (api.getUsedCalls() - startCalls);
            }

            try { Thread.sleep(sleepTime); } catch (InterruptedException ex) { }
            
            if (++count > pruneCount) {
                api.pruneStatusCache(System.currentTimeMillis() - pruneTime);
            }
        }
		}

    /**
     * Reset the state of the parser.
     */
    private void resetState() {
        connected = false;
        channels.clear();
        clients.clear();
        
        myself = new TwitterClientInfo(myUsername, this);
    }

    /**
     * Get the Twitter API Object
     * 
     * @return The Twitter API Object
     */
    public TwitterAPI getApi() {
        return api;
    }

    /** {@inheritDoc} */
    @Override
    public StringConverter getStringConverter() {
        return myStringConverter;
    }

    /** {@inheritDoc} */
    @Override
    public void setIgnoreList(final IgnoreList ignoreList) {
        myIgnoreList = ignoreList;
    }

    /** {@inheritDoc} */
    @Override
    public IgnoreList getIgnoreList() {
        return myIgnoreList;
    }

    /**
     * Set the twitter status.
     * 
     * @param message Status to use.
     * @return True if status was updated, else false.
     */
    public boolean setStatus(final String message) {
        return setStatus(message, -1);
    }

    /**
     * Set the twitter status.
     *
     * @param message Status to use.
     * @param id
     * @return True if status was updated, else false.
     */
    private boolean setStatus(final String message, final long id) {
        if (api.setStatus(message, id)) {
            final TwitterChannelInfo channel = (TwitterChannelInfo) this.getChannel("&twitter");
            if (channel != null) {
                checkTopic(channel);
            }
            return true;
        }

        return false;
    }

    /**
     * Rename the given client from the given name.
     *
     * @param client Client to rename
     * @param old Old nickname
     */
    void renameClient(final TwitterClientInfo client, final String old) {
        clients.remove(old.toLowerCase());
        clients.put(client.getNickname().toLowerCase(), client);
        
        getCallbackManager().getCallbackType(NickChangeListener.class).call(client, old);
    }

    @Override
    public int getMaxLength() {
        return 140;
    }

    /**
     * Make the core think a channel was joined.
     *
     * @param channel Channel to join.
     */
    private void doJoinChannel(final TwitterChannelInfo channel) {
        // Fake Join Channel
        getCallbackManager().getCallbackType(ChannelSelfJoinListener.class).call(channel);
        getCallbackManager().getCallbackType(ChannelTopicListener.class).call(channel, true);
        getCallbackManager().getCallbackType(ChannelNamesListener.class).call(channel);
        getCallbackManager().getCallbackType(ChannelModeChangeListener.class).call(channel, null, "", "");
    }

    /**
     * Update the users and topic of the main channel.
     */
    private void updateTwitterChannel() {
        final TwitterChannelInfo channel = (TwitterChannelInfo) getChannel("&twitter");
        final TwitterStatus myStatus = ((TwitterClientInfo)getLocalClient()).getUser().getStatus();
        checkTopic(channel);

        channel.clearChannelClients();
        channel.addChannelClient(new TwitterChannelClientInfo(channel, myself));

        for (TwitterUser user : api.getFriends()) {
            final TwitterClientInfo ci = new TwitterClientInfo(user.getScreenName(), this);
            clients.put(ci.getNickname().toLowerCase(), ci);
            final TwitterChannelClientInfo cci = new TwitterChannelClientInfo(channel, ci);

            channel.addChannelClient(cci);
        }
        getCallbackManager().getCallbackType(ChannelNamesListener.class).call(channel);
    }

    /**
     * Check if the topic in the given channel has been changed, and if it has
     * fire the callback.
     * 
     * @param channel channel to check.
     */
    private void checkTopic(final TwitterChannelInfo channel) {
        final String oldStatus = channel.getTopic();
        if (myself.getUser().getStatus() == null) { return; }
        final String newStatus = myself.getUser().getStatus().getText();

        System.out.println("Old Status: "+oldStatus);
        System.out.println("New Status: "+newStatus);

        if (!newStatus.equalsIgnoreCase(oldStatus)) {
            channel.setTopicSetter(myUsername);
            channel.setTopicTime(System.currentTimeMillis());
            channel.setLocalTopic(newStatus);
            getCallbackManager().getCallbackType(ChannelTopicListener.class).call(channel, false);
        }
    }
}