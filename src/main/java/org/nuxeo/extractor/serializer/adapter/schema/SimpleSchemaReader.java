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

package org.nuxeo.extractor.serializer.adapter.schema;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.nuxeo.extractor.runtime.ExtractorContext;

public class SimpleSchemaReader {
    protected Map<String, Field> types = new HashMap<>();

    protected List<Field> fields = new ArrayList<>();

    protected Document doc;

    public SimpleSchemaReader(String src) {
        this(ExtractorContext.instance.getResource(src));
    }

    public SimpleSchemaReader(URL url) {
        assert url != null;

        SAXReader saxReader = new SAXReader();
        try {
            doc = saxReader.read(url);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        loadExternalTypes();
        loadInternalTypes();
    }

    public Map<String, Field> getTypeFields() {
        return types;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void load() {
        doc.selectNodes("/xs:schema/xs:element").forEach(n -> {
            fields.add(fieldFromNode(n));
        });
    }

    protected void loadExternalTypes() {
        doc.selectNodes("/xs:schema/xs:include").forEach(n -> {
            String location = n.valueOf("@schemaLocation");
            // types.putAll(new SimpleSchemaReader(location).getTypeFields());
        });
    }

    protected void loadInternalTypes() {
        doc.selectNodes("/xs:schema/xs:complexType").forEach(n -> {
            ComplexField field = new ComplexField(n.valueOf("@name"), false);
            n.selectNodes("./xs:sequence/xs:element").forEach(e -> field.addField(fieldFromNode(e)));
            registerType(field);
        });

        doc.selectNodes("/xs:schema/xs:simpleType").forEach(n -> registerType(createFromSimpleType(n)));
    }

    protected Field fieldFromNode(Node node) {
        String type = node.valueOf("@type");
        if (StringUtils.isEmpty(type)) {
            // List or Restriction
            return fieldFromParent(node);
        } else {
            return createField(node.valueOf("@name"), type, false);
        }
    }

    protected Field fieldFromParent(Node parent) {
        Node node = parent.selectSingleNode("./xs:complexType/xs:sequence/xs:element");
        String fieldName = parent.valueOf("@name");

        if (node != null) {
            return createField(fieldName, node.valueOf("@type"), true);
        }

        node = parent.selectSingleNode("./xs:simpleType");
        if (node != null) {
            return createFromSimpleType(node).copy(fieldName, false);
        }

        throw new IllegalStateException("Unable to build field from node: " + parent.getPath() + " " + fieldName);
    }

    protected Field createFromSimpleType(Node simpleType) {
        assert simpleType.getName().equalsIgnoreCase("simpleType");

        String fieldName = simpleType.valueOf("@name");
        Node node = simpleType.selectSingleNode("./xs:restriction");

        if (node != null) {
            return createField(fieldName, node.valueOf("@base"), false);
        }

        node = simpleType.selectSingleNode("./xs:list");
        if (node != null) {
            String type = node.valueOf("@itemType");
            if (StringUtils.isNotEmpty(type)) {
                return createField(fieldName, type, true);
            }

            node = node.selectSingleNode("./xs:simpleType");
            if (node != null) {
                Field f = createFromSimpleType(node);
                return f.copy(fieldName, true);
            }
        }

        throw new IllegalStateException(
                "Unable to build field from simpleType: " + simpleType.getPath() + " " + fieldName);
    }

    protected void registerType(Field field) {
        types.put(field.getName(), field);
    }

    protected Field createField(String name, String type, boolean isArray) {
        String[] split = type.split(":");
        type = split.length > 1 ? split[1] : split[0];

        if (types.containsKey(type)) {
            return types.get(type).copy(name, isArray);
        } else {
            return new SimpleField(name, type, isArray);
        }
    }
}
