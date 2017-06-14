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

package org.nuxeo.maven.mapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.common.xmap.XMap;
import org.nuxeo.runtime.model.Extension;
import org.nuxeo.runtime.model.RuntimeContext;
import org.nuxeo.runtime.model.impl.XMapContext;

public abstract class ExtensionMapper {

    protected Map<String, Class<?>> descriptors = new HashMap<>();

    public ExtensionMapper() {
        registerDescriptors();
    }

    public abstract void registerDescriptors();

    protected abstract boolean accept(String target, String point);

    public void registerDescriptor(String name, Class<?> descriptor) {
        descriptors.put(name, descriptor);
    }

    public boolean accept(Extension ext) {
        return accept(ext.getTargetComponent().getName(), ext.getExtensionPoint());
    }

    /**
     * Method used to be overriden in order to detect a disabled contribution
     *
     * @param contribution 
     * @return false if the contribution is removing another one, true otherwise.
     */
    public boolean isEnabled(Object contribution) {
        return true;
    }

    /**
     * Method used to be overriden in order to detect a partial contribution
     *
     * @return true if the contribution is partial, false otherwise.
     */
    public boolean isPartial(Object contribution) {
        return false;
    }

    public Object[] loadAll(RuntimeContext ctx, Extension extension) {
        XMap xmap = new XMap();
        Arrays.stream(getDescriptors()).forEach(xmap::register);
        return xmap.loadAll(new XMapContext(ctx), extension.getElement());
    }

    public Class<?>[] getDescriptors() {
        return descriptors.values().toArray(new Class[0]);
    }

    public Class<?> getDescriptor(String name) {
        return descriptors.getOrDefault(name, null);
    }

    public boolean contains(Class<?> descriptor) {
        return descriptors.values().contains(descriptor);
    }
}
