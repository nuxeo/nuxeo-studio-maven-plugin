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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.extractor.mapper.descriptors.DocumentTypeDescriptor;
import org.nuxeo.extractor.serializer.JacksonConverter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = DocTypeMixin.DocTypeSerializer.class)
public abstract class DocTypeMixin {
    public static class DocTypeSerializer extends JacksonConverter.StudioJacksonSerializer<DocumentTypeDescriptor> {
        @Override
        public void serialize(DocumentTypeDescriptor value, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            Map<String, Object> obj = new HashMap<>();
            if (StringUtils.isNotBlank(value.superTypeName)) {
                obj.put("parent", value.superTypeName);
            }
            obj.put("schemas", Arrays.stream(value.schemas).map(s -> s.name).collect(Collectors.toList()));
            obj.put("facets", value.facets);

            gen.writeFieldName(value.name);
            gen.writeRawValue(":");
            gen.writeObject(obj);
        }
    }
}
