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

package uk.org.ownage.dmdirc.commandparser.commands.server;

import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.commandparser.CommandManager;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.commandparser.ServerCommand;

/**
 * Allows the user to send notices.
 * @author chris
 */
public final class Notice extends ServerCommand {
    
    /**
     * Creates a new instance of Notice.
     */
    public Notice() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /**
     * Executes this command.
     * @param origin The frame in which this command was issued
     * @param server The server object that this command is associated with
     * @param args The user supplied arguments
     */
    public void execute(final CommandWindow origin, final Server server,
            final String... args) {
        if (args.length < 2) {
            origin.addLine("commandUsage", Config.getCommandChar(), "notice",
                    "<target> <message>");
        } else {
            server.getParser().sendLine("NOTICE " + args[0] + " :"
                    + implodeArgs(1, args));
            origin.addLine("selfNotice", args[0], implodeArgs(1, args));
        }
    }
    
    
    /** {@inheritDoc}. */
    public String getName() {
        return "notice";
    }
    
    /** {@inheritDoc}. */
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc}. */
    public boolean isPolyadic() {
        return true;
    }
    
    /** {@inheritDoc}. */
    public int getArity() {
        return 0;
    }
    
    /** {@inheritDoc}. */
    public String getHelp() {
        return "notice <target> <message> - sends a notice";
    }
    
}
