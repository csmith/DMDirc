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

package com.dmdirc.commandparser.commands.global;

import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.GlobalCommand;
import com.dmdirc.ui.InputWindow;
import com.dmdirc.ui.messages.Formatter;

/**
 * Allows the user to save the message formatter to a file.
 * @author chris
 */
public final class SaveFormatter extends GlobalCommand {
    
    /**
     * Creates a new instance of SaveFormatter.
     */
    public SaveFormatter() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    public void execute(final InputWindow origin, final boolean isSilent,
            final String... args) {
        if (Formatter.saveAs(args[0])) {
            sendLine(origin, isSilent, "commandOutput", "Formatter saved.");
        } else {
            sendLine(origin, isSilent, "commandError", "Formatter save failed.");
        }
    }
    
    
    /** {@inheritDoc}. */
    public String getName() {
        return "saveformatter";
    }
    
    /** {@inheritDoc}. */
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc}. */
    public boolean isPolyadic() {
        return false;
    }
    
    /** {@inheritDoc}. */
    public int getArity() {
        return 1;
    }
    
    /** {@inheritDoc}. */
    public String getHelp() {
        return "saveformatter <file> - saves the message formatter to a file";
    }
    
}
