/*
 * (C) Copyright 2017 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Arnaud Kervern
 */

package org.nuxeo.maven.runtime;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.nuxeo.runtime.RuntimeService;
import org.nuxeo.runtime.Version;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.ComponentManager;
import org.nuxeo.runtime.model.ComponentName;
import org.nuxeo.runtime.model.RuntimeContext;
import org.nuxeo.runtime.services.config.ConfigurationService;
import org.nuxeo.runtime.services.config.ConfigurationServiceImpl;
import org.osgi.framework.Bundle;

/**
 * Fake Runtime in order to expose, and be able to return,
 * {@code org.nuxeo.runtime.services.config.ConfigurationService}.
 * <p>
 * XXX It could be interesting to map some Maven parameters to ConfigurationService properties
 */
public class FakeRuntimeService implements RuntimeService {

    protected ConfigurationService configurationService = new ConfigurationServiceImpl();

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public boolean isShuttingDown() {
        return false;
    }

    @Override
    public File getHome() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Version getVersion() {
        return null;
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public void reloadProperties() throws IOException {

    }

    @Override
    public String getProperty(String name) {
        return configurationService.getProperty(name);
    }

    @Override
    public String getProperty(String name, String defaultValue) {
        return configurationService.getProperty(name, defaultValue);
    }

    @Override
    public String expandVars(String expression) {
        return null;
    }

    @Override
    public ComponentManager getComponentManager() {
        return null;
    }

    @Override
    public Object getComponent(String name) {
        return null;
    }

    @Override
    public Object getComponent(ComponentName name) {
        return null;
    }

    @Override
    public ComponentInstance getComponentInstance(String name) {
        return null;
    }

    @Override
    public ComponentInstance getComponentInstance(ComponentName name) {
        return null;
    }

    @Override
    public RuntimeContext getContext() {
        return null;
    }

    @Override
    public <T> T getService(Class<T> serviceClass) {
        if (serviceClass.isAssignableFrom(configurationService.getClass())) {
            return serviceClass.cast(configurationService);
        }
        return null;
    }

    @Override
    public List<String> getWarnings() {
        return null;
    }

    @Override
    public List<String> getErrors() {
        return null;
    }

    @Override
    public File getBundleFile(Bundle bundle) {
        return null;
    }

    @Override
    public Bundle getBundle(String symbolicName) {
        return null;
    }

    @Override
    public boolean getStatusMessage(StringBuilder msg) {
        return false;
    }

    @Override
    public void setProperty(String name, Object value) {

    }
}
