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

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.automation.OperationDocumentation;
import org.nuxeo.ecm.automation.core.OperationChainContribution;
import org.nuxeo.ecm.automation.core.OperationContribution;
import org.nuxeo.ecm.core.event.impl.EventListenerDescriptor;
import org.nuxeo.ecm.core.lifecycle.extensions.LifeCycleDescriptor;
import org.nuxeo.ecm.core.schema.DocumentTypeDescriptor;
import org.nuxeo.ecm.core.schema.FacetDescriptor;
import org.nuxeo.ecm.core.schema.SchemaBindingDescriptor;
import org.nuxeo.ecm.core.schema.types.SchemaImpl;
import org.nuxeo.ecm.core.security.PermissionDescriptor;
import org.nuxeo.maven.serializer.adapter.DefaultAdapter;
import org.nuxeo.maven.serializer.adapter.OperationAdapter;
import org.nuxeo.maven.serializer.adapter.OperationChainAdapter;
import org.nuxeo.maven.serializer.adapter.SchemaAdapter;
import org.nuxeo.maven.serializer.adapter.SerializerAdapter;
import org.nuxeo.maven.serializer.mixin.DocTypeMixin;
import org.nuxeo.maven.serializer.mixin.EventListenerMixin;
import org.nuxeo.maven.serializer.mixin.FacetMixin;
import org.nuxeo.maven.serializer.mixin.LifeCycleMixin;
import org.nuxeo.maven.serializer.mixin.OperationDocumentationMixin;
import org.nuxeo.maven.serializer.mixin.PermissionMixin;
import org.nuxeo.maven.serializer.mixin.SchemaMixin;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class JacksonConverter {
    public static final JacksonConverter instance = new JacksonConverter();

    protected Map<Class<?>, Class<?>> mixins = new HashMap<>();

    protected Map<Class<?>, SerializerAdapter> adapters = new HashMap<>();

    protected SerializerAdapter defaultAdapter = new DefaultAdapter();

    private JacksonConverter() {
        // Adapters aim to adapt descriptor to a more specific object
        registerAdapter(OperationContribution.class, OperationAdapter.class);
        registerAdapter(SchemaBindingDescriptor.class, SchemaAdapter.class);
        registerAdapter(OperationChainContribution.class, OperationChainAdapter.class);

        // Mixins allow to define the way the serialization is done
        registerMixin(FacetDescriptor.class, FacetMixin.class);
        registerMixin(PermissionDescriptor.class, PermissionMixin.class);
        registerMixin(OperationDocumentation.class, OperationDocumentationMixin.class);
        registerMixin(LifeCycleDescriptor.class, LifeCycleMixin.class);
        registerMixin(EventListenerDescriptor.class, EventListenerMixin.class);
        registerMixin(SchemaImpl.class, SchemaMixin.class);
        registerMixin(DocumentTypeDescriptor.class, DocTypeMixin.class);
    }

    protected void registerMixin(Class<?> target, Class<?> mixin) {
        mixins.put(target, mixin);
    }

    protected void registerAdapter(Class<?> target, Class<? extends SerializerAdapter> serializer) {
        try {
            adapters.put(target, serializer.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public String serialize(Object target) {
        try {
            ObjectMapper om = new ObjectMapper();
            Object targetAdapted = adapters.getOrDefault(target.getClass(), defaultAdapter).adapt(target);

            om.addMixIn(targetAdapted.getClass(), mixins.getOrDefault(targetAdapted.getClass(), Object.class));
            return om.writeValueAsString(targetAdapted);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void newGlobalStudioObject(OutputStream os, Map<String, String> serialized) {
        JsonFactory factory = new JsonFactory();

        try (JsonGenerator gen = factory.createGenerator(os, JsonEncoding.UTF8)) {
            gen.writeStartObject();
            for (String k : serialized.keySet()) {
                gen.writeFieldName(k);
                gen.writeRawValue(serialized.get(k));
            }
            gen.writeEndObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static abstract class StudioJacksonSerializer<T> extends StdSerializer<T> {
        /**
         * An empty constructor is required by Jackson
         */
        public StudioJacksonSerializer() {
            this(null);
        }

        protected StudioJacksonSerializer(Class<T> t) {
            super(t);
        }
    }
}
