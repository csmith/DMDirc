/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package uk.org.ownage.dmdirc.ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.commandparser.ChannelCommandParser;
import uk.org.ownage.dmdirc.parser.ChannelClientInfo;
import uk.org.ownage.dmdirc.ui.components.Frame;
import uk.org.ownage.dmdirc.ui.input.InputHandler;
import uk.org.ownage.dmdirc.ui.messages.ColourManager;

/**
 * The channel frame is the GUI component that represents a channel to the user.
 * @author  chris
 */
public class ChannelFrame extends Frame {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    
    /**
     * The nick list model used for this channel's nickname list.
     */
    private NicklistListModel nicklistModel;

    /**
     * This channel's command parser.
     */
    private ChannelCommandParser commandParser;
    
    /** Nick list. */
    private JList nickList;
    
    /** scrollpane. */
    private JScrollPane nickScrollPane;
    
    /** split pane. */
    private JSplitPane splitPane;
    
    /**
     * Creates a new instance of ChannelFrame. Sets up callbacks and handlers,
     * and default options for the form.
     * @param owner The Channel object that owns this frame
     */
    public ChannelFrame(final Channel owner) {
        super(owner);
        
        initComponents();
        
        nickList.setBackground(ColourManager.getColour(
                Integer.parseInt(Config.getOption("ui", "backgroundcolour"))));
        nickList.setForeground(ColourManager.getColour(
                Integer.parseInt(Config.getOption("ui", "foregroundcolour"))));
        
        commandParser = new ChannelCommandParser(((Channel) getFrameParent()).
                getServer(), (Channel) getFrameParent());
        
        setInputHandler(new InputHandler(getInputField(), commandParser, this));
    }
    
    /**
     * Updates the list of clients on this channel.
     * @param newNames The new list of clients
     */
    public final void updateNames(final ArrayList<ChannelClientInfo> newNames) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nicklistModel.replace(newNames);
            }
        });
    }
    
    /**
     * Has the nick list update, to take into account mode changes.
     */
    public final void updateNames() {
        nicklistModel.sort();
    }
    
    /**
     * Adds a client to this channels' nicklist.
     * @param newName the new client to be added
     */
    public final void addName(final ChannelClientInfo newName) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nicklistModel.add(newName);
            }
        });
    }
    
    /**
     * Removes a client from this channels' nicklist.
     * @param name the client to be deleted
     */
    public final void removeName(final ChannelClientInfo name) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nicklistModel.remove(name);
            }
        });
    }
    
    /**
     * Initialises the compoents in this frame.
     */
    private void initComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        scrollPane = new JScrollPane();
        textPane = new JTextPane();
        inputField = new JTextField();
        nickScrollPane = new JScrollPane();
        nickList = new JList();
        
        splitPane.setBorder(null);
        final BasicSplitPaneDivider divider = 
                ((BasicSplitPaneUI) splitPane.getUI()).getDivider();
        if (divider != null) {
            divider.setBorder(null);
        }
        
        scrollPane.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        textPane.setEditable(false);
        scrollPane.setViewportView(textPane);
        
        nicklistModel = new NicklistListModel();
        
        nickList.setFont(new Font("Dialog", 0, 12));
        nickList.setModel(nicklistModel);
        nickScrollPane.setViewportView(nickList);
        
        getContentPane().setLayout(new GridBagLayout());
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 5, 5);
        getContentPane().add(splitPane, constraints);
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 1;
        getContentPane().add(inputField, constraints);
        
        splitPane.setLeftComponent(scrollPane);
        splitPane.setRightComponent(nickScrollPane);
        
        splitPane.setDividerLocation(465);
        splitPane.setResizeWeight(1);
        splitPane.setDividerSize(5);
        
        pack();
    }
    
}
