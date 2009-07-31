/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.addons.parser_twitter;

import com.dmdirc.parser.common.MyInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.util.IrcAddress;
import java.util.ArrayList;

/**
 *
 * @author shane
 */
public class TwitterPlugin extends Plugin  {
    /** Are we currently unloading? */
    private static boolean unloading = false;

    /**
     * Create a TwitterPlugin
     */
    public TwitterPlugin() { }


    @Override
    public void onLoad() { }

    @Override
    public void onUnload() {
        unloading = true;
        for (Twitter parser : new ArrayList<Twitter>(Twitter.currentParsers)) {
            parser.disconnect("");
        }
    }

    /**
     * Get a Twitter parser instance.
     *
     * @param myInfo The client information to use
     * @param address The address of the server to connect to
     * @return An appropriately configured parser
     */
    public Parser getParser(final MyInfo myInfo, final IrcAddress address) {
        return (unloading) ? null : new Twitter(myInfo, address, this);
    }
}
