/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.commandparser.commands.channel;

import com.dmdirc.Topic;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.BaseCommand;
import com.dmdirc.commandparser.commands.CommandOptions;
import com.dmdirc.commandparser.commands.ExternalCommand;
import com.dmdirc.commandparser.commands.context.ChannelCommandContext;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.events.ChannelGotTopicEvent;
import com.dmdirc.events.ChannelNoTopicEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.GroupChatUser;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.WindowModel;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Optional;

/**
 * The show topic command shows the user the current topic.
 */
@CommandOptions(allowOffline = false)
public class ShowTopic extends BaseCommand implements ExternalCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("topic",
            "topic - displays the current topic\ntopic <newtopic> - sets the channel topic",
            CommandType.TYPE_CHANNEL);

    /**
     * Creates a new instance of this command.
     *
     * @param controller The controller to use for command information.
     */
    @Inject
    public ShowTopic(final CommandController controller) {
        super(controller);
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        final GroupChat channel = ((ChannelCommandContext) context).getGroupChat();
        if (args.getArguments().length == 0) {
            final Optional<Topic> topic = channel.getCurrentTopic();
            if (topic.isPresent()) {
                final User user = topic
                        .flatMap(Topic::getClient)
                        .map(GroupChatUser::getUser)
                        .orElse(null);
                channel.getEventBus().publishAsync(
                        new ChannelGotTopicEvent(channel, topic.get(), user));
            } else {
                channel.getEventBus().publishAsync(
                        new ChannelNoTopicEvent(channel));
            }
        } else {
            channel.setTopic(args.getArgumentsAsString());
        }
    }

    @Override
    public void execute(final WindowModel origin, final Connection connection,
            final String channel, final boolean isSilent, final CommandArguments args) {
        if (args.getArguments().length == 0) {
            connection.getParser().get().sendRawMessage("TOPIC " + channel);
        } else {
            connection.getParser().get().sendRawMessage("TOPIC " + channel + " :" + args.
                    getArgumentsAsString());
        }
    }

}
