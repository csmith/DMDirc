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

package com.dmdirc.ui.swing.dialogs.profiles;

import com.dmdirc.Main;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.swing.JWrappingLabel;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.StandardDialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

/** Profile editing dialog. */
public final class ProfileManagerDialog extends StandardDialog implements ActionListener,
        ListSelectionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Previously created instance of ProfileEditorDialog. */
    private static ProfileManagerDialog me;
    /** Profile list. */
    private List<Identity> profiles;
    /** Profile list. */
    private JList profileList;
    /** Profile list mode. */
    private ProfileListModel model;
    /** Profile detail panel. */
    private ProfileDetailPanel details;
    /** Info label. */
    private JWrappingLabel infoLabel;
    /** Add button. */
    private JButton addButton;
    /** Delete button. */
    private JButton deleteButton;
    /** Selected index. */
    private int selectedIndex;

    /** Creates a new instance of ProfileEditorDialog. */
    private ProfileManagerDialog() {
        super((MainFrame) Main.getUI().getMainWindow(), false);

        initComponents();

        layoutComponents();

        addListeners();

        if (model.getSize() > 0) {
            profileList.setSelectedIndex(0);
        } else {
            selectedIndex = -1;
        }
    }

    /** Creates the dialog if one doesn't exist, and displays it. */
    public static synchronized void showProfileManagerDialog() {
        me = getProfileManagerDialog();

        me.pack();
        me.setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
        me.setVisible(true);
        me.requestFocus();
    }

    /**
     * Returns the current instance of the ProfileManagerDialog.
     *
     * @return The current ProfileManagerDialog instance
     */
    public static synchronized ProfileManagerDialog getProfileManagerDialog() {
        if (me == null) {
            me = new ProfileManagerDialog();
        }

        return me;
    }

    /** Initialises the components. */
    private void initComponents() {
        setTitle("Profile Editor");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        orderButtons(new JButton(), new JButton());

        model = new ProfileListModel();
        profileList = new JList(model);
        details = new ProfileDetailPanel();
        addButton = new JButton("Add");
        deleteButton = new JButton("Delete");
        infoLabel =
                new JWrappingLabel("Profiles describe information needed to " +
                "connect to a server.  You can use a different profile for " +
                "each connection. Profiles are automatically saved when you " +
                "select another or click OK");

        profileList.setCellRenderer(new ProfileListCellRenderer());

        populateList();
    }

    /** Lays out the dialog. */
    private void layoutComponents() {
        getContentPane().setLayout(new MigLayout("fill"));

        infoLabel.setMaximumSize(new Dimension(600, 0));
        getContentPane().add(infoLabel, "wrap, growx, spanx 2");
        getContentPane().add(new JScrollPane(profileList), "growy, w 200");
        getContentPane().add(details, "grow, wrap");
        getContentPane().add(addButton, "wrap, w 200");
        getContentPane().add(deleteButton, "left, w 200");
        getContentPane().add(getLeftButton(), "split, right, sg button");
        getContentPane().add(getRightButton(), "right, sg button");
        
        pack();
    }

    /** Adds listeners to the components. */
    private void addListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);

        addButton.addActionListener(this);
        deleteButton.addActionListener(this);

        profileList.addListSelectionListener(this);
    }

    /** Populates the profile list. */
    public void populateList() {
        final String profileString = "profile";
        model.clear();
        profiles = IdentityManager.getProfiles();
        for (Identity profile : profiles) {
            model.add(new Profile(profile.getName(),
                    profile.getOption(profileString, "nickname"),
                    profile.getOption(profileString, "realname"),
                    profile.getOption(profileString, "ident"),
                    profile.getOptionList(profileString, "altnicks"),
                    false));
        }
    }

    /** Saves the profile list. */
    private void save() {
        if (details.validateDetails()) {
            details.save();
            final Iterator<Profile> it = model.iterator();

            while (it.hasNext()) {
                it.next().save();
            }

            dispose();
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource().equals(getOkButton())) {
            save();
        } else if (e.getSource().equals(getCancelButton())) {
            dispose();
        } else if (e.getSource().equals(addButton)) {
            final Profile profile = new Profile("Unnamed");
            model.add(profile);
            profileList.setSelectedIndex(model.indexOf(profile));
        } else if (e.getSource().equals(deleteButton)) {
            if (JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this profile?",
                    "Delete Confirmaton", JOptionPane.YES_NO_OPTION) ==
                    JOptionPane.YES_OPTION) {
                model.remove((Profile) profileList.getSelectedValue());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            if (!details.validateDetails()) {
                profileList.setSelectedIndex(selectedIndex);
            }
        }
        if (!e.getValueIsAdjusting()) {
            details.save();
            details.setProfile((Profile) profileList.getSelectedValue());
            if (profileList.getSelectedIndex() == -1) {
                deleteButton.setEnabled(false);
            } else {
                deleteButton.setEnabled(true);
            }
        }
        selectedIndex = profileList.getSelectedIndex();
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        synchronized (me) {
            super.dispose();
            me = null;
        }
    }
}
