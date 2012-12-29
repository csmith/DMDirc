/*
 * Copyright (c) 2006-2012 DMDirc Developers
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
package com.dmdirc.commandparser.commands.channel;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.ChannelCommandContext;
import com.dmdirc.interfaces.CommandController;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class SetNickColourTest {

    private CommandController controller;
    private Channel channel;
    private SetNickColour command;

    @Before
    public void setUp() {
        controller = mock(CommandController.class);
        when(controller.getCommandChar()).thenReturn('/');
        channel = mock(Channel.class);
        command = new SetNickColour(controller);
    }

    @Test
    public void testUsageNoArgs() {
        final FrameContainer tiw = mock(FrameContainer.class);
        command.execute(tiw, new CommandArguments(controller, "/foo"),
                new ChannelCommandContext(null, SetNickColour.INFO, channel));

        verify(tiw).addLine(eq("commandUsage"), anyChar(), anyString(), anyString());
    }

    @Test
    public void testUsageNicklist() {
        final FrameContainer tiw = mock(FrameContainer.class);
        command.execute(tiw, new CommandArguments(controller, "/foo --nicklist"),
                new ChannelCommandContext(null, SetNickColour.INFO, channel));

        verify(tiw).addLine(eq("commandUsage"), anyChar(), anyString(), anyString());
    }

    @Test
    public void testUsageText() {
        final FrameContainer tiw = mock(FrameContainer.class);
        command.execute(tiw, new CommandArguments(controller, "/foo --text"),
                new ChannelCommandContext(null, SetNickColour.INFO, channel));

        verify(tiw).addLine(eq("commandUsage"), anyChar(), anyString(), anyString());
    }

}