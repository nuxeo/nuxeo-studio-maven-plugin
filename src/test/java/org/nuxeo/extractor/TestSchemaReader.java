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

package org.nuxeo.extractor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.nuxeo.extractor.serializer.adapter.schema.ComplexField;
import org.nuxeo.extractor.serializer.adapter.schema.Field;
import org.nuxeo.extractor.serializer.adapter.schema.SimpleField;
import org.nuxeo.extractor.serializer.adapter.schema.SimpleSchemaReader;

public class TestSchemaReader {

    protected SimpleSchemaReader readerFrom(String resource) {
        return new SimpleSchemaReader(TestHelper.getResource(resource));
    }

    protected Field findField(String name, Collection<Field> fields) {
        return fields.stream().filter(s -> name.equals(s.getName())).findFirst().orElse(null);
    }

    @Test
    public void shouldExtractComplexTypeWithSimpleType() {
        SimpleSchemaReader reader = readerFrom("schema/complextype-test.xsd");
        ComplexField custom = (ComplexField) reader.getTypeFields().get("custom");
        assertThat(custom).isNotNull();

        List<Field> fields = custom.getFields();
        assertThat(fields).hasSize(5);
        assertThat(fields).extracting("isArray").containsOnly(false);

        assertThat(findField("encoding", fields)).extracting("type").containsExactly("string");
        assertThat(findField("mime-type", fields)).extracting("type").containsExactly("string");
        assertThat(findField("name", fields)).extracting("type").containsExactly("string");
        assertThat(findField("length", fields)).extracting("type").containsExactly("long");
        assertThat(findField("digest", fields)).extracting("type").containsExactly("string");
    }

    @Test
    public void shouldExtractComplexTypeWithRestrictedType() {
        SimpleSchemaReader reader = readerFrom("schema/complextype-test.xsd");
        ComplexField custom = (ComplexField) reader.getTypeFields().get("withRestriction");
        assertThat(custom).isNotNull();

        List<Field> fields = custom.getFields();
        assertThat(fields).hasSize(1);
        assertThat(fields).extracting("isArray").containsOnly(false);

        assertThat(findField("coverage", custom.getFields())).extracting("type").containsExactly("string");
    }

    @Test
    public void shouldExtractComplexTypeWithSequencedType() {
        SimpleSchemaReader reader = readerFrom("schema/complextype-test.xsd");
        ComplexField custom = (ComplexField) reader.getTypeFields().get("withSequence");
        assertThat(custom).isNotNull();

        List<Field> fields = custom.getFields();
        assertThat(fields).hasSize(1);
        assertThat(fields).extracting("isArray").containsOnly(true);

        assertThat(findField("attachments", custom.getFields())).extracting("type").containsExactly("string");
    }

    @Test
    public void shouldResolveInternalType() {
        SimpleSchemaReader reader = readerFrom("schema/complextype-test.xsd");
        reader.load();

        Field elt = findField("customElement", reader.getFields());
        assertThat(elt).isNotNull();
        assertThat(elt.isComplex()).isTrue();
        assertThat(((ComplexField) elt).getFields()).hasSize(5);
    }

    @Test
    public void shouldRegisterSimpleTypes() {
        SimpleSchemaReader reader = readerFrom("schema/simpletype-test.xsd");
        Field field = reader.getTypeFields().get("stringArray");
        assertThat(field).isNotNull();
        assertThat(field.isComplex()).isFalse();
        assertThat(field.isArray()).isTrue();
        assertThat(((SimpleField) field).getType()).isEqualTo("string");

        field = reader.getTypeFields().get("subjectList");
        assertThat(field).isNotNull();
        assertThat(field.isComplex()).isFalse();
        assertThat(field.isArray()).isTrue();
        assertThat(((SimpleField) field).getType()).isEqualTo("string");

        field = reader.getTypeFields().get("clob");
        assertThat(field).isNotNull();
        assertThat(field.isComplex()).isFalse();
        assertThat(field.isArray()).isFalse();
        assertThat(((SimpleField) field).getType()).isEqualTo("string");
    }
}
