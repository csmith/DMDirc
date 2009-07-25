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

package com.dmdirc.addons.parser_email;

import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.common.DefaultStringConverter;
import com.dmdirc.parser.common.IgnoreList;
import com.dmdirc.parser.common.MyInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.LocalClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.StringConverter;
import com.dmdirc.parser.interfaces.callbacks.NetworkDetectedListener;
import com.dmdirc.parser.interfaces.callbacks.Post005Listener;
import com.dmdirc.parser.interfaces.callbacks.ServerReadyListener;
import com.dmdirc.parser.interfaces.callbacks.SocketCloseListener;
import com.dmdirc.util.IrcAddress;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 *
 * @author chris
 */
public class EmailParser implements Parser {

    private final StringConverter converter = new DefaultStringConverter();
    private Session session;
    private CallbackManager<EmailParser> manager = new EmailCallbackManager(this);
    private IgnoreList ignoreList = new IgnoreList();

    private Store store;
    
    private final IrcAddress address;
    private final EmailLocalClient local;
    private final Map<String, EmailChannelInfo> channels
            = new HashMap<String, EmailChannelInfo>();

    public EmailParser(MyInfo myInfo, IrcAddress address) {
        InternetAddress addr = null;

        try {
            addr = new InternetAddress(address.getPassword()
                    + "@" + address.getServer(), myInfo.getRealname());
        } catch (UnsupportedEncodingException ex) {
            // Do nothing, we'll break horribly later
        }

        this.local = new EmailLocalClient(this, addr);
        this.address = address;
    }

    /** {@inheritDoc} */
    @Override
    public void disconnect(String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void joinChannel(String channel) {
        getChannel(channel);
    }

    /** {@inheritDoc} */
    @Override
    public void joinChannel(String channel, String key) {
        getChannel(channel);
    }

    /** {@inheritDoc} */
    @Override
    public ChannelInfo getChannel(String channel) {
        if (!channels.containsKey(channel)) {
            try {
                EmailChannelInfo cinfo;

                if (channel.startsWith("#")) {
                    cinfo = new EmailChannelInfo(this,
                            store.getFolder(channel.substring(1)));
                } else {
                    final int lof = channel.lastIndexOf('/');
                    cinfo = new EmailChannelInfo(this,
                            store.getFolder(channel.substring(1, lof)),
                            Integer.parseInt(channel.substring(1 + lof)));
                }

                channels.put(channel, cinfo);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        return channels.get(channel);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<? extends ChannelInfo> getChannels() {
        return channels.values();
    }

    /** {@inheritDoc} */
    @Override
    public void setBindIP(String ip) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxLength(String type, String target) {
        return Integer.MAX_VALUE;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxLength() {
        return Integer.MAX_VALUE;
    }

    /** {@inheritDoc} */
    @Override
    public LocalClientInfo getLocalClient() {
        return local;
    }

    /** {@inheritDoc} */
    @Override
    public ClientInfo getClient(String details) {
        try {
            return new EmailClientInfo(this, new InternetAddress(details));
        } catch (AddressException ex) {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void sendRawMessage(String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public StringConverter getStringConverter() {
        return converter;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isValidChannelName(String name) {
        return name.matches("^[#^]\\S+$");
    }

    /** {@inheritDoc} */
    @Override
    public String getServerName() {
        return address.getServer();
    }

    /** {@inheritDoc} */
    @Override
    public String getNetworkName() {
        return "<email>";
    }

    /** {@inheritDoc} */
    @Override
    public String getServerSoftware() {
        return "TODO";
    }

    /** {@inheritDoc} */
    @Override
    public String getServerSoftwareType() {
        return "TODO";
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxTopicLength() {
        return -1;
    }

    /** {@inheritDoc} */
    @Override
    public String getBooleanChannelModes() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public String getListChannelModes() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxListModes(char mode) {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isUserSettable(char mode) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String getParameterChannelModes() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public String getDoubleParameterChannelModes() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public String getUserModes() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public String getChannelUserModes() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public CallbackManager<? extends Parser> getCallbackManager() {
        return manager;
    }

    /** {@inheritDoc} */
    @Override
    public long getServerLatency() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void sendCTCP(String target, String type, String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void sendCTCPReply(String target, String type, String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void sendMessage(String target, String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void sendNotice(String target, String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void sendAction(String target, String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getLastLine() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public void setIgnoreList(IgnoreList ignoreList) {
        this.ignoreList = ignoreList;
    }

    /** {@inheritDoc} */
    @Override
    public IgnoreList getIgnoreList() {
        return ignoreList;
    }

    /** {@inheritDoc} */
    @Override
    public String[] parseHostmask(String hostmask) {
        String[] first = hostmask.split("!", 2);
        String[] second = first[1].split("@", 2);
        
        return new String[]{first[0], second[0], second[1]};
    }

    /** {@inheritDoc} */
    @Override
    public int getLocalPort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public long getPingTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setPingTimerInterval(long newValue) {
        // TODO?
    }

    /** {@inheritDoc} */
    @Override
    public long getPingTimerInterval() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public void setPingTimerFraction(int newValue) {
        // TODO?
    }

    /** {@inheritDoc} */
    @Override
    public int getPingTimerFraction() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", address.getProtocol()
                + (address.isSSL() ? "s" : ""));
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", address.getServer());
        props.setProperty("mail.user", address.getPassword().split(":")[0]);

        try {
            session = Session.getInstance(props);
            store = session.getStore();
            store.addConnectionListener(new ConnectionListener() {

                @Override
                public void opened(ConnectionEvent arg0) {
                    try {
                        manager.getCallbackType(ServerReadyListener.class).call();
                        manager.getCallbackType(NetworkDetectedListener.class)
                                .call(getNetworkName(), getServerSoftware(),
                                getServerSoftwareType());
                        manager.getCallbackType(Post005Listener.class).call();
                        
                        for (Folder def : store.getDefaultFolder().listSubscribed()) {
                            channels.put("#" + def.getFullName(),
                                    new EmailChannelInfo(EmailParser.this, def));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void disconnected(ConnectionEvent arg0) {
                    manager.getCallbackType(SocketCloseListener.class).call();
                }

                @Override
                public void closed(ConnectionEvent arg0) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });

            store.connect(address.getPassword().split(":")[0],
                    address.getPassword().split(":")[1]);
        } catch (NoSuchProviderException ex) {
            ex.printStackTrace();
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
    }

}
