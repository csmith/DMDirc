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

package com.dmdirc.addons.redirectplugin;

import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.plugins.Plugin;
    

/**
 * The redirect plugin allows the suer to redirect the output of commands that
 * would normally echo their results locally to a channel or chat window instead.
 * 
 * @author chris
 */
public final class RedirectPlugin extends Plugin {
    
    private RedirectCommand command;
    
    /** Creates a new system tray plugin. */
    public RedirectPlugin() {
        super();
        
        command = new RedirectCommand();
    }
    
    /** {@inheritDoc} */
    public boolean onLoad() {
        return true;
    }
    
    /** {@inheritDoc} */
    public void onActivate() {
        CommandManager.registerCommand(command);
    }
    
    /** {@inheritDoc}. */
    public void onDeactivate() {
        CommandManager.unregisterCommand(command);
    }
    
    /** {@inheritDoc} */
    public String getVersion() {
        return "0.1";
    }
    
    /** {@inheritDoc} */
    public String getAuthor() {
        return "Chris <chris@dmdirc.com>";
    }
    
    /** {@inheritDoc} */
    public String getDescription() {
        return "Adds commands to redirect command output to channels";
    }
    
    /** {@inheritDoc}. */
    public String toString() {
        return "Redirect Plugin";
    }
}
