package org.nuxeo.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.nuxeo.ecm.core.schema.DocumentTypeDescriptor;
import org.nuxeo.ecm.core.schema.SchemaBindingDescriptor;
import org.nuxeo.maven.bundle.ContributionsHolder;
import org.nuxeo.maven.mapper.MappersManager;
import org.nuxeo.maven.mapper.impl.DoctypeMapper;
import org.nuxeo.runtime.model.Extension;
import org.nuxeo.runtime.model.RegistrationInfo;

public class TestWalker extends AbstractTest {

    @Test
    public void readComponent() throws URISyntaxException {
        RegistrationInfo ri = getRegistrationInfo("component-contrib.xml");

        assertEquals("org.nuxeo.ecm.platform.picture.coreTypes", ri.getName().getName());
        assertEquals(3, ri.getExtensions().length);
        Extension extension = ri.getExtensions()[0];

        assertEquals("schema", extension.getExtensionPoint());
        assertEquals("org.nuxeo.ecm.core.schema.TypeService", extension.getTargetComponent().getName());
    }

    @Test
    public void testReadExtension() throws URISyntaxException {
        MappersManager manager = new MappersManager();
        manager.register(new DoctypeMapper());

        assertEquals(DocumentTypeDescriptor.class, manager.getDescriptor("doctypes"));

        RegistrationInfo ri = getRegistrationInfo("component-contrib.xml");
        assertTrue(Arrays.stream(ri.getExtensions()).anyMatch(manager::accept));
    }

    @Test
    public void testContributionsHolder() throws URISyntaxException {
        RegistrationInfo ri = getRegistrationInfo("component-contrib.xml");

        ContributionsHolder holder = new ContributionsHolder();
        holder.load(ri);

        List<SchemaBindingDescriptor> contributions = holder.getContributions(SchemaBindingDescriptor.class);
        assertEquals(2, contributions.size());
    }

    @Test
    public void walkerTest() throws IOException {
        assertNotNull(walker.getManifest());

        List<Path> components = walker.getComponents().collect(Collectors.toList());
        assertEquals(3, components.size());

        RegistrationInfo ri = walker.read(components.get(0));
        assertNotNull(ri);
    }
}
