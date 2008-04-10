/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.wrappers.Alias;
import com.dmdirc.actions.wrappers.AliasWrapper;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.List;

/**
 * The alias command allows users to create aliases on-the-fly.
 * 
 * @author chris
 */
public final class AliasCommand extends GlobalCommand implements IntelligentCommand {
    
    /**
     * Creates a new instance of Active.
     */
    public AliasCommand() {
        super();
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final boolean isSilent,
            final String... args) {
        if (args.length < 2) {
            showUsage(origin, isSilent, "alias", "[--remove] <name> [command]");
            return;
        } else if (args[0].equalsIgnoreCase("--remove")) {
            if (doRemove(args[1])) {
                sendLine(origin, isSilent, FORMAT_OUTPUT, "Alias '" + args[1] + "' removed.");
            } else {
                sendLine(origin, isSilent, FORMAT_ERROR, "Alias '" + args[1] + "' not found.");
            }
            
            return;
        }
        
        for (Action alias : AliasWrapper.getAliasWrapper()) {
            if (AliasWrapper.getCommandName(alias).substring(1).equalsIgnoreCase(args[0])) {
                sendLine(origin, isSilent, FORMAT_ERROR, "Alias '" + args[0] + "' already exists.");
                return;
            }
        }
        
        final Alias myAlias = new Alias(args[0]);
        myAlias.setResponse(new String[]{implodeArgs(1, args)});
        myAlias.createAction().save();
        
        sendLine(origin, isSilent, FORMAT_OUTPUT, "Alias '" + args[0] + "' created.");
    }
    
    /**
     * Removes the alias with the specified name.
     * 
     * @param name The name of the alias to remove
     * @return True if the alias was deleted, false otherwise
     */
    private boolean doRemove(final String name) {
        for (Action alias : AliasWrapper.getAliasWrapper()) {
            if (AliasWrapper.getCommandName(alias).substring(1).equalsIgnoreCase(name)) {
                alias.delete();
                ActionManager.unregisterAction(alias);
                
                return true;
            }
        }
        
        return false;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "alias";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "alias [--remove] <name> [command] - creates or removes the specified alias";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        
        if (arg == 0) {
            res.add("--remove");
            res.excludeAll();
        } else if (arg == 1 && previousArgs.get(0).equals("--remove")) {
            for (Action alias : AliasWrapper.getAliasWrapper()) {
                res.add(alias.getName());
            }   
        } else if (arg >= 1 && !previousArgs.get(0).equals("--remove")) {
            return TabCompleter.getIntelligentResults(arg, previousArgs, 1);
        }
        
        return res;
    } 
    
}
