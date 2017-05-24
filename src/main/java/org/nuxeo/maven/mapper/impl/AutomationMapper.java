package org.nuxeo.maven.mapper.impl;

import org.nuxeo.ecm.automation.core.OperationContribution;
import org.nuxeo.maven.mapper.ExtensionMapper;

public class AutomationMapper extends ExtensionMapper {

    @Override
    public void registerDescriptors() {
        registerDescriptor("operations", OperationContribution.class);
    }

    @Override
    protected boolean accept(String target, String point) {
        return target.equals("org.nuxeo.ecm.core.operation.OperationServiceComponent") && point.equals("operations");
    }
}
