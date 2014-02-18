/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.input.AdditionalTabTargets;

import javax.inject.Inject;

/**
 * Allows the user to unload a plugin.
 */
public class UnloadPlugin extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("unloadplugin",
            "unloadplugin <plugin> - Unloads the specified plugin",
            CommandType.TYPE_GLOBAL);
    /** The plugin manager to use to unload plugins. */
    private final PluginManager pluginManager;

    /**
     * Creates a new instance of the {@link UnloadPlugin} command.
     *
     * @param controller    The controller to use for command information.
     * @param pluginManager The plugin manager to unload plugins with.
     */
    @Inject
    public UnloadPlugin(final CommandController controller, final PluginManager pluginManager) {
        super(controller);
        this.pluginManager = pluginManager;
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length == 0) {
            showUsage(origin, args.isSilent(), "unloadplugin", "<plugin>");
            return;
        }

        final PluginInfo plugin = pluginManager.getPluginInfoByName(args.getArguments()[0]);
        if (plugin == null) {
            sendLine(origin, args.isSilent(), FORMAT_ERROR,
                    "Plugin unloading failed - Plugin not loaded");
        } else if (pluginManager.delPlugin(plugin.getMetaData().getRelativeFilename())) {
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Plugin Unloaded.");
            pluginManager.updateAutoLoad(plugin);
        } else {
            sendLine(origin, args.isSilent(), FORMAT_ERROR, "Plugin Unloading failed");
        }
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets().excludeAll();

        if (arg == 0) {
            for (PluginInfo possPlugin : pluginManager.getPluginInfos()) {
                if (possPlugin.isLoaded()) {
                    res.add(possPlugin.getMetaData().getName());
                }
            }
        }

        return res;
    }

}
