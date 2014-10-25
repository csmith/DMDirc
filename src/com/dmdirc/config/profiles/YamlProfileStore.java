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

import com.google.common.base.Splitter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import static com.dmdirc.util.YamlReaderUtils.asList;
import static com.dmdirc.util.YamlReaderUtils.asMap;
import static com.dmdirc.util.YamlReaderUtils.optionalString;
import static com.dmdirc.util.YamlReaderUtils.requiredString;

/**
 * Store that reads and writes profiles from a YAML file on disk.
 */
public class YamlProfileStore implements ProfileStore {

    private static final Logger LOG = LoggerFactory.getLogger(YamlProfileStore.class);
    /** The charset to use when reading and writing files. */
    private static final String CHARSET = "UTF-8";

    /** The path to the file to read and write auto commands in. */
    private final Path path;

    /**
     * Creates a new YAML profiles store.
     *
     * @param path The path to the YAML file to read and write profiles in.
     */
    public YamlProfileStore(final Path path) {
        this.path = path;
    }

    @Override
    public Set<Profile> readProfiles() {
        final Set<Profile> commands = new HashSet<>();
        if (Files.exists(path)) {
            try (final InputStream stream = Files.newInputStream(path);
                    final InputStreamReader reader = new InputStreamReader(stream, CHARSET)) {
                final YamlReader yamlReader = new YamlReader(reader);
                final Object root = yamlReader.read();
                commands.addAll(readProfilesFromYaml(asList(root)));
                yamlReader.close();
            } catch (IOException | IllegalArgumentException ex) {
                LOG.warn("Unable to read auto commands", ex);
            }
        }

        return commands;
    }

    @Override
    public void writeProfiles(final Set<Profile> profiles) {
        final List<Object> list = getProfilesForYaml(profiles);
        try (final OutputStream stream = Files.newOutputStream(path);
                final OutputStreamWriter writer = new OutputStreamWriter(stream, CHARSET)) {
            final YamlWriter yamlWriter = new YamlWriter(writer);
            yamlWriter.write(list);
            yamlWriter.close();
        } catch (IOException ex) {
            LOG.error("Unable to write auto commands", ex);
        }
    }

    /**
     * Builds a set of auto commands from the given YAML representations.
     *
     * @param objects The raw objects read from the YAML file.
     *
     * @return A list of corresponding auto commands.
     */
    private Collection<Profile> readProfilesFromYaml(final Collection<Object> objects) {
        final Collection<Profile> res = new ArrayList<>(objects.size());
        for (Object profile : objects) {
            try {
                final Map<Object, Object> map = asMap(profile);
                final String name = requiredString(map, "name");
                final String realname = requiredString(map, "realname");
                final Optional<String> ident =
                        Optional.ofNullable(optionalString(map, "ident"));
                final List<String> nicknames =
                        Splitter.on('\n').splitToList(requiredString(map, "nicknames"));
                res.add(new Profile(name, realname, ident, nicknames));
            } catch (IllegalArgumentException ex) {
                LOG.info("Unable to read alias", ex);
            }
        }
        return res;
    }

    /**
     * Gets a set of maps that can be written to a YAML file.
     *
     * @param profiles The profiles to be stored.
     *
     * @return A list of corresponding maps.
     */
    private List<Object> getProfilesForYaml(final Collection<Profile> profiles) {
        final List<Object> res = new ArrayList<>(profiles.size());
        for (Profile profile : profiles) {
            final Map<Object, Object> map = new HashMap<>();
            map.put("name", profile.getName());
            map.put("realname", profile.getRealname());
            if (profile.getIdent().isPresent()) {
                map.put("ident", profile.getIdent().get());
            }
            map.put("nicknames", profile.getNicknames());
            res.add(map);
        }
        return res;
    }
}
