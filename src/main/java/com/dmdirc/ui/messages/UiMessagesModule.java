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

package com.dmdirc.ui.messages;

import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.config.GlobalConfig;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.nio.file.Path;

import static com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType.BASE;

/**
 * Dagger module for message related objects.
 */
@SuppressWarnings("TypeMayBeWeakened")
@Module(library = true, complete = false)
public class UiMessagesModule {

    @Provides
    @Singleton
    public MultiEventFormatProvider getTemplateProvider(
            @Directory(BASE) final Path directory,
            @GlobalConfig final ColourManager colourManager,
            final DisplayLocationManager displayLocationManager) {
        final YamlEventFormatProvider yamlProvider =
                new YamlEventFormatProvider(directory.resolve("format.yml"), colourManager, displayLocationManager);
        yamlProvider.load();
        return new MultiEventFormatProvider(yamlProvider);
    }

    @Provides
    @Singleton
    public EventFormatProvider getTemplateProvider(final MultiEventFormatProvider provider) {
        return provider;
    }

}