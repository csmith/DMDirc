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
import com.dmdirc.parser.common.IgnoreList;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.LocalClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.ChannelMessageListener;
import com.dmdirc.parser.interfaces.StringConverter;
import com.dmdirc.parser.interfaces.callbacks.AuthNoticeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelNamesListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelSelfJoinListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelTopicListener;
import com.dmdirc.parser.interfaces.callbacks.MotdEndListener;
import com.dmdirc.parser.interfaces.callbacks.MotdLineListener;
import com.dmdirc.parser.interfaces.callbacks.MotdStartListener;
import com.dmdirc.parser.interfaces.callbacks.NetworkDetectedListener;
import com.dmdirc.parser.interfaces.callbacks.NumericListener;
import com.dmdirc.parser.interfaces.callbacks.Post005Listener;
import com.dmdirc.parser.interfaces.callbacks.ServerReadyListener;
import java.util.ArrayList;
import java.util.Collection;

import java.util.HashMap;
import java.util.Map;
import net.unto.twitter.Api;
import net.unto.twitter.TwitterProtos.Status;
import net.unto.twitter.TwitterProtos.User;

/**
 * Twitter Parser for DMDirc.
 *
 * @author shane
 */
public class Twitter implements Parser {
    /** Are we connected? */
    private boolean connected = false;

    /** Twitter API */
    private Api api = null;

    /** Channels we are in. */
    final Map<String, TwitterChannelInfo> channels = new HashMap<String, TwitterChannelInfo>();

    /** Clients we know. */
    final Map<String, TwitterClientInfo> clients = new HashMap<String, TwitterClientInfo>();

    /** How long between querying the API? */
    private long pingTimerInterval = 30000;

    /** When did we last query the API? */
    private long lastQueryTime = 0;

    /** Username for twitter. */
    private String myUsername;

    /** Password for twitter. */
    private String myPassword;

    /** Callback Manager for Twitter. */
    private CallbackManager<Twitter> myCallbackManager = new TwitterCallbackManager(this);

    /** String Convertor. */
    private TwitterStringConverter myStringConverter = new TwitterStringConverter();

    /** Ignore list (unused) */
    private IgnoreList myIgnoreList = new IgnoreList();
    
    /** Myself. */
    private TwitterClientInfo myself = null;

    /**
     * Create a new Twitter Parser!
     *
     * @param myUsername Username for twitter.
     * @param myPassword Password for twitter.
     */
    public Twitter(String myUsername, String myPassword) {
        this.myUsername = myUsername;
        this.myPassword = myPassword;
    }

		@Override
		public void disconnect(String message) {
        connected = false;
        api.endSession().build().post();
		}

		@Override
		public void joinChannel(String channel) {
        joinChannel(channel, "");
		}

		@Override
		public void joinChannel(String channel, String key) {
        getCallbackManager().getCallbackType(NumericListener.class).call(474, new String[]{":twitter.com", "474", myself.getNickname(), channel, "Cannot join channel, not yet implemented."});
		}

		@Override
		public ChannelInfo getChannel(String channel) {
				return channels.containsKey(channel.toLowerCase()) ? channels.get(channel.toLowerCase()) : null;
		}

		@Override
		public Collection<? extends ChannelInfo> getChannels() {
				return new ArrayList<TwitterChannelInfo>(channels.values());
		}

		@Override
		public void setBindIP(String ip) {
				return;
		}

		@Override
		public int getMaxLength(String type, String target) {
				return 140;
		}

		@Override
		public LocalClientInfo getLocalClient() {
				return myself;
		}

		@Override
		public ClientInfo getClient(String details) {
        final String client = TwitterClientInfo.parseHost(details);
				return clients.containsKey(client.toLowerCase()) ? clients.get(client.toLowerCase()) : null;
		}

		@Override
		public void sendRawMessage(String message) {
        // TODO: Parse some lines in order to fake IRC.
        System.out.println("Twitter: "+message);
		}

		@Override
		public boolean isValidChannelName(String name) {
				return (name.matches("^&[0-9]+$") || name.equalsIgnoreCase("&twitter") || name.startsWith("&"));
		}

		@Override
		public String getServerName() {
				return "twitter.com";
		}

		@Override
		public String getNetworkName() {
				return "twitter";
		}

		@Override
		public String getServerSoftware() {
				return "twitter";
		}

		@Override
		public String getServerSoftwareType() {
				return "twitter";
		}

		@Override
		public int getMaxTopicLength() {
				return 140;
		}

		@Override
		public String getBooleanChannelModes() {
				return "";
		}

		@Override
		public String getListChannelModes() {
				return "b";
		}

		@Override
		public int getMaxListModes(char mode) {
				return 0;
		}

		@Override
		public boolean isUserSettable(char mode) {
				switch (mode) {
            case 'b':
                return true;
            default:
                return false;
        }
		}

		@Override
		public String getParameterChannelModes() {
				return "";
		}

		@Override
		public String getDoubleParameterChannelModes() {
				return "b";
		}

		@Override
		public String getUserModes() {
				return "";
		}

		@Override
		public String getChannelUserModes() {
				return "ov";
		}

		@Override
		public CallbackManager<? extends Parser> getCallbackManager() {
				return myCallbackManager;
		}

		@Override
		public long getServerLatency() {
				return 0;
		}

		@Override
		public void sendCTCP(String target, String type, String message) {
				throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void sendCTCPReply(String target, String type, String message) {
				throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void sendMessage(String target, String message) {
        final TwitterChannelInfo channel = (TwitterChannelInfo) this.getChannel(target);
        if (target.equalsIgnoreCase("&twitter")) {
            api.updateStatus(message).build().post();
            channel.setLocalTopic(message);
        } else if (target.matches("^&[0-9]+$")) {
            try {
                long id = Long.parseLong(target.substring(1));
                api.updateStatus(message).inReplyToStatusId(id).build().post();
                channel.setLocalTopic(message);
            } catch (NumberFormatException nfe) { }
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
		}

		@Override
		public void sendNotice(String target, String message) {
				throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void sendAction(String target, String message) {
				throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public String getLastLine() {
				throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public String[] parseHostmask(String hostmask) {
        return TwitterClientInfo.parseHostFull(hostmask);
    }

		@Override
		public int getLocalPort() {
				return Api.DEFAULT_PORT;
		}

		@Override
		public long getPingTime() {
				return System.currentTimeMillis() - lastQueryTime;
		}

		@Override
		public void setPingTimerInterval(long newValue) {
				pingTimerInterval = newValue;
		}

		@Override
		public long getPingTimerInterval() {
				return pingTimerInterval;
		}

		@Override
		public void setPingTimerFraction(int newValue) {
				throw new UnsupportedOperationException("Not supported by this parser.");
		}

		@Override
		public int getPingTimerFraction() {
				throw new UnsupportedOperationException("Not supported by this parser.");
		}

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
        // Fake Join Channel
        getCallbackManager().getCallbackType(ChannelSelfJoinListener.class).call(channel);
        getCallbackManager().getCallbackType(ChannelTopicListener.class).call(channel, true);
        getCallbackManager().getCallbackType(ChannelNamesListener.class).call(channel);
        
				while (connected) {
            lastQueryTime = System.currentTimeMillis();
                    
            for (Status status : api.friendsTimeline().build().get()) {
                final ChannelClientInfo cci = channel.getChannelClient(status.getUser().getScreenName());
                final String message = status.getText();

                getCallbackManager().getCallbackType(ChannelMessageListener.class).call(channel, cci, message, cci.getClient().getHostname());
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

    public Api getApi() {
        return api;
    }

    @Override
    public StringConverter getStringConverter() {
        return myStringConverter;
    }

    @Override
    public void setIgnoreList(IgnoreList ignoreList) {
        myIgnoreList = ignoreList;
    }

    @Override
    public IgnoreList getIgnoreList() {
        return myIgnoreList;
    }

}
