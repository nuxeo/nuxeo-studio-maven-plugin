package org.nuxeo.maven.serializer.adapter;

import org.nuxeo.ecm.automation.OperationDocumentation;
import org.nuxeo.ecm.automation.core.OperationContribution;
import org.nuxeo.ecm.automation.core.impl.OperationTypeImpl;

public class OperationAdapter implements SerializerAdapter<OperationContribution, OperationDocumentation> {

    @Override
    public OperationDocumentation adapt(OperationContribution src) {
        OperationTypeImpl operationType = new OperationTypeImpl(null, src.type, src.toString());
        return operationType.getDocumentation();
    }
}
