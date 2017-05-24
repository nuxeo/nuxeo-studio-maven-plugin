package org.nuxeo.maven.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.automation.OperationDocumentation;
import org.nuxeo.ecm.automation.core.OperationContribution;
import org.nuxeo.ecm.core.schema.FacetDescriptor;
import org.nuxeo.ecm.core.security.PermissionDescriptor;
import org.nuxeo.maven.serializer.mixin.FacetMixin;
import org.nuxeo.maven.serializer.mixin.OperationDocumentationMixin;
import org.nuxeo.maven.serializer.mixin.PermissionMixin;
import org.nuxeo.maven.serializer.adapter.DefaultAdapter;
import org.nuxeo.maven.serializer.adapter.OperationAdapter;
import org.nuxeo.maven.serializer.adapter.SerializerAdapter;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonSerializer {
    public static final JacksonSerializer instance = new JacksonSerializer();

    protected Map<Class<?>, Class<?>> mixins = new HashMap<>();

    protected Map<Class<?>, SerializerAdapter> adapters = new HashMap<>();

    protected SerializerAdapter defaultAdapter = new DefaultAdapter();

    private JacksonSerializer() {
        registerAdapter(OperationContribution.class, new OperationAdapter());

        registerMixin(FacetDescriptor.class, FacetMixin.class);
        registerMixin(PermissionDescriptor.class, PermissionMixin.class);
        registerMixin(OperationDocumentation.class, OperationDocumentationMixin.class);
    }

    protected void registerMixin(Class<?> target, Class<?> mixin) {
        mixins.put(target, mixin);
    }

    protected void registerAdapter(Class<?> target, SerializerAdapter adapter) {
        adapters.put(target, adapter);
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
}
