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
import java.util.Arrays;

import org.nuxeo.ecm.core.schema.SchemaDescriptor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public abstract class FacetMixin {
    @JsonProperty(value = "id")
    String name;

    @JsonIgnore
    Boolean perDocumentQuery;

    @JsonSerialize(using = FacetSchemaSerializer.class)
    SchemaDescriptor[] schemas;

    public static class FacetSchemaSerializer extends StdSerializer<SchemaDescriptor[]> {
        /**
         * An empty constructor is required by Jackson
         */
        public FacetSchemaSerializer() {
            this(null);
        }

        protected FacetSchemaSerializer(Class<SchemaDescriptor[]> t) {
            super(t);
        }

        @Override
        public void serialize(SchemaDescriptor[] value, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            gen.writeStartArray();
            Arrays.stream(value).forEach(s -> {
                try {
                    gen.writeString(s.name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            gen.writeEndArray();
        }
    }
}
