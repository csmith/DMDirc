/*
 * Copyright (c) 2006-2013 DMDirc Developers
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
import com.dmdirc.ui.Colour;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.messages.ColourManager;

import javax.inject.Inject;

/**
 * The notify command allows the user to set the notification colour for a
 * window.
 */
public class Notify extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("notify",
            "notify <colour> - sets the notification colour for this window",
            CommandType.TYPE_GLOBAL);

    /** Manager to use to convert colours. */
    private final ColourManager colourManager;

    /**
     * Creates a new instance of the {@link Notify} command.
     *
     * @param controller The controller to use for command information.
     * @param colourManager The colour manager to use to convert colours.
     */
    @Inject
    public Notify(final CommandController controller, final ColourManager colourManager) {
        super(controller);
        this.colourManager = colourManager;
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length == 0) {
            showUsage(origin, args.isSilent(), "notify", "<colour>");
            return;
        }

        final Colour colour = colourManager.getColourFromString(args.getArguments()[0], null);

        if (colour == null) {
            showUsage(origin, args.isSilent(), "notify",
                    "<colour> - colour must be an IRC colour code (0-15) or a "
                    + "hex string (e.g. FFFF00).");
        } else if (origin != null) {
            // There's not much point echoing an error if the origin isn't
            // valid, as errors go to the origin!
            origin.sendNotification(colour);
        }
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        return new AdditionalTabTargets().excludeAll();
    }

}
