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

import org.nuxeo.extractor.mapper.descriptors.OperationScriptingDescriptor;
import org.nuxeo.extractor.serializer.adapter.automation.OperationDocumentation;

public class OperationScriptingAdapter
        implements SerializerAdapter<OperationScriptingDescriptor, OperationDocumentation> {

    @Override
    public OperationDocumentation adapt(OperationScriptingDescriptor desc) {
        OperationDocumentation doc = new OperationDocumentation(desc.getId());
        doc.label = desc.getId();
        doc.category = desc.getCategory();
        doc.description = desc.getDescription();
        doc.params = desc.getParams();
        doc.signature = new String[] { desc.getInputType(), desc.getOutputType() };
        doc.aliases = desc.getAliases();
        return doc;
    }
}
