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

package org.nuxeo.maven.serializer;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.nuxeo.maven.bundle.ContributionsHolder;

public class StudioSerializer {

    private static final List<String> asArray = Arrays.asList("facets", "operations");

    private ContributionsHolder holder;

    public StudioSerializer(ContributionsHolder holder) {
        this.holder = holder;
    }

    public void serializeInto(OutputStream os, String[] targets) {
        Map<String, String> serialized = new HashMap<>();
        Arrays.stream(targets).forEach(t -> serialized.put(t, this.serializeAll(t)));

        JacksonConverter.instance.newGlobalStudioObject(os, serialized);
    }

    public String serializeAll(String name) {
        return serializeAll(name, holder.getManager().getDescriptor(name));
    }

    public String serializeAll(Class<?> descriptor) {
        String descriptorName = holder.getManager().getDescriptorName(descriptor);
        return serializeAll(descriptorName, Collections.singletonList(descriptor));
    }

    public String serializeAll(String name, List<Class<?>> descriptor) {
        final String delimiter = getDelimiter();
        final String prefix = getPrefix(name);
        final String suffix = getSuffix(name);

        return descriptor.stream()
                         .map(holder::getContributions)
                         .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll)
                         .stream()
                         .map(this::serialize)
                         .collect(Collectors.joining(delimiter, prefix, suffix));
    }

    protected String serialize(Object obj) {
        return JacksonConverter.instance.serialize(obj);
    }

    public String getDelimiter() {
        return ",";
    }

    public String getPrefix(String name) {
        return asArray.contains(name) ? "[" : "{";
    }

    public String getSuffix(String name) {
        return asArray.contains(name) ? "]" : "}";
    }
}
