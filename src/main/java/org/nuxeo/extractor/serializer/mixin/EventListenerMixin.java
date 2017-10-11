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

package org.nuxeo.extractor.serializer.mixin;

import static org.nuxeo.extractor.serializer.SerializerHelper.humanize;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.extractor.mapper.descriptors.EventListenerDescriptor;
import org.nuxeo.extractor.serializer.JacksonConverter.StudioJacksonSerializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = EventListenerMixin.EventSerializer.class)
public abstract class EventListenerMixin {
    public static class EventSerializer extends StudioJacksonSerializer<EventListenerDescriptor> {
        private static List<String> systemEvents;

        static {
            // Registering Common System events from Nuxeo classes in order to skip them during export if prensent on
            // Studio
            // TODO Temporary; we should use a big constant file with all core events...
            systemEvents = extractField(DocumentEventTypes.class);
            systemEvents.addAll(extractField(LifeCycleConstants.class));
        }

        private static List<String> extractField(Class<?> type) {
            return FieldUtils.getAllFieldsList(type)
                             .stream()
                             .filter(s -> Modifier.isStatic(s.getModifiers()))
                             .map(s -> {
                                 try {
                                     return s.get(null).toString();
                                 } catch (IllegalAccessException e) {
                                     return null;
                                 }
                             })
                             .collect(Collectors.toList());
        }

        @Override
        public void serialize(EventListenerDescriptor value, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            // TODO Hacky: as one contribution will be able to export several elements. It must have been handled
            // before; when reading contributions
            String events = value.getEvents()
                                 .stream()
                                 .filter(e -> !systemEvents.contains(e))
                                 .map(e -> String.format("\"%s\":\"%s\"", e, humanize(e)))
                                 .collect(Collectors.joining(","));

            gen.writeRaw(events);
        }
    }
}
