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

package org.nuxeo.operation;

import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;

@Operation(id = MyFullOperation.ID, category = "foo", label = "bar", requires = "something", since = "9.3", deprecatedSince = "9.10", addToStudio = false, aliases = {
        "alias1", "alias2" }, description = "description")
public class MyFullOperation {
    public static final String ID = "Document.FullOperation";

    @Param(name = "parameters", description = "description", order = 2, required = false, values = { "val1", "val2" })
    public String[] parameters;

    @OperationMethod
    public DocumentModelList runWithoutParam() {
        return null;
    }

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel runWithCollector(DocumentRef toto) {
        return null;
    }

}
