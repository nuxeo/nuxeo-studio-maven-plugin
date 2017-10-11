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
import org.nuxeo.ecm.core.schema.types.ComplexType;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.core.schema.types.ListType;
import org.nuxeo.ecm.core.schema.types.PrimitiveType;
import org.nuxeo.ecm.core.schema.types.Schema;
import org.nuxeo.ecm.core.schema.types.Type;
import org.nuxeo.extractor.serializer.JacksonConverter.StudioJacksonSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = SchemaMixin.SchemaSerializer.class)
public abstract class SchemaMixin {

    @JsonIgnore
    public Schema schema;

    /**
     * Following Serializer is copied/pasted from {@code org.nuxeo.ecm.core.io.marshallers.json.types.SchemaJsonWriter}
     * and adapted to use {@code com.fasterxml.jackson} instead of {@code org.codehaus.jackson}.
     */
    public static class SchemaSerializer extends StudioJacksonSerializer<Schema> {
        @Override
        public void serialize(Schema value, JsonGenerator gen, SerializerProvider provider) throws IOException {

            gen.writeFieldName(value.getName());
            gen.writeRaw(":");
            gen.writeStartObject();

            // Add prefix
            String prefix = value.getNamespace().prefix;
            if (StringUtils.isNotBlank(prefix)) {
                gen.writeStringField("@prefix", prefix);
            }

            for (Field field : value.getFields()) {
                writeField(gen, field);
            }
            gen.writeEndObject();
        }

        /**
         * Original Object headers; kept in case Studio format is following this new one.
         *
         * @param schema Schema Object to write
         * @param jg Destination JsonGenerator
         * @throws IOException When unable to write field
         */
        protected void writeSchemaObject(Schema schema, JsonGenerator jg) throws IOException {
            jg.writeStringField("name", schema.getName());
            String prefix = schema.getNamespace().prefix;
            if (StringUtils.isNotBlank(prefix)) {
                jg.writeStringField("prefix", prefix);
                // backward compat for old schema writers
                jg.writeStringField("@prefix", prefix);
            }
            jg.writeObjectFieldStart("fields");
            for (Field field : schema.getFields()) {
                writeField(jg, field);
            }
            jg.writeEndObject();
        }

        protected void writeField(JsonGenerator jg, Field field) throws IOException {
            if (!field.getType().isComplexType()) {
                if (field.getType().isListType()) {
                    ListType lt = (ListType) field.getType();
                    if (lt.getFieldType().isComplexType()) {
                        if (lt.getFieldType().getName().equals("content")) {
                            jg.writeStringField(field.getName().getLocalName(), "blob[]");
                        } else {
                            jg.writeObjectFieldStart(field.getName().getLocalName());
                            jg.writeStringField("type", "complex[]");
                            jg.writeObjectFieldStart("fields");
                            ComplexType cplXType = (ComplexType) lt.getField().getType();
                            for (Field subField : cplXType.getFields()) {
                                writeField(jg, subField);
                            }
                            jg.writeEndObject();
                            jg.writeEndObject();
                        }
                    } else {
                        doWriteField(jg, field);
                    }
                } else {
                    doWriteField(jg, field);
                }
            } else {
                if (field.getType().getName().equals("content")) {
                    jg.writeStringField(field.getName().getLocalName(), "blob");
                } else {
                    jg.writeObjectFieldStart(field.getName().getLocalName());
                    ComplexType cplXType = (ComplexType) field.getType();
                    jg.writeObjectFieldStart("fields");
                    for (Field subField : cplXType.getFields()) {
                        writeField(jg, subField);
                    }
                    jg.writeEndObject();
                    jg.writeStringField("type", "complex");
                    jg.writeEndObject();
                }
            }
        }

        protected void doWriteField(JsonGenerator jg, Field field) throws IOException {
            String typeValue;
            if (field.getType().isListType()) {
                ListType lt = (ListType) field.getType();
                Type type = lt.getFieldType();
                while (!(type instanceof PrimitiveType)) {
                    type = type.getSuperType();
                }
                typeValue = type.getName() + "[]";
            } else {
                Type type = field.getType();
                while (!(type instanceof PrimitiveType)) {
                    type = type.getSuperType();
                }
                typeValue = type.getName();
            }
            jg.writeStringField(field.getName().getLocalName(), typeValue);
        }
    }
}
