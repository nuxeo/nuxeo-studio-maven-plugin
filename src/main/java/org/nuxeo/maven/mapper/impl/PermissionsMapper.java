package org.nuxeo.maven.mapper.impl;

import org.nuxeo.ecm.core.security.PermissionDescriptor;
import org.nuxeo.maven.mapper.ExtensionMapper;

public class PermissionsMapper extends ExtensionMapper {

    @Override
    public void registerDescriptors() {
        registerDescriptor("permissions", PermissionDescriptor.class);
    }

    @Override
    protected boolean accept(String target, String point) {
        return "org.nuxeo.ecm.core.security.SecurityService".equals(target) && "permissions".equals(point);
    }
}
