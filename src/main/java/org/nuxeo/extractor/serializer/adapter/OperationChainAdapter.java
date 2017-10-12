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

package org.nuxeo.extractor.serializer.adapter;

import static org.nuxeo.extractor.serializer.adapter.automation.Constants.T_BLOB;
import static org.nuxeo.extractor.serializer.adapter.automation.Constants.T_BLOBS;
import static org.nuxeo.extractor.serializer.adapter.automation.Constants.T_DOCUMENT;
import static org.nuxeo.extractor.serializer.adapter.automation.Constants.T_DOCUMENTS;
import static org.nuxeo.extractor.serializer.adapter.automation.Constants.T_VOID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.extractor.mapper.descriptors.OperationChainDescriptor;
import org.nuxeo.extractor.serializer.adapter.automation.OperationDocumentation;

public class OperationChainAdapter implements SerializerAdapter<OperationChainDescriptor, OperationDocumentation> {
    /**
     * Not possible to get a correct Signature. Signature must be computed with first Operation consumer and last
     * Operation producer... but, there is no chance to have the the corresponding Operation in the class Path.
     */
    protected static final String[] DEFAULT_SIGNATURE;

    static {
        List<String> signatures = new ArrayList<>();

        List<String> possibleValues = Arrays.asList(T_DOCUMENTS, T_DOCUMENT, T_BLOB, T_BLOBS, T_VOID);
        possibleValues.forEach(inputType -> possibleValues.forEach(outputType -> {
            signatures.add(inputType);
            signatures.add(outputType);
        }));

        DEFAULT_SIGNATURE = signatures.toArray(new String[signatures.size()]);
    }

    @Override
    public OperationDocumentation adapt(OperationChainDescriptor src) {
        OperationDocumentation doc = new OperationDocumentation(src.getId());
        doc.label = src.getLabel();
        doc.requires = src.getRequires();
        doc.category = src.getCategory();
        doc.setAliases(src.getAliases());
        doc.since = src.getSince();
        doc.description = src.getDescription();
        doc.params = src.getParams();

        if (StringUtils.isEmpty(doc.label)) {
            doc.label = doc.id;
        }
        if (StringUtils.isEmpty(doc.requires)) {
            doc.requires = null;
        }

        OperationChainDescriptor.Operation[] ops = src.getOps();
        doc.operations = ops;

        if (ops.length != 0) {
            doc.signature = DEFAULT_SIGNATURE;
        } else {
            doc.signature = new String[] { T_VOID, T_VOID };
        }
        return doc;
    }
}
