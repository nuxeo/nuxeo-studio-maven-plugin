package org.nuxeo.maven.serializer.mixin;

import java.io.IOException;

import org.nuxeo.ecm.core.security.PermissionDescriptor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@JsonSerialize(using = PermissionMixin.PermissionSerializer.class)
public abstract class PermissionMixin {

    public static class PermissionSerializer extends StdSerializer<PermissionDescriptor> {
        protected PermissionSerializer() {
            this(null);
        }

        protected PermissionSerializer(Class<PermissionDescriptor> t) {
            super(t);
        }

        @Override
        public void serialize(PermissionDescriptor value, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            gen.writeFieldName(value.getName());
            gen.writeRawValue(":");
            gen.writeObject(value.getName());
        }
    }
}
