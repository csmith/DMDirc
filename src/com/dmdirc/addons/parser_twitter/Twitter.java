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
import com.dmdirc.addons.parser_twitter.api.TwitterErrorHandler;
import com.dmdirc.addons.parser_twitter.api.TwitterMessage;
import com.dmdirc.addons.parser_twitter.api.TwitterStatus;
import com.dmdirc.addons.parser_twitter.api.TwitterUser;
import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.common.DefaultStringConverter;
import com.dmdirc.parser.common.IgnoreList;
import com.dmdirc.parser.common.MyInfo;
import com.dmdirc.parser.common.QueuePriority;
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
import com.dmdirc.parser.interfaces.callbacks.UnknownMessageListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateNoticeListener;
import com.dmdirc.parser.interfaces.callbacks.ServerReadyListener;
import com.dmdirc.parser.interfaces.callbacks.UserModeDiscoveryListener;
import com.dmdirc.parser.interfaces.callbacks.SocketCloseListener;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.util.IrcAddress;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dmdirc.addons.parser_twitter.api.TwitterException;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.parser.interfaces.callbacks.ChannelJoinListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelKickListener;
import java.lang.reflect.Method;
import java.util.Calendar;

/**
 * Twitter Parser for DMDirc.
 *
 * @author shane
 */
public class Twitter implements Parser, TwitterErrorHandler {
    /** Are we connected? */
    private boolean connected = false;

    /** Our owner plugin */
    private TwitterPlugin myPlugin = null;

    /** Twitter API. */
    private TwitterAPI api = new TwitterAPI("", "", "");

    /** Channels we are in. */
    private final Map<String, TwitterChannelInfo> channels = new HashMap<String, TwitterChannelInfo>();

    /** Clients we know. */
    private final Map<String, TwitterClientInfo> clients = new HashMap<String, TwitterClientInfo>();

    /** When did we last query the API? */
    private long lastQueryTime = 0;

    /** Username for twitter. */
    private String myUsername;

    /** Password for twitter if not able to use oauth. */
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

    /** Are we waiting for authentication? */
    private boolean wantAuth = false;

    /** Server we are connecting to. */
    final String myServerName;

    /** Address that created us. */
    final IrcAddress myAddress;
    
    /** Main Channel Name */
    final String mainChannelName;

    /**
     * Create a new Twitter Parser!
     *
     * @param myInfo The client information to use
     * @param address The address of the server to connect to
     * @param myPlugin Plugin that created this parser
     */
    protected Twitter(final MyInfo myInfo, final IrcAddress address, final TwitterPlugin myPlugin) {
        final String[] bits = address.getPassword().split(":");
        this.myUsername = bits[0];
        this.myPassword = (bits.length > 1) ? bits[1] : "";

        this.myPlugin = myPlugin;
        this.myServerName = address.getServer().toLowerCase();
        this.myAddress = address;

        this.mainChannelName = "&twitter";
    }

    /** {@inheritDoc} */
    @Override
    public void disconnect(final String message) {
        connected = false;
        currentParsers.remove(this);
        api = new TwitterAPI("", "", "");

        getCallbackManager().getCallbackType(SocketCloseListener.class).call();
    }

    /** {@inheritDoc} */
    @Override
    public void joinChannel(final String channel) {
        joinChannel(channel, "");
    }

    /** {@inheritDoc} */
    @Override
    public void joinChannel(final String channel, final String key) {
        if (isValidChannelName(channel) && getChannel(channel) == null && !channel.equalsIgnoreCase(mainChannelName)) {
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
            getCallbackManager().getCallbackType(NumericListener.class).call(474, new String[]{":"+myServerName, "474", myself.getNickname(), channel, "Cannot join channel - name is not valid, or you are already there."});
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
        sendRawMessage(message, QueuePriority.NORMAL);
    }

    /** {@inheritDoc} */
    @Override
    public void sendRawMessage(final String message, final QueuePriority priority) {
        // TODO: Parse some lines in order to fake IRC.
        final String[] bits = tokeniseLine(message);

        if (bits[0].equalsIgnoreCase("JOIN") && bits.length > 1) {
            joinChannel(bits[1]);
        } else if (bits[0].equalsIgnoreCase("WHOIS") && bits.length > 1) {
            if (bits[1].equalsIgnoreCase(myServerName)) {
                getCallbackManager().getCallbackType(NumericListener.class).call(311, new String[]{":"+myServerName, "311", myself.getNickname(), bits[1], "user", myServerName, "*", "Psuedo-User for DMDirc "+myServerName+" plugin"});
                getCallbackManager().getCallbackType(NumericListener.class).call(312, new String[]{":"+myServerName, "312", myself.getNickname(), bits[1], myServerName, "DMDirc "+myServerName+" plugin"});
            } else {
                final boolean forced = (bits.length > 2 && bits[1].equalsIgnoreCase(bits[2]));
                final TwitterUser user = (forced) ? api.getUser(bits[1], true) : api.getCachedUser(bits[1]);
                
                if (user == null) {
                    final String reason = (forced) ? "No such user found, see http://"+myAddress.getServer()+"/"+user.getScreenName() : "No such user found in cache, try /WHOIS "+bits[1]+" "+bits[1]+" to poll twitter (uses 1 API call) or try http://"+myAddress.getServer()+"/"+user.getScreenName();
                    getCallbackManager().getCallbackType(NumericListener.class).call(401, new String[]{":"+myServerName, "401", myself.getNickname(), bits[1], reason});
                } else {
                    // Time since last update
                    final long secondsIdle = ((user.getStatus() != null) ? System.currentTimeMillis() - user.getStatus().getTime() : user.getRegisteredTime()) / 1000;
                    final long signonTime = user.getRegisteredTime() / 1000;

                    getCallbackManager().getCallbackType(NumericListener.class).call(311, new String[]{":"+myServerName, "311", myself.getNickname(), bits[1], "user", myServerName, "*", user.getRealName()+" (http://"+myAddress.getServer()+"/"+user.getScreenName()+")"});

                    final TwitterClientInfo client = (TwitterClientInfo)getClient(bits[1]);
                    if (client != null) {
                        final StringBuilder channelList = new StringBuilder();

                        for (ChannelClientInfo cci : client.getChannelClients()) {
                            if (channelList.length() > 0) { channelList.append(" "); }
                            channelList.append(cci.getImportantModePrefix()+cci.getChannel().getName());
                        }

                        if (channelList.length() > 0) {
                            getCallbackManager().getCallbackType(NumericListener.class).call(319, new String[]{":"+myServerName, "319", myself.getNickname(), bits[1], channelList.toString()});
                        }
                    }

                    // AWAY Message Abuse!
                    getCallbackManager().getCallbackType(NumericListener.class).call(301, new String[]{":"+myServerName, "301", myself.getNickname(), bits[1], "URL: "+user.getURL()});
                    getCallbackManager().getCallbackType(NumericListener.class).call(301, new String[]{":"+myServerName, "301", myself.getNickname(), bits[1], "Bio: "+user.getDescription()});
                    getCallbackManager().getCallbackType(NumericListener.class).call(301, new String[]{":"+myServerName, "301", myself.getNickname(), bits[1], "Location: "+user.getLocation()});
                    getCallbackManager().getCallbackType(NumericListener.class).call(301, new String[]{":"+myServerName, "301", myself.getNickname(), bits[1], "Status: "+user.getStatus().getText()});
                    if (bits[1].equalsIgnoreCase(myself.getNickname())) {
                        final Long[] apiCalls = api.getRemainingApiCalls();
                        getCallbackManager().getCallbackType(NumericListener.class).call(301, new String[]{":"+myServerName, "301", myself.getNickname(), bits[1], "API Allowance: "+apiCalls[1]});
                        getCallbackManager().getCallbackType(NumericListener.class).call(301, new String[]{":"+myServerName, "301", myself.getNickname(), bits[1], "API Allowance Remaining: "+apiCalls[0]});
                        getCallbackManager().getCallbackType(NumericListener.class).call(301, new String[]{":"+myServerName, "301", myself.getNickname(), bits[1], "API Calls Used: "+apiCalls[3]});
                    }

                    getCallbackManager().getCallbackType(NumericListener.class).call(312, new String[]{":"+myServerName, "312", myself.getNickname(), bits[1], myServerName, "DMDirc "+myServerName+" plugin"});
                    getCallbackManager().getCallbackType(NumericListener.class).call(317, new String[]{":"+myServerName, "317", myself.getNickname(), bits[1], Long.toString(secondsIdle), Long.toString(signonTime), "seconds idle, signon time"});
                }
            }

            getCallbackManager().getCallbackType(NumericListener.class).call(318, new String[]{":"+myServerName, "318", myself.getNickname(), bits[1], "End of /WHOIS list."});
        } else if (bits[0].equalsIgnoreCase("INVITE") && bits.length > 2) {
            if (bits[2].equalsIgnoreCase(mainChannelName)) {
                final TwitterUser user = api.addFriend(bits[1]);
                final TwitterChannelInfo channel = (TwitterChannelInfo) getChannel(bits[2]);
                if (channel != null && user != null) {
                    final TwitterClientInfo ci = new TwitterClientInfo(user.getScreenName(), this);
                    clients.put(ci.getNickname().toLowerCase(), ci);
                    final TwitterChannelClientInfo cci = new TwitterChannelClientInfo(channel, ci);

                    channel.addChannelClient(cci);
                    getCallbackManager().getCallbackType(ChannelJoinListener.class).call(channel, cci);
                }
            } else {
                getCallbackManager().getCallbackType(NumericListener.class).call(474, new String[]{":"+myServerName, "482", myself.getNickname(), bits[1], "You can't do that here."});
            }
        } else if (bits[0].equalsIgnoreCase("KICK") && bits.length > 2) {
            if (bits[1].equalsIgnoreCase(mainChannelName)) {
                final TwitterChannelInfo channel = (TwitterChannelInfo) getChannel(bits[1]);
                if (channel != null) {
                    final TwitterChannelClientInfo cci = (TwitterChannelClientInfo) channel.getChannelClient(bits[2]);
                    if (cci != null) { cci.kick("Bye"); }
                    final TwitterChannelClientInfo mycci = (TwitterChannelClientInfo) channel.getChannelClient(myUsername);
                    getCallbackManager().getCallbackType(ChannelKickListener.class).call(channel, cci, mycci, "", myUsername);
                }
            } else {
                getCallbackManager().getCallbackType(NumericListener.class).call(474, new String[]{":"+myServerName, "482", myself.getNickname(), bits[1], "You can't do that here."});
            }
        } else {
            getCallbackManager().getCallbackType(NumericListener.class).call(474, new String[]{":"+myServerName, "421", myself.getNickname(), bits[0], "Unknown Command - "+message});
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isValidChannelName(final String name) {
        return (name.matches("^&[0-9]+$") || name.equalsIgnoreCase(mainChannelName) || name.startsWith("#"));
    }

    /** {@inheritDoc} */
    @Override
    public String getServerName() {
        return myServerName + "/" + myself.getNickname();
    }

    /** {@inheritDoc} */
    @Override
    public String getNetworkName() {
        return myServerName + "/" + myself.getNickname();
    }

    /** {@inheritDoc} */
    @Override
    public String getServerSoftware() {
        return myServerName;
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
        sendPrivateNotice("This parser does not support CTCPs.");
    }

    /** {@inheritDoc} */
    @Override
    public void sendCTCPReply(final String target, final String type, final String message) {
        sendPrivateNotice("This parser does not support CTCP replies.");
    }

    /** {@inheritDoc} */
    @Override
    public void sendMessage(final String target, final String message) {
        final TwitterChannelInfo channel = (TwitterChannelInfo) this.getChannel(target);
        if (target.equalsIgnoreCase(mainChannelName)) {
            if (wantAuth) {
                final String[] bits = message.split(" ");
                if (bits[0].equalsIgnoreCase("usepw")) {
                    sendChannelMessage(channel, "Switching to once-off password authentication, please enter your password.");
                    api.setUseOAuth(false);
                    return;
                }
                try {
                    if (api.useOAuth()) {
                        api.setAccessPin(bits[0]);
                        if (api.isAllowed(true)) {
                            IdentityManager.getConfigIdentity().setOption(myPlugin.getDomain(), "token-"+myServerName+"-"+myUsername, api.getToken());
                            IdentityManager.getConfigIdentity().setOption(myPlugin.getDomain(), "tokenSecret-"+myServerName+"-"+myUsername, api.getTokenSecret());
                            sendChannelMessage(channel, "Thank you for authorising DMDirc.");
                            updateTwitterChannel();
                            wantAuth = false;
                        } else {
                            sendChannelMessage(channel, "Authorising DMDirc failed, please try again: "+api.getOAuthURL());
                        }
                    } else {
                        api.setPassword(message);
                        if (api.isAllowed(true)) {
                            sendChannelMessage(channel, "Password accepted. Please note you will need to do this every time unless your password is given in the URL.");
                            updateTwitterChannel();
                            wantAuth = false;
                        } else {
                            sendChannelMessage(channel, "Password seems incorrect, please try again.");
                        }

                    }
                } catch (TwitterException te) {
                    sendChannelMessage(channel, "There was a problem authorising DMDirc ("+te.getCause().getMessage()+").");
                    sendChannelMessage(channel, "Please try again: "+api.getOAuthURL());
                }
            } else {
                if (setStatus(message)) {
                    sendPrivateNotice("Setting status ok.");
                } else {
                    sendPrivateNotice("Setting status failed.");
                }
            }
        } else if (wantAuth) {
            sendPrivateNotice("DMDirc has not been authorised to use this account yet.");
        } else if (target.matches("^&[0-9]+$")) {
            try {
                long id = Long.parseLong(target.substring(1));
                if (setStatus(message, id)) {
                    sendPrivateNotice("Setting status ok.");
                } else {
                    sendPrivateNotice("Setting status failed.");
                }
            } catch (NumberFormatException nfe) { }
        } else if (!target.matches("^#.+$")) {
            api.newDirectMessage(target, message);
        } else {
            sendPrivateNotice("Messages to '"+target+"' are not currently supported.");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void sendNotice(final String target, final String message) {
        sendPrivateNotice("This parser does not support notices.");
    }

    /** {@inheritDoc} */
    @Override
    public void sendAction(final String target, final String message) {
        sendPrivateNotice("This parser does not support CTCPs.");
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
     * Send a notice to the client.
     *
     * @param message Message to send.
     */
    private void sendPrivateNotice(final String message) {
        getCallbackManager().getCallbackType(PrivateNoticeListener.class).call(message, myServerName);
    }

    /**
     * Send a PM to the client.
     *
     * @param message Message to send.
     */
    private void sendPrivateMessage(final String message) {
        sendPrivateMessage(message, myServerName);
    }

    /**
     * Send a PM to the client.
     *
     * @param message Message to send.
     * @param hostname Who is the message from?
     */
    private void sendPrivateMessage(final String message, final String hostname) {
        sendPrivateMessage(message, hostname, myUsername);
    }

    /**
     * Send a PM to the client.
     *
     * @param message Message to send.
     * @param hostname Who is the message from?
     * @param target Who is the message to?
     */
    private void sendPrivateMessage(final String message, final String hostname, final String target) {
        if (hostname.equalsIgnoreCase(myUsername)) {
            getCallbackManager().getCallbackType(UnknownMessageListener.class).call(message, target, hostname);
        } else {
            getCallbackManager().getCallbackType(PrivateMessageListener.class).call(message, hostname);
        }
    }

    /**
     * Send a message to the given channel.
     *
     * @param channel Channel to send message to
     * @param message Message to send.
     */
    private void sendChannelMessage(final ChannelInfo channel, final String message) {
        sendChannelMessage(channel, message, myServerName);
    }

    /**
     * Send a message to the given channel.
     *
     * @param channel Channel to send message to
     * @param message Message to send.
     * @param hostname Hostname that the message is from.
     */
    private void sendChannelMessage(final ChannelInfo channel, final String message, final String hostname) {
        sendChannelMessage(channel, message, null, hostname);
    }

    /**
     * Send a message to the given channel.
     *
     * @param channel Channel to send message to
     * @param message Message to send.
     * @param cci Channel Client to send from
     * @param hostname Hostname that the message is from.
     */
    private void sendChannelMessage(final ChannelInfo channel, final String message, final ChannelClientInfo cci, final String hostname) {
        getCallbackManager().getCallbackType(ChannelMessageListener.class).call(channel, cci, message, hostname);
    }

    /**
     * Show the user an ascii failwhale!
     */
    public void showFailWhale() {
        sendPrivateNotice(""+Styliser.CODE_FIXED+Styliser.CODE_HEXCOLOUR+"EB5405,71C5C5                        ");
        sendPrivateNotice(""+Styliser.CODE_FIXED+Styliser.CODE_HEXCOLOUR+"EB5405,71C5C5  W     W      W        ");
        sendPrivateNotice(""+Styliser.CODE_FIXED+Styliser.CODE_HEXCOLOUR+"EB5405,71C5C5  W        W  W     W   ");
        sendPrivateNotice(""+Styliser.CODE_FIXED+Styliser.CODE_HEXCOLOUR+"FFFFFF,71C5C5                '."+Styliser.CODE_HEXCOLOUR+"EB5405,71C5C5  W   ");
        sendPrivateNotice(""+Styliser.CODE_FIXED+Styliser.CODE_HEXCOLOUR+"FFFFFF,71C5C5    .-\"\"-._     \\ \\.--| ");
        sendPrivateNotice(""+Styliser.CODE_FIXED+Styliser.CODE_HEXCOLOUR+"FFFFFF,71C5C5   /       \"-..__) .-'  ");
        sendPrivateNotice(""+Styliser.CODE_FIXED+Styliser.CODE_HEXCOLOUR+"FFFFFF,71C5C5  |     _         /     ");
        sendPrivateNotice(""+Styliser.CODE_FIXED+Styliser.CODE_HEXCOLOUR+"FFFFFF,71C5C5  \\'-.__,   .__.,'      ");
        sendPrivateNotice(""+Styliser.CODE_FIXED+Styliser.CODE_HEXCOLOUR+"FFFFFF,71C5C5   `'----'._\\--'        ");
        sendPrivateNotice(""+Styliser.CODE_FIXED+Styliser.CODE_HEXCOLOUR+"FFFFFF,71C5C5V"+Styliser.CODE_HEXCOLOUR+"EB5405,71C5C5V"+Styliser.CODE_HEXCOLOUR+"FFFFFF,71C5C5V"+Styliser.CODE_HEXCOLOUR+"EB5405,71C5C5V"+Styliser.CODE_HEXCOLOUR+"FFFFFF,71C5C5V"+Styliser.CODE_HEXCOLOUR+"EB5405,71C5C5V"+Styliser.CODE_HEXCOLOUR+"FFFFFF,71C5C5V"+Styliser.CODE_HEXCOLOUR+"EB5405,71C5C5V"+Styliser.CODE_HEXCOLOUR+"FFFFFF,71C5C5V"+Styliser.CODE_HEXCOLOUR+"EB5405,71C5C5V"+Styliser.CODE_HEXCOLOUR+"FFFFFF,71C5C5V"+Styliser.CODE_HEXCOLOUR+"EB5405,71C5C5V"+Styliser.CODE_HEXCOLOUR+"FFFFFF,71C5C5V"+Styliser.CODE_HEXCOLOUR+"EB5405,71C5C5V"+Styliser.CODE_HEXCOLOUR+"FFFFFF,71C5C5V"+Styliser.CODE_HEXCOLOUR+"EB5405,71C5C5V"+Styliser.CODE_HEXCOLOUR+"FFFFFF,71C5C5V"+Styliser.CODE_HEXCOLOUR+"EB5405,71C5C5V"+Styliser.CODE_HEXCOLOUR+"FFFFFF,71C5C5V"+Styliser.CODE_HEXCOLOUR+"EB5405,71C5C5V"+Styliser.CODE_HEXCOLOUR+"FFFFFF,71C5C5V"+Styliser.CODE_HEXCOLOUR+"EB5405,71C5C5V"+Styliser.CODE_HEXCOLOUR+"FFFFFF,71C5C5V"+Styliser.CODE_HEXCOLOUR+"EB5405,71C5C5V");
    }

    /**
     * Run the twitter parser.
     */
    @Override
    public void run() {
        resetState();

        // Get the consumerKey and consumerSecret for this server if known
        // else default to our twitter key and secret
        final String consumerKey;
        final String consumerSecret;
        if (IdentityManager.getGlobalConfig().hasOptionString(myPlugin.getDomain(), "consumerKey-"+myServerName)) { 
            consumerKey = IdentityManager.getGlobalConfig().getOption(myPlugin.getDomain(), "consumerKey-"+myServerName);
        } else { consumerKey = "qftK3mAbLfbWWHf8shiyjw"; }
        if (IdentityManager.getGlobalConfig().hasOptionString(myPlugin.getDomain(), "consumerSecret-"+myServerName)) { 
            consumerSecret = IdentityManager.getGlobalConfig().getOption(myPlugin.getDomain(), "consumerSecret-"+myServerName);
        } else { consumerSecret = "flPr2TJGp4795DeTu4VkUlNLX8g25SpXWXZ7SKW0Bg"; }

        final String token;
        final String tokenSecret;

        if (IdentityManager.getGlobalConfig().hasOptionString(myPlugin.getDomain(), "token-"+myServerName+"-"+myUsername)) {
            token = IdentityManager.getGlobalConfig().getOption(myPlugin.getDomain(), "token-"+myServerName+"-"+myUsername);
        } else { token = ""; }
        if (IdentityManager.getGlobalConfig().hasOptionString(myPlugin.getDomain(), "tokenSecret-"+myServerName+"-"+myUsername)) {
            tokenSecret = IdentityManager.getGlobalConfig().getOption(myPlugin.getDomain(), "tokenSecret-"+myServerName+"-"+myUsername);
        } else { tokenSecret = ""; }

        final StringBuilder serverExtra = new StringBuilder("/");
        for (String addressChannel : myAddress.getChannels()) {
            if (serverExtra.length() > 1) { serverExtra.append(","); }
            serverExtra.append(addressChannel);
        }

        api = new TwitterAPI(myUsername, myPassword, myServerName+serverExtra.toString(), consumerKey, consumerSecret, token, tokenSecret);
        api.setSource("DMDirc");
        currentParsers.add(this);
        connected = true;
        
        final TwitterChannelInfo channel = new TwitterChannelInfo(mainChannelName, this);
        channels.put(mainChannelName, channel);
        channel.addChannelClient(new TwitterChannelClientInfo(channel, myself));

        // Fake 001
        getCallbackManager().getCallbackType(ServerReadyListener.class).call();
        // Fake 005
        getCallbackManager().getCallbackType(NetworkDetectedListener.class).call(this.getNetworkName(), this.getServerSoftware(), this.getServerSoftwareType());
        getCallbackManager().getCallbackType(Post005Listener.class).call();
        // Fake MOTD
        getCallbackManager().getCallbackType(AuthNoticeListener.class).call("Welcome to "+myServerName+".");
        getCallbackManager().getCallbackType(MotdStartListener.class).call("- "+myServerName+" Message of the Day -");
        getCallbackManager().getCallbackType(MotdLineListener.class).call("- This is an experimental parser, to allow DMDirc to use "+myServerName);
        getCallbackManager().getCallbackType(MotdLineListener.class).call("- ");
        getCallbackManager().getCallbackType(MotdLineListener.class).call("- Your timeline appears in "+mainChannelName+" (topic is your last status)");
        getCallbackManager().getCallbackType(MotdLineListener.class).call("- All messages sent to this channel (or topics set) will cause the status to be set.");
        getCallbackManager().getCallbackType(MotdLineListener.class).call("- ");
        getCallbackManager().getCallbackType(MotdLineListener.class).call("- Messages can be replied to using /msg &<messageid> <reply>");
        getCallbackManager().getCallbackType(MotdLineListener.class).call("- ");
        getCallbackManager().getCallbackType(MotdEndListener.class).call(false, "End of /MOTD command");
        // Fake some more on-connect crap
        getCallbackManager().getCallbackType(UserModeDiscoveryListener.class).call(myself, "");

        channel.setLocalTopic("No status known.");
        this.doJoinChannel(channel);

        sendChannelMessage(channel, "Checking to see if we have been authorised to use the account \""+api.getUsername()+"\"...");

        if (!api.isAllowed()) {
            wantAuth = true;
            if (api.useOAuth()) {
                sendChannelMessage(channel, "Sorry, DMDirc has not been authorised to use the account \""+api.getUsername()+"\"");
                sendChannelMessage(channel, "");
                sendChannelMessage(channel, "Before you can use DMDirc with "+myServerName+" you need to authorise it.");
                sendChannelMessage(channel, "");
                sendChannelMessage(channel, "To do this, please visit: "+api.getOAuthURL());
                sendChannelMessage(channel, "and then type the pin number here.");
            } else {
                sendChannelMessage(channel, "Sorry, You did not provide DMDirc with a password for the account \""+api.getUsername()+"\" and the server \""+myServerName+"\" does not support OAuth or is not accepting our key.");
                sendChannelMessage(channel, "");
                sendChannelMessage(channel, "Before you can use DMDirc with "+myServerName+" you need to provide a password.");
                sendChannelMessage(channel, "");
                sendChannelMessage(channel, "To do this, please type the password here, or set it correctly in the URL (twitter://" + myUsername + ":your_password@"+myServerName+serverExtra.toString()+").");
            }
        } else {
            sendChannelMessage(channel, "DMDirc has been authorised to use the account \""+api.getUsername()+"\"");
            updateTwitterChannel();
        }

        long lastReplyId = -1;
        long lastTimelineId = -1;
        long lastDirectMessageId = -1;

        if (IdentityManager.getGlobalConfig().getOptionBool(myPlugin.getDomain(), "saveLastIDs")) {
            if (IdentityManager.getGlobalConfig().hasOptionString(myPlugin.getDomain(), "lastReplyId-"+myServerName+"-"+myUsername)) {
                lastReplyId = TwitterAPI.parseLong(IdentityManager.getGlobalConfig().getOption(myPlugin.getDomain(), "lastReplyId-"+myServerName+"-"+myUsername), -1);
            }
            if (IdentityManager.getGlobalConfig().hasOptionString(myPlugin.getDomain(), "lastRlastTimelineIdeplyId-"+myServerName+"-"+myUsername)) {
                lastTimelineId = TwitterAPI.parseLong(IdentityManager.getGlobalConfig().getOption(myPlugin.getDomain(), "lastTimelineId-"+myServerName+"-"+myUsername), -1);
            }
            if (IdentityManager.getGlobalConfig().hasOptionString(myPlugin.getDomain(), "lastDirectMessageId-"+myServerName+"-"+myUsername)) {
                lastDirectMessageId = TwitterAPI.parseLong(IdentityManager.getGlobalConfig().getOption(myPlugin.getDomain(), "lastDirectMessageId-"+myServerName+"-"+myUsername), -1);
            }
        }

        int count = 0;
        final long pruneCount = 20; // Every 20 loops, clear the status cache of
        final long pruneTime = 3600 * 1000 ; // anything older than 1 hour.
        while (connected) {
            final int startCalls = (wantAuth) ? 0 : api.getUsedCalls();

            if (!wantAuth && api.isAllowed()) {
                lastQueryTime = System.currentTimeMillis();

                final int statusesPerAttempt = Math.min(200, IdentityManager.getGlobalConfig().getOptionInt(myPlugin.getDomain(), "statuscount"));

                final List<TwitterStatus> statuses = new ArrayList<TwitterStatus>();
                for (TwitterStatus status : api.getReplies(lastReplyId, statusesPerAttempt)) {
                    statuses.add(status);
                    if (status.getID() > lastReplyId) { lastReplyId = status.getID(); }
                }

                for (TwitterStatus status : api.getFriendsTimeline(lastTimelineId, statusesPerAttempt)) {
                    if (!statuses.contains(status)) {
                        statuses.add(status);
                    }
                    // Add new friends that may have been added elsewhere.
                    if (channel.getChannelClient(status.getUser().getScreenName()) == null) {
                        final TwitterClientInfo ci = new TwitterClientInfo(status.getUser().getScreenName(), this);
                        clients.put(ci.getNickname().toLowerCase(), ci);
                        final TwitterChannelClientInfo cci = new TwitterChannelClientInfo(channel, ci);

                        channel.addChannelClient(cci);
                        getCallbackManager().getCallbackType(ChannelJoinListener.class).call(channel, cci);
                    }
                    if (status.getID() > lastTimelineId) { lastTimelineId = status.getID(); }
                }

                Collections.sort(statuses);

                for (TwitterStatus status : statuses) {
                    final ChannelClientInfo cci = channel.getChannelClient(status.getUser().getScreenName());
                    final String message;
                    if (status.getReplyTo() > 0) {
                        message = String.format("%s   %c15 &%d %cin reply to%4$c &%d", status.getText(), Styliser.CODE_COLOUR, status.getID(), Styliser.CODE_ITALIC, status.getReplyTo());
                    } else {
                        message = String.format("%s    %c15 &%d", status.getText(), Styliser.CODE_COLOUR, status.getID());
                    }
                    final String hostname = status.getUser().getScreenName();
                    sendChannelMessage(channel, message, cci, hostname);
                }

                final List<TwitterMessage> directMessages = new ArrayList<TwitterMessage>();
                for (TwitterMessage directMessage : api.getDirectMessages(lastDirectMessageId)) {
                    directMessages.add(directMessage);
                    if (directMessage.getID() > lastDirectMessageId) { lastDirectMessageId = directMessage.getID(); }
                }

                if (IdentityManager.getGlobalConfig().getOptionBool(myPlugin.getDomain(), "getSentMessages")) {
                    for (TwitterMessage directMessage : api.getSentDirectMessages(lastDirectMessageId)) {
                        directMessages.add(directMessage);
                        if (directMessage.getID() > lastDirectMessageId) { lastDirectMessageId = directMessage.getID(); }
                    }
                }
                Collections.sort(directMessages);

                for (TwitterMessage dm : directMessages) {
                    System.out.println("<"+dm.getSenderScreenName()+" - "+dm.getTargetScreenName()+" > "+dm.getText());
                    sendPrivateMessage(dm.getText(), dm.getSenderScreenName(), dm.getTargetScreenName());
                }

                checkTopic(channel);
            }

            if (IdentityManager.getGlobalConfig().getOptionBool(myPlugin.getDomain(), "saveLastIDs")) {
                IdentityManager.getConfigIdentity().setOption(myPlugin.getDomain(), "lastReplyId-"+myServerName+"-"+myUsername, Long.toString(lastReplyId));
                IdentityManager.getConfigIdentity().setOption(myPlugin.getDomain(), "lastTimelineId-"+myServerName+"-"+myUsername, Long.toString(lastTimelineId));
                IdentityManager.getConfigIdentity().setOption(myPlugin.getDomain(), "lastDirectMessageId-"+myServerName+"-"+myUsername, Long.toString(lastDirectMessageId));
            }

            final boolean debug = IdentityManager.getGlobalConfig().getOptionBool(myPlugin.getDomain(), "debugEnabled");

            final int apiLimit = IdentityManager.getGlobalConfig().getOptionInt(myPlugin.getDomain(), "apicalls");
            final int endCalls = (wantAuth) ? 0 : api.getUsedCalls();
            final Long[] apiCalls = api.getRemainingApiCalls();
            if (debug) { System.out.println("Twitter calls Remaining: "+apiCalls[0]); }
            // laconica doesn't rate limit, so time to reset is always 0, in this case
            // we will assume the time of the next hour.
            final Calendar cal = Calendar.getInstance();
            cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY)+1, 0, 0) ;

            final Long timeLeft = ((apiCalls[2] > 0) ? apiCalls[2] : cal.getTimeInMillis()) - System.currentTimeMillis();
            final long sleepTime;
            if (wantAuth) {
                // When waiting for auth, sleep for less time so that when the
                // auth happens, we can quickly start twittering!
                sleepTime = 5 * 1000;
            } else if (!api.isAllowed()) {
                // If we aren't allowed, but aren't waiting for auth, then
                // sleep for 1 minute.
                sleepTime = 60 * 1000;
            } else if (apiCalls[1] == 0L) {
                // Twitter has said we have no API Calls in total, so sleep for
                // 10 minutes and try again.
                // (This will also happen if twitter didn't respond for some reason)
                sleepTime = 10 * 60 * 1000;
                // Also alert the user.
                if (Math.random() <= 0.25) {
                    showFailWhale();
                } else {
                    sendPrivateNotice("Unable to communicate with twitter, or no API calls allowed at all, retrying in 10 minutes.");
                }
            } else if (api.getUsedCalls() > apiLimit) {
                // Sleep for the rest of the hour, we have done too much!
                sleepTime = timeLeft;
            } else {
                // Else work out how many calls we have left.
                // Whichever is less between the number of calls we want to make
                // and the number of calls twitter is going to allow us to make.
                final long callsLeft = Math.min(apiLimit - api.getUsedCalls(), apiCalls[0]);
                // How many calls do we make each time?
                // If this is less than 0 (If there was a time reset between
                // calculating the start and end calls used) then assume 3.
                final long callsPerTime = (endCalls - startCalls) > 0 ? (endCalls - startCalls) : 3;

                if (debug) {
                    System.out.println("\tCalls Remaining: "+callsLeft);
                    System.out.println("\tCalls per time: "+callsPerTime);
                }

                // And divide this by the number of calls we make each time to
                // see how many times we have to sleep this hour.
                final long sleepsRequired = callsLeft / callsPerTime;

                if (debug) {
                    System.out.println("\tSleeps Required: "+sleepsRequired);
                    System.out.println("\tTime Left: "+timeLeft);
                }

                // Then finally discover how long we need to sleep for.
                sleepTime = (sleepsRequired > 0) ? timeLeft / sleepsRequired : timeLeft;
            }

            if (debug) { System.out.println("Sleeping for: "+sleepTime); }

            // Sleep for sleep time, 
            // If we have a negative sleep time, use 5 minutes.
            try { Thread.sleep((sleepTime > 0) ? sleepTime : 5 * 60 * 1000); } catch (InterruptedException ex) { }
            
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
            final TwitterChannelInfo channel = (TwitterChannelInfo) this.getChannel(mainChannelName);
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
        final TwitterChannelInfo channel = (TwitterChannelInfo) getChannel(mainChannelName);
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
        api.getFollowers();
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

        if (!newStatus.equalsIgnoreCase(oldStatus)) {
            channel.setTopicSetter(myUsername);
            channel.setTopicTime(System.currentTimeMillis());
            channel.setLocalTopic(newStatus);
            getCallbackManager().getCallbackType(ChannelTopicListener.class).call(channel, false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void handleTwitterError(final TwitterAPI api, final Throwable t, final String source, final String twitterInput, final String twitterOutput, final String message) {
        try {
            if (!message.isEmpty()) {
                sendPrivateNotice("Error: "+message);
            }
            sendPrivateNotice(t.getClass().getSimpleName()+": "+t+" -> "+t.getMessage());
        
            // If debugging is enabled, also let the user know all this and more in PM.
            if (IdentityManager.getGlobalConfig().getOptionBool(myPlugin.getDomain(), "debugEnabled")) {
                if (!message.isEmpty()) {
                    sendPrivateMessage("Error: "+message);
                }
                sendPrivateMessage(t.getClass().getSimpleName()+": "+t+" -> "+t.getMessage());

                // And give more information:
                sendPrivateMessage("Source: "+source);
                sendPrivateMessage("Input: "+twitterInput);
                sendPrivateMessage("Output: ");
                for (String out : twitterOutput.split("\n")) {
                    sendPrivateMessage("                "+out);
                }
                sendPrivateMessage("");
                sendPrivateMessage("Exception:");

                // Hax the error manager to get a nice String[] representing the stack trace and output it.
                try {
                    final Method gt = ErrorManager.class.getDeclaredMethod("getTrace");
                    gt.setAccessible(true);
                    final String[] trace = (String[]) gt.invoke(ErrorManager.getErrorManager(), t);

                    for (String out : trace) {
                        sendPrivateMessage("                "+out);
                    }
                } catch (NoSuchMethodException ex) {
                    sendPrivateMessage("    ... Unable to get StackTrace (nsme)");
                } catch (SecurityException ex) {
                    sendPrivateMessage("    ... Unable to get StackTrace (se)");
                } catch (IllegalAccessException ex) {
                    sendPrivateMessage("    ... Unable to get StackTrace (iae)");
                } catch (IllegalArgumentException ex) {
                    sendPrivateMessage("    ... Unable to get StackTrace (iae2)");
                } catch (InvocationTargetException ex) {
                    sendPrivateMessage("    ... Unable to get StackTrace (ite)");
                }

                sendPrivateMessage("==================================");
            }
        } catch (Throwable t2) {
            System.out.println("wtf? "+t2);
            t2.printStackTrace();
        }
    }
}