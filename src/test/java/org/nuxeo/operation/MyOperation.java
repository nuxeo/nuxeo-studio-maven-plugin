package org.nuxeo.operation;

import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.DocumentModel;

@Operation(id = MyOperation.ID)
public class MyOperation {

    public static final String ID = "Document.MyOperation";

    @Param(name = "dummy")
    public String parameter;

    @OperationMethod
    public DocumentModel run(DocumentModel doc) {
        return doc;
    }
}
