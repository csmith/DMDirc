/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.addons.twitter;

import com.dmdirc.parser.common.MyInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.util.IrcAddress;

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
    public TwitterPlugin() {
    }


    @Override
    public void onLoad() {
        System.out.println("Twitter was loaded!");
    }

    @Override
    public void onUnload() {
        unloading = true;
        for (Twitter parser : Twitter.currentParsers) {
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
        System.out.println("Twitter was wanted!");
        return (unloading) ? null : new Twitter(myInfo, address);
    }
}
