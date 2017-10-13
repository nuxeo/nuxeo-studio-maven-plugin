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

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.extractor.serializer.JacksonConverter.StudioJacksonSerializer;
import org.nuxeo.extractor.serializer.adapter.schema.ComplexField;
import org.nuxeo.extractor.serializer.adapter.schema.Field;
import org.nuxeo.extractor.serializer.adapter.schema.Schema;
import org.nuxeo.extractor.serializer.adapter.schema.SimpleField;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = SchemaMixin.SchemaSerializer.class)
public abstract class SchemaMixin {

    public static class SchemaSerializer extends StudioJacksonSerializer<Schema> {
        @Override
        public void serialize(Schema value, JsonGenerator gen, SerializerProvider provider) throws IOException {

            gen.writeFieldName(value.getName());
            gen.writeRaw(":");
            gen.writeStartObject();

            // Add prefix
            String prefix = value.getPrefix();
            if (StringUtils.isNotBlank(prefix)) {
                gen.writeStringField("@prefix", prefix);
            }

            for (Field field : value.getFields()) {
                writeField(gen, field);
            }
            gen.writeEndObject();
        }

        protected void writeField(JsonGenerator gen, Field field) throws IOException {
            if (field.isComplex()) {
                writeComplex(gen, (ComplexField) field);
            } else {
                writeSimple(gen, (SimpleField) field);
            }
        }

        protected void writeComplex(JsonGenerator gen, ComplexField field) throws IOException {

            gen.writeFieldName(field.getName());
            gen.writeStartObject();
            gen.writeObjectField("type", "complex" + (field.isArray() ? "[]" : ""));

            gen.writeFieldName("fields");
            gen.writeStartObject();
            for (Field f : field.getFields()) {
                writeField(gen, f);
            }
            gen.writeEndObject();
            gen.writeEndObject();
        }

        protected void writeSimple(JsonGenerator gen, SimpleField field) throws IOException {
            gen.writeObjectField(field.getName(), transformType(field.getTypeJson()));
        }

        protected String transformType(String type) {
            if (type.startsWith("content")) {
                return type.replace("content", "blob");
            }

            if (type.startsWith("base64Binary")) {
                return type.replace("base64Binary", "binary");
            }

            return type;
        }
    }
}
