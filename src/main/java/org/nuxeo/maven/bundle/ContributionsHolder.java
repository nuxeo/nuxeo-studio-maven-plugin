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

package org.nuxeo.maven.bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.maven.mapper.MappersManager;
import org.nuxeo.maven.mapper.impl.AutomationMapper;
import org.nuxeo.maven.mapper.impl.DoctypeMapper;
import org.nuxeo.maven.mapper.impl.LifeCycleMapper;
import org.nuxeo.maven.mapper.impl.PermissionsMapper;
import org.nuxeo.runtime.model.RegistrationInfo;

/**
 * Contributions Holder is aim to map and track all visited contributions.
 * <p>
 * This Holder is based on a {@link MappersManager} to map all known contributions to his Descriptor object.
 * </p>
 */
public class ContributionsHolder {
    /**
     * Contains a map of all contributions per contributions descriptor class.
     */
    protected final Map<String, List<Object>> contributions = new HashMap<>();

    protected final MappersManager mapper;

    public ContributionsHolder() {
        mapper = new MappersManager();
        mapper.register(new DoctypeMapper());
        mapper.register(new PermissionsMapper());
        mapper.register(new AutomationMapper());
        mapper.register(new LifeCycleMapper());
    }

    public MappersManager getMapper() {
        return mapper;
    }

    public void load(RegistrationInfo ri) {
        Arrays.stream(ri.getExtensions()).map(mapper::load).forEach(a -> Arrays.stream(a).forEach(c -> {
            List<Object> sortedContributions = contributions.computeIfAbsent(c.getClass().getName(),
                    s -> new ArrayList<>());
            sortedContributions.add(c);
        }));
    }

    /**
     * Get all contributions based on this descriptor; filtering the ones that target to be deleted or partial.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getContributions(Class<T> descriptor) {
        List<T> list = new ArrayList<>();
        for (Object contribution : contributions.getOrDefault(descriptor.getName(), Collections.emptyList())) {
            if (mapper.isSerializable(descriptor, contribution)) {
                list.add((T) contribution);
            }
        }
        return list;
    }
}
