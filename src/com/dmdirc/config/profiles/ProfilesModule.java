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

package com.dmdirc.config.profiles;

import com.dmdirc.commandline.CommandLineOptionsModule;
import com.dmdirc.commandparser.auto.AutoCommandStore;
import com.dmdirc.commandparser.auto.YamlAutoCommandStore;
import com.dmdirc.interfaces.SystemLifecycleComponent;

import java.nio.file.Path;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for profiles.
 */
@Module(library = true, complete = false)
public class ProfilesModule {

    @Provides
    @Singleton
    public ProfileStore getProfileStore(
            @CommandLineOptionsModule.Directory(CommandLineOptionsModule.DirectoryType.BASE) final Path directory) {
        return new YamlProfileStore(directory.resolve("auto-commands.yml"));
    }

    @Provides(type = Provides.Type.SET)
    public SystemLifecycleComponent getLifecycleComponent(
            final ProfileManagerLifeCycleManager manager) {
        return manager;
    }
}
