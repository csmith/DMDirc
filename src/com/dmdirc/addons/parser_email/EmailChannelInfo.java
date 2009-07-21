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

import com.dmdirc.parser.common.ChannelListModeItem;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.ChannelMessageListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelNamesListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelSelfJoinListener;

import com.dmdirc.parser.interfaces.callbacks.ChannelTopicListener;
import com.dmdirc.ui.messages.Styliser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.mail.Address;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.event.ConnectionAdapter;
import javax.mail.event.ConnectionEvent;
import javax.mail.internet.InternetAddress;

/**
 *
 * @author chris
 */
public class EmailChannelInfo implements ChannelInfo, Runnable {

    private final int message;
    private final ChannelClientInfo localinfo;
    private final List<ChannelClientInfo> clients = new ArrayList<ChannelClientInfo>();
    private final EmailParser parser;
    private Message msg = null;
    private final Folder folder;

    public EmailChannelInfo(final EmailParser parser, Folder folder) {
        this.parser = parser;
        this.folder = folder;
        this.message = -1;
        this.localinfo = new EmailChannelClientInfo(parser.getLocalClient(), this);
        clients.add(localinfo);

        parser.getCallbackManager().getCallbackType(ChannelSelfJoinListener.class).call(this);
        parser.getCallbackManager().getCallbackType(ChannelNamesListener.class).call(this);

        try {
            if (folder.getType() == Folder.HOLDS_FOLDERS) {
                // Meh?
            } else {
                folder.addConnectionListener(new ConnectionAdapter() {

                    @Override
                    public void opened(ConnectionEvent e) {
                        new Thread(EmailChannelInfo.this,
                                "Message announce thread - " + getName()).start();
                    }
                });
                
                folder.open(Folder.READ_ONLY);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public EmailChannelInfo(final EmailParser parser, final Folder folder, final int message) {
        this.parser = parser;
        this.folder = folder;
        this.message = message;
        this.localinfo = new EmailChannelClientInfo(parser.getLocalClient(), this);

        parser.getCallbackManager().getCallbackType(ChannelSelfJoinListener.class).call(this);

        try {
            if (folder.getType() == Folder.HOLDS_FOLDERS) {
                // Meh?
            } else {
                folder.addConnectionListener(new ConnectionAdapter() {

                    @Override
                    public void opened(ConnectionEvent e) {
                        try {
                            msg = folder.getMessage(message);
                            parser.getCallbackManager()
                                    .getCallbackType(ChannelTopicListener.class)
                                    .call(EmailChannelInfo.this, true);

                            EmailChannelClientInfo cci = null;
                            for (Address addr : msg.getFrom()) {
                               cci = new EmailChannelClientInfo(
                                    new EmailClientInfo(parser,
                                    (InternetAddress) addr),
                                    EmailChannelInfo.this);
                               clients.add(cci);
                            }

                            for (Address addr : msg.getAllRecipients()) {
                               clients.add(new EmailChannelClientInfo(
                                    new EmailClientInfo(parser,
                                    (InternetAddress) addr),
                                    EmailChannelInfo.this));
                            }

                            parser.getCallbackManager()
                                    .getCallbackType(ChannelNamesListener.class)
                                    .call(EmailChannelInfo.this);
                            
                            for (String line : String.valueOf(msg.getContent()).split("\n")) {
                                System.out.println(line);
                                parser.getCallbackManager()
                                        .getCallbackType(ChannelMessageListener.class)
                                        .call(EmailChannelInfo.this, cci, line,
                                        msg.getFrom().toString());
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                folder.open(Folder.READ_ONLY);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return (message == -1 ? "#" : "&") + folder.getFullName()
                + (message == -1 ? "" : "/" + message);
    }

    @Override
    public void setTopic(String topic) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getTopic() {
        try {
            return msg == null ? getName() : (msg.getSubject() == null
                    ? "(no subject)" : msg.getSubject());
        } catch (Exception ex) {
            ex.printStackTrace();
            return "?";
        }
    }

    @Override
    public long getTopicTime() {
        try {
            return msg == null ? System.currentTimeMillis() / 1000
                    : msg.getSentDate().getTime() / 1000;
        } catch (Exception ex) {
            ex.printStackTrace();
            return System.currentTimeMillis() / 1000;
        }
    }

    @Override
    public String getTopicSetter() {
        return "who!the@hell.knows";
    }

    @Override
    public String getModes() {
        return "";
    }

    @Override
    public String getMode(char mode) {
        return "";
    }

    @Override
    public Collection<ChannelListModeItem> getListMode(char mode) {
        return new ArrayList<ChannelListModeItem>();
    }

    @Override
    public void sendMessage(String message) {
        // TODO?
    }

    @Override
    public void sendAction(String action) {
        // TODO?
    }

    @Override
    public void part(String reason) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendWho() {
        // TODO?
    }

    @Override
    public void alterMode(boolean add, Character mode, String parameter) {
        // Do nothing
    }

    @Override
    public void flushModes() {
        // Do nothing
    }

    @Override
    public ChannelClientInfo getChannelClient(ClientInfo client) {
        if (client.equals(parser.getLocalClient())) {
            return localinfo;
        }

        return null;
    }

    @Override
    public ChannelClientInfo getChannelClient(String client) {
        return getChannelClient(parser.getClient(client));
    }

    @Override
    public ChannelClientInfo getChannelClient(String client, boolean create) {
        return getChannelClient(parser.getClient(client));
    }

    @Override
    public Collection<ChannelClientInfo> getChannelClients() {
        return clients;
    }

    @Override
    public int getChannelClientCount() {
        return clients.size();
    }

    @Override
    public Parser getParser() {
        return parser;
    }

    @Override
    public void run() {
        try {
            for (Message cmessage : folder.getMessages(Math.max(folder.getMessageCount() - 20, 1),
                    folder.getMessageCount())) {
                parser.getCallbackManager().getCallbackType(ChannelMessageListener.class)
                        .call(this, new EmailChannelClientInfo(
                        new EmailClientInfo(parser, (InternetAddress) cmessage.getFrom()[0]),
                        this),
                        (cmessage.getFlags().contains(Flag.SEEN) ? "" : Styliser.CODE_BOLD)
                        + cmessage.getSubject() + Styliser.CODE_STOP + Styliser.CODE_COLOUR
                        + "15 &" + folder.getName() + "/" + cmessage.getMessageNumber(),
                        cmessage.getFrom()[0].toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
