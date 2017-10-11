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

package org.nuxeo.extractor.bundle;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.junit.Test;
import org.nuxeo.ecm.core.schema.DocumentTypeDescriptor;
import org.nuxeo.extractor.AbstractExtractorTest;
import org.nuxeo.extractor.mapper.MappersManager;
import org.nuxeo.extractor.mapper.impl.AutomationMapper;
import org.nuxeo.extractor.mapper.impl.TypeServiceMapper;

public class TestHolder extends AbstractExtractorTest {

    @Test
    public void testSkippedPartialContribution() throws URISyntaxException {
        final Class<DocumentTypeDescriptor> docTypeDesc = DocumentTypeDescriptor.class;

        RegistrationInfo ri = getRegistrationInfo("simple-doctype-contrib.xml");
        ContributionsHolder holder = new ContributionsHolder();
        holder.load(ri);

        assertEquals(2, holder.contributions.get(docTypeDesc.getName()).size());
        assertEquals(1, holder.getContributions(docTypeDesc).size());
    }

    @Test
    public void testMapperTargets() {
        MappersManager manager = new MappersManager();
        assertEquals(0, manager.getRegisteredTargets().length);

        // Add AutomationMapper
        manager.add(new AutomationMapper());
        String[] registeredTargets = manager.getRegisteredTargets();
        assertEquals(1, registeredTargets.length);
        assertEquals("operations", registeredTargets[0]);

        // Add TypeServiceMapper with 3 available targets
        manager.add(new TypeServiceMapper());
        assertEquals(4, manager.getRegisteredTargets().length);
    }
}
