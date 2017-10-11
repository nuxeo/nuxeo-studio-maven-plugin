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

package org.nuxeo.extractor.mapper.descriptors;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.ecm.core.schema.SchemaDescriptor;

/**
 * Document Type Descriptor.
 * <p>
 * Can be used to delay document type registration when not all prerequisites are met (e.g. supertype was not yet
 * registered).
 * <p>
 * In this case the descriptor containing all the information needed to register the document is put in a queue waiting
 * for the prerequisites to be met.
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
@XObject("doctype")
public class DocumentTypeDescriptor {

    @XNode("@name")
    public String name;

    @XNodeList(value = "schema", type = SchemaDescriptor[].class, componentType = SchemaDescriptor.class)
    public SchemaDescriptor[] schemas;

    @XNode("@extends")
    public String superTypeName;

    @XNodeList(value = "facet@name", type = String[].class, componentType = String.class)
    public String[] facets;

    @XNode("prefetch")
    public String prefetch;

    @XNode("@append")
    public boolean append = false;

    @XNodeList(value = "subtypes/type", type = String[].class, componentType = String.class)
    public String[] subtypes = new String[0];

    @XNodeList(value = "subtypes-forbidden/type", type = String[].class, componentType = String.class)
    public String[] forbiddenSubtypes = new String[0];

    public DocumentTypeDescriptor() {
    }

    public DocumentTypeDescriptor(String superTypeName, String name, SchemaDescriptor[] schemas, String[] facets) {
        this.name = name;
        this.superTypeName = superTypeName;
        this.schemas = schemas;
        this.facets = facets;
    }

    public DocumentTypeDescriptor(String superTypeName, String name, SchemaDescriptor[] schemas, String[] facets,
            String[] subtypes, String[] forbiddenSubtypes) {
        this(superTypeName, name, schemas, facets);
        this.subtypes = subtypes;
        this.forbiddenSubtypes = forbiddenSubtypes;
    }

    @Override
    public String toString() {
        return "DocType: " + name;
    }

    public DocumentTypeDescriptor clone() {
        DocumentTypeDescriptor clone = new DocumentTypeDescriptor();
        clone.name = name;
        clone.schemas = schemas;
        clone.superTypeName = superTypeName;
        clone.facets = facets;
        clone.prefetch = prefetch;
        clone.append = append;
        clone.subtypes = subtypes;
        clone.forbiddenSubtypes = forbiddenSubtypes;
        return clone;
    }
}
