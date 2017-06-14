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

import static org.nuxeo.maven.serializer.SerializerHelper.humanize;

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
            gen.writeObject(humanize(value.getName()));
        }
    }
}
