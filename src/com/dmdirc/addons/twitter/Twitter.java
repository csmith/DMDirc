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

import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.common.DefaultStringConverter;
import com.dmdirc.parser.common.IgnoreList;
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
import com.dmdirc.parser.interfaces.callbacks.ServerReadyListener;
import com.dmdirc.parser.interfaces.callbacks.UserModeDiscoveryListener;
import java.util.ArrayList;
import java.util.Collection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.unto.twitter.Api;
import net.unto.twitter.TwitterProtos.DirectMessage;
import net.unto.twitter.TwitterProtos.Status;
import net.unto.twitter.TwitterProtos.User;
import net.unto.twitter.methods.UpdateStatusRequest.Builder;

/**
 * Twitter Parser for DMDirc.
 *
 * @author shane
 */
public class Twitter implements Parser {
    /** Are we connected? */
    private boolean connected = false;

    /** Twitter API. */
    private Api api = null;

    /** Channels we are in. */
    private final Map<String, TwitterChannelInfo> channels = new HashMap<String, TwitterChannelInfo>();

    /** Clients we know. */
    private final Map<String, TwitterClientInfo> clients = new HashMap<String, TwitterClientInfo>();

    /**
     * How long between querying the API?
     * Every time we query we use 3 API calls. (Direct Message, friends_timeline,
     * direct_replies)
     * Defaults to every 180 seconds (same as tircd) which results in using 60
     * API Calls an hour (Current limit is 150).
     * TODO: Different time per request type.
     */
    private long pingTimerInterval = 180 * 1000;

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

    /**
     * Create a new Twitter Parser!
     *
     * @param myUsername Username for twitter.
     * @param myPassword Password for twitter.
     */
    public Twitter(final String myUsername, final String myPassword) {
        this.myUsername = myUsername;
        this.myPassword = myPassword;
    }

    /** {@inheritDoc} */
		@Override
		public void disconnect(final String message) {
        connected = false;
        api.endSession().build().post();
		}

    /** {@inheritDoc} */
		@Override
		public void joinChannel(final String channel) {
        joinChannel(channel, "");
		}

    /** {@inheritDoc} */
		@Override
		public void joinChannel(final String channel, final String key) {
        getCallbackManager().getCallbackType(NumericListener.class).call(474, new String[]{":twitter.com", "474", myself.getNickname(), channel, "Cannot join channel, not yet implemented."});
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
				return clients.containsKey(client.toLowerCase()) ? clients.get(client.toLowerCase()) : null;
		}

    /** {@inheritDoc} */
		@Override
		public void sendRawMessage(final String message) {
        // TODO: Parse some lines in order to fake IRC.
        System.out.println("Twitter: "+message);
		}

    /** {@inheritDoc} */
		@Override
		public boolean isValidChannelName(final String name) {
				return (name.matches("^&[0-9]+$") || name.equalsIgnoreCase("&twitter") || name.startsWith("&"));
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
				throw new UnsupportedOperationException("Not supported yet.");
		}

    /** {@inheritDoc} */
		@Override
		public void sendCTCPReply(final String target, final String type, final String message) {
				throw new UnsupportedOperationException("Not supported yet.");
		}

    /** {@inheritDoc} */
		@Override
		public void sendMessage(final String target, final String message) {
        final TwitterChannelInfo channel = (TwitterChannelInfo) this.getChannel(target);
        if (target.equalsIgnoreCase("&twitter")) {
            setStatus(message);
        } else if (target.matches("^&[0-9]+$")) {
            try {
                long id = Long.parseLong(target.substring(1));
                setStatus(message, id);
            } catch (NumberFormatException nfe) { }
        } else if (!target.matches("^#.+$")) {
            api.newDirectMessage(target, message);
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
		}

    /** {@inheritDoc} */
		@Override
		public void sendNotice(final String target, final String message) {
				throw new UnsupportedOperationException("Not supported yet.");
		}

    /** {@inheritDoc} */
		@Override
		public void sendAction(final String target, final String message) {
				throw new UnsupportedOperationException("Not supported yet.");
		}

    /** {@inheritDoc} */
		@Override
		public String getLastLine() {
				throw new UnsupportedOperationException("Not supported yet.");
		}

    /** {@inheritDoc} */
		@Override
		public String[] parseHostmask(final String hostmask) {
        return TwitterClientInfo.parseHostFull(hostmask);
    }

    /** {@inheritDoc} */
		@Override
		public int getLocalPort() {
				return Api.DEFAULT_PORT;
		}

    /** {@inheritDoc} */
		@Override
		public long getPingTime() {
				return System.currentTimeMillis() - lastQueryTime;
		}

    /** {@inheritDoc} */
		@Override
		public void setPingTimerInterval(final long newValue) {
				pingTimerInterval = newValue;
		}

    /** {@inheritDoc} */
		@Override
		public long getPingTimerInterval() {
				return pingTimerInterval;
		}

    /** {@inheritDoc} */
		@Override
		public void setPingTimerFraction(final int newValue) {
				throw new UnsupportedOperationException("Not supported by this parser.");
		}

    /** {@inheritDoc} */
		@Override
		public int getPingTimerFraction() {
				throw new UnsupportedOperationException("Not supported by this parser.");
		}

    /**
     * Run the twitter parser.
     */
    @Override
		public void run() {
        resetState();
        connected = true;

        Status myStatus = null;
        for (Status status : api.userTimeline().build().get()) {
            myStatus = status;
            break;
        }

        // myself = new TwitterClientInfo(myStatus.getUser(), this);
        myself = new TwitterClientInfo(api.showUser().build().get(), this);
        
        final TwitterChannelInfo channel = new TwitterChannelInfo("&twitter", this);
        channels.put("&twitter", channel);
        if (myStatus != null) { channel.setLocalTopic(myStatus.getText()); }
        channel.addChannelClient(new TwitterChannelClientInfo(channel, myself));

        for (User user : api.friends().build().get()) {
            final TwitterClientInfo ci = new TwitterClientInfo(user, this);
            clients.put(ci.getNickname().toLowerCase(), ci);
            final TwitterChannelClientInfo cci = new TwitterChannelClientInfo(channel, ci);

            channel.addChannelClient(cci);
        }

        channel.setLocalTopic("Temp Topic");

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
        getCallbackManager().getCallbackType(MotdEndListener.class).call("End of /MOTD command");
        // Fake some more on-connect crap
        getCallbackManager().getCallbackType(UserModeDiscoveryListener.class).call(myself, "");
        // Fake Join Channel
        getCallbackManager().getCallbackType(ChannelSelfJoinListener.class).call(channel);
        getCallbackManager().getCallbackType(ChannelTopicListener.class).call(channel, true);
        getCallbackManager().getCallbackType(ChannelNamesListener.class).call(channel);
        getCallbackManager().getCallbackType(ChannelModeChangeListener.class).call(channel, null, "", "");

        long lastReplyId = -1;
        long lastTimelineId = -1;
        long lastDirectMessageId = -1;
				while (connected) {
            lastQueryTime = System.currentTimeMillis();
            
            final List<Status> statuses = new ArrayList<Status>();
            for (Status status : api.replies().sinceId(lastReplyId).build().get()) {
                statuses.add(status);
            }

            for (Status status : api.friendsTimeline().sinceId(lastTimelineId).build().get()) {
                if (!statuses.contains(status)) {
                    statuses.add(status);
                }
            }

            for (Status status : statuses) {
                final ChannelClientInfo cci = channel.getChannelClient(status.getUser().getScreenName());
                ((TwitterClientInfo) cci.getClient()).setUser(status.getUser());
                final String message = String.format("(&%d) %s", status.getId(), status.getText());
                final String hostname = status.getUser().getScreenName()+"!user@twitter.com";

                getCallbackManager().getCallbackType(ChannelMessageListener.class).call(channel, cci, message, hostname);
            }

            for (DirectMessage directMessage : api.directMessages().sinceId(lastDirectMessageId).build().get()) {
                final TwitterClientInfo ci = (TwitterClientInfo) getClient(directMessage.getSenderScreenName());
                if (ci != null) {
                    ci.setUser(directMessage.getSender());
                }
                final String message = directMessage.getText();
                final String hostname = directMessage.getSenderScreenName()+"!user@twitter.com";

                getCallbackManager().getCallbackType(PrivateMessageListener.class).call(channel, ci, message, hostname);
            }

            try { Thread.sleep(getPingTimerInterval()); } catch (InterruptedException ex) { }
        }
		}

    /**
     * Reset the state of the parser.
     */
    private void resetState() {
        connected = false;
        api = Api.builder().username(myUsername).password(myPassword).build();
        channels.clear();
        clients.clear();
    }

    /**
     * Get the Twitter API Object
     * 
     * @return The Twitter API Object
     */
    public Api getApi() {
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
     */
    public void setStatus(final String message) {
        setStatus(message, -1);
    }

    /**
     * Set the twitter status.
     *
     * @param message Status to use.
     * @param id
     */
    private void setStatus(final String message, final long id) {
        Builder foo = api.updateStatus(message);
        if (id != -1) {
            foo = foo.inReplyToStatusId(id);
        }
        
        foo.build().post();

        final TwitterChannelInfo channel = (TwitterChannelInfo) this.getChannel("&twitter");
        if (channel != null) {
            channel.setLocalTopic(message);
            channel.setTopicTime(System.currentTimeMillis());
            channel.setTopicSetter(myself.getHostname());
        }
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

}
