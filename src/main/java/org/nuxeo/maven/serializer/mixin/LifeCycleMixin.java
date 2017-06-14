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

package org.nuxeo.maven.serializer.mixin;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.nuxeo.ecm.core.lifecycle.LifeCycleState;
import org.nuxeo.ecm.core.lifecycle.LifeCycleTransition;
import org.nuxeo.ecm.core.lifecycle.extensions.LifeCycleDescriptor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@JsonSerialize(using = LifeCycleMixin.LifeCycleSerializer.class)
public abstract class LifeCycleMixin {
    public static class LifeCycleSerializer extends StdSerializer<LifeCycleDescriptor> {

        public LifeCycleSerializer() {
            this(null);
        }

        protected LifeCycleSerializer(Class<LifeCycleDescriptor> t) {
            super(t);
        }

        @Override
        public void serialize(LifeCycleDescriptor value, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            List<String> states = value.getStates().stream().map(LifeCycleState::getName).collect(Collectors.toList());
            List<String> transitions = value.getTransitions()
                                            .stream()
                                            .map(LifeCycleTransition::getName)
                                            .collect(Collectors.toList());

            Map<String, List> lifecycle = new HashMap<>();
            lifecycle.put("states", states);
            lifecycle.put("transitions", transitions);

            gen.writeFieldName(value.getName());
            gen.writeRawValue(":");
            gen.writeObject(lifecycle);
        }
    }
}
