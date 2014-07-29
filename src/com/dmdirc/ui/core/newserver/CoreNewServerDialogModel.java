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

package com.dmdirc.ui.core.newserver;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.ClientModule.UserConfig;
import com.dmdirc.ServerManager;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.ConfigProviderListener;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.ui.NewServerDialogModel;
import com.dmdirc.interfaces.ui.NewServerDialogModelListener;
import com.dmdirc.util.collections.ListenerList;
import com.dmdirc.util.validators.IntegerPortValidator;
import com.dmdirc.util.validators.ListNotEmptyValidator;
import com.dmdirc.util.validators.PermissiveValidator;
import com.dmdirc.util.validators.ServerNameValidator;
import com.dmdirc.util.validators.Validator;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of a new server dialog model.
 */
public class CoreNewServerDialogModel implements NewServerDialogModel, ConfigProviderListener {

    private final ListenerList listeners;
    private final AggregateConfigProvider globalConfig;
    private final ConfigProvider userConfig;
    private final ServerManager serverManager;
    private final IdentityController controller;
    private final List<ConfigProvider> profiles;
    private Optional<ConfigProvider> selectedProfile;
    private Optional<String> hostname;
    private Optional<Integer> port;
    private Optional<String> password;
    private boolean ssl;
    private boolean saveAsDefault;

    @Inject
    public CoreNewServerDialogModel(
            @GlobalConfig final AggregateConfigProvider globalConfig,
            @UserConfig final ConfigProvider userConfig,
            final IdentityController identityController,
            final ServerManager serverManager) {
        this.globalConfig = globalConfig;
        this.userConfig = userConfig;
        this.controller = identityController;
        this.serverManager = serverManager;
        listeners = new ListenerList();
        profiles = new ArrayList<>(5);
        selectedProfile = Optional.absent();
        hostname = Optional.absent();
        port = Optional.absent();
        password = Optional.absent();
        ssl = false;
        saveAsDefault = false;
    }

    @Override
    public void loadModel() {
        controller.registerIdentityListener("profile", this);
        for (ConfigProvider provider : controller.getProvidersByType("profile")) {
            profiles.add(provider);
        }
        hostname = Optional.fromNullable(globalConfig.getOption("newserver", "hostname"));
        port = Optional.fromNullable(globalConfig.getOptionInt("newserver", "port"));
        password = Optional.fromNullable(globalConfig.getOption("newserver", "password"));
        ssl = globalConfig.getOptionBool("newserver", "ssl");
        saveAsDefault = false;
        listeners.getCallable(NewServerDialogModelListener.class).serverDetailsChanged(hostname,
                port, password, ssl, saveAsDefault);
    }

    @Override
    public List<ConfigProvider> getProfileList() {
        return ImmutableList.copyOf(profiles);
    }

    @Override
    public Optional<ConfigProvider> getSelectedProfile() {
        return selectedProfile;
    }

    @Override
    public void setSelectedProfile(final Optional<ConfigProvider> selectedProfile) {
        checkNotNull(selectedProfile);
        final Optional<ConfigProvider> oldSelectedProfile = this.selectedProfile;
        this.selectedProfile = selectedProfile;
        listeners.getCallable(NewServerDialogModelListener.class).selectedProfileChanged(
                oldSelectedProfile, selectedProfile);
    }

    @Override
    public boolean isProfileListValid() {
        return !getProfileListValidator().validate(getProfileList()).isFailure();
    }

    @Override
    public Validator<List<ConfigProvider>> getProfileListValidator() {
        return new ListNotEmptyValidator<ConfigProvider>();
    }

    @Override
    public Optional<String> getHostname() {
        return hostname;
    }

    @Override
    public void setHostname(final Optional<String> hostname) {
        checkNotNull(hostname);
        this.hostname = hostname;
        listeners.getCallable(NewServerDialogModelListener.class).serverDetailsChanged(hostname,
                port, password, ssl, saveAsDefault);
    }

    @Override
    public boolean isHostnameValid() {
        return getHostname().isPresent() && !getHostnameValidator().validate(getHostname().get()).
                isFailure();
    }

    @Override
    public Validator<String> getHostnameValidator() {
        return new ServerNameValidator();
    }

    @Override
    public Optional<Integer> getPort() {
        return port;
    }

    @Override
    public void setPort(final Optional<Integer> port) {
        checkNotNull(port);
        this.port = port;
        listeners.getCallable(NewServerDialogModelListener.class).serverDetailsChanged(hostname,
                port, password, ssl, saveAsDefault);
    }

    @Override
    public boolean isPortValid() {
        return getPort().isPresent() && !getPortValidator().validate(getPort().get()).isFailure();
    }

    @Override
    public Validator<Integer> getPortValidator() {
        return new IntegerPortValidator();
    }

    @Override
    public Optional<String> getPassword() {
        return password;
    }

    @Override
    public void setPassword(final Optional<String> password) {
        checkNotNull(password);
        this.password = password;
        listeners.getCallable(NewServerDialogModelListener.class).serverDetailsChanged(hostname,
                port, password, ssl, saveAsDefault);
    }

    @Override
    public boolean isPasswordValid() {
        return !getPassword().isPresent() || !getPasswordValidator().validate(getPassword().get()).
                isFailure();
    }

    @Override
    public Validator<String> getPasswordValidator() {
        return new PermissiveValidator<>();
    }

    @Override
    public boolean getSSL() {
        return ssl;
    }

    @Override
    public void setSSL(final boolean ssl) {
        this.ssl = ssl;
        listeners.getCallable(NewServerDialogModelListener.class).serverDetailsChanged(hostname,
                port, password, ssl, saveAsDefault);
    }

    @Override
    public boolean getSaveAsDefault() {
        return saveAsDefault;
    }

    @Override
    public void setSaveAsDefault(final boolean saveAsDefault) {
        this.saveAsDefault = saveAsDefault;
        listeners.getCallable(NewServerDialogModelListener.class).serverDetailsChanged(hostname,
                port, password, ssl, saveAsDefault);
    }

    @Override
    public void save() {
        if (saveAsDefault) {
            userConfig.
                    setOption("newserver", "hostname", hostname.isPresent() ? hostname.get() : "");
            userConfig.setOption("newserver", "port", port.isPresent() ? port.get() : 6667);
            userConfig.setOption("newserver", "password",
                    password.isPresent() ? password.get() : "");
            userConfig.setOption("newserver", "ssl", ssl);
        }
        try {
            if (selectedProfile.isPresent()) {
                serverManager.connectToAddress(getServerURI(), selectedProfile.get());
            } else {
                serverManager.connectToAddress(getServerURI());
            }
        } catch (URISyntaxException ex) {
            //This is tested in isSaveAllowed, shouldn't happen here.
        }
    }

    @Override
    public boolean isSaveAllowed() {
        try {
            getServerURI();
        } catch (URISyntaxException ex) {
            return false;
        }
        return isHostnameValid() && isPortValid() && isPasswordValid() && isProfileListValid();
    }

    @Override
    public void addListener(final NewServerDialogModelListener listener) {
        checkNotNull(listener);
        listeners.add(NewServerDialogModelListener.class, listener);
    }

    @Override
    public void removeListener(final NewServerDialogModelListener listener) {
        checkNotNull(listener);
        listeners.remove(NewServerDialogModelListener.class, listener);
    }

    @Override
    public void configProviderAdded(final ConfigProvider configProvider) {
        checkNotNull(configProvider);
        profiles.add(configProvider);
        listeners.getCallable(NewServerDialogModelListener.class).profileListChanged(
                ImmutableList.copyOf(profiles));
    }

    @Override
    public void configProviderRemoved(final ConfigProvider configProvider) {
        checkNotNull(configProvider);
        if (Optional.fromNullable(configProvider).equals(selectedProfile)) {
            final Optional<ConfigProvider> oldSelectedProfile = selectedProfile;
            selectedProfile = Optional.absent();
            listeners.getCallable(NewServerDialogModelListener.class).selectedProfileChanged(
                    oldSelectedProfile, Optional.<ConfigProvider>absent());

        }
        profiles.remove(configProvider);
        listeners.getCallable(NewServerDialogModelListener.class).profileListChanged(
                ImmutableList.copyOf(profiles));
    }

    /**
     * Gets the URI for the details in the dialog.
     *
     * @return Returns the URI the details represent
     *
     * @throws URISyntaxException    If the resulting URI is invalid
     */
    private URI getServerURI() throws URISyntaxException {
        return new URI("irc" + (ssl ? "s" : ""),
                password.isPresent() ? password.get() : "",
                hostname.isPresent() ? hostname.get() : "",
                port.isPresent() ? port.get() : 6667,
                null, null, null);
    }

}