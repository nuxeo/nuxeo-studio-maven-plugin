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
import org.nuxeo.maven.mapper.impl.TypeServiceMapper;
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
        manager.add(new TypeServiceMapper());

        List<Class<?>> descriptors = manager.getDescriptors("doctypes");
        assertEquals(1, descriptors.size());
        assertEquals(DocumentTypeDescriptor.class, descriptors.get(0));

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
