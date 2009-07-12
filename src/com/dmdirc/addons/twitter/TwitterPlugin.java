/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.addons.twitter;

import com.dmdirc.plugins.Plugin;

/**
 * Plugin object for the twitter plugin.
 *
 * @author shane
 */
public class TwitterPlugin extends Plugin {

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        //TODO: Tell the core we are a twitter:// parser!
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        //TODO: Tell the core we are no longer a twitter:// parser!
        //Close all open parsers somehow.
    }

}
