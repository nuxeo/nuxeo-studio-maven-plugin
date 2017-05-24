package org.nuxeo.maven.mapper.impl;

import java.util.Arrays;
import java.util.List;

import org.nuxeo.ecm.core.schema.DocumentTypeDescriptor;
import org.nuxeo.ecm.core.schema.FacetDescriptor;
import org.nuxeo.ecm.core.schema.SchemaBindingDescriptor;
import org.nuxeo.maven.mapper.ExtensionMapper;

/**
 * Can contain: - doctype - facets
 */
public class DoctypeMapper extends ExtensionMapper {

    protected static final List<String> extensionPoints = Arrays.asList("doctype", "schema", "configuration");

    @Override
    public void registerDescriptors() {
        registerDescriptor("facets", FacetDescriptor.class);
        registerDescriptor("doctypes", DocumentTypeDescriptor.class);
        registerDescriptor("schemas", SchemaBindingDescriptor.class);
    }

    @Override
    public boolean accept(String target, String point) {
        return "org.nuxeo.ecm.core.schema.TypeService".equals(target) && extensionPoints.contains(point);
    }
}
