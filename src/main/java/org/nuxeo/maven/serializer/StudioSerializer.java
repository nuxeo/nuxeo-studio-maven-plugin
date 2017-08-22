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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.nuxeo.ecm.automation.core.OperationChainContribution;
import org.nuxeo.ecm.automation.core.OperationContribution;
import org.nuxeo.ecm.core.schema.FacetDescriptor;
import org.nuxeo.maven.bundle.ContributionsHolder;

public class StudioSerializer {

    private static final List<Class<?>> asArray = Arrays.asList(FacetDescriptor.class, OperationContribution.class,
            OperationChainContribution.class);

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
        return serializeAll(holder.getManager().getDescriptor(name));
    }

    public String serializeAll(Class<?> descriptor) {
        final String delimiter = getDelimiter();
        final String prefix = getPrefix(descriptor);
        final String suffix = getSuffix(descriptor);

        return holder.getContributions(descriptor)
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

    public String getPrefix(Class<?> descriptor) {
        return asArray.contains(descriptor) ? "[" : "{";
    }

    public String getSuffix(Class<?> descriptor) {
        return asArray.contains(descriptor) ? "]" : "}";
    }
}
