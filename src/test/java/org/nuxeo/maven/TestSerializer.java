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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.junit.Test;
import org.nuxeo.ecm.automation.core.OperationContribution;
import org.nuxeo.ecm.core.schema.FacetDescriptor;
import org.nuxeo.ecm.core.security.PermissionDescriptor;
import org.nuxeo.maven.bundle.ContributionsHolder;
import org.nuxeo.maven.mapper.impl.DoctypeMapper;
import org.nuxeo.maven.serializer.StudioSerializer;
import org.nuxeo.runtime.model.RegistrationInfo;

import net.sf.json.test.JSONAssert;

public class TestSerializer extends AbstractTest {

    public static final String EXPECTED_JSON_OPERATIONS = "[{\"id\":\"Document.MyOperation\",\"aliases\":[],\"signature\":[\"document\",\"document\"],\"category\":\"Others\",\"label\":\"Document.MyOperation\",\"requires\":null,\"since\":\"\",\"description\":\"\",\"params\":[{\"name\":\"dummy\",\"description\":\"\",\"type\":\"string\",\"widget\":null,\"values\":[],\"order\":0,\"required\":true}],\"widgetDefinitions\":null}]";

    public static final String EXPECTED_JSON_FACETS = "[{\"id\": \"Picture\", \"schemas\": [\"file\",\"picture\",\"image_metadata\"]}]";

    public static final String EXPECTED_JSON_PERMISSIONS = "{\"Browse\": \"Browse\", \"ReadProperties\": \"ReadProperties\", \"ReadChildren\": \"ReadChildren\", \"ReadLifeCycle\": \"ReadLifeCycle\"}";

    @Test
    public void testDoctypeMapper() throws URISyntaxException {
        RegistrationInfo ri = getRegistrationInfo("component-contrib.xml");

        DoctypeMapper dte = new DoctypeMapper();
        assertTrue(Arrays.stream(ri.getExtensions()).anyMatch(dte::accept));
    }

    @Test
    public void testFacetSerializer() throws URISyntaxException {
        RegistrationInfo ri = getRegistrationInfo("component-contrib.xml");
        ContributionsHolder holder = new ContributionsHolder();
        holder.load(ri);
        assertEquals(1, holder.getContributions(FacetDescriptor.class).size());

        StudioSerializer serializer = new StudioSerializer(holder);
        String result = serializer.serializeAll(FacetDescriptor.class);

        JSONAssert.assertJsonEquals(EXPECTED_JSON_FACETS, result);
    }

    @Test
    public void testPermissionSerializer() throws URISyntaxException {
        RegistrationInfo ri = getRegistrationInfo("permission-contrib.xml");
        ContributionsHolder holder = new ContributionsHolder();
        holder.load(ri);
        assertEquals(4, holder.getContributions(PermissionDescriptor.class).size());

        StudioSerializer serializer = new StudioSerializer(holder);
        String result = serializer.serializeAll(PermissionDescriptor.class);

        JSONAssert.assertJsonEquals(EXPECTED_JSON_PERMISSIONS, result);
    }

    @Test
    public void testOperationSerializer() throws URISyntaxException {
        RegistrationInfo ri = getRegistrationInfo("operation-contrib.xml");
        ContributionsHolder holder = new ContributionsHolder();
        holder.load(ri);
        assertEquals(1, holder.getContributions(OperationContribution.class).size());

        StudioSerializer serializer = new StudioSerializer(holder);
        String result = serializer.serializeAll(OperationContribution.class);
        assertEquals(EXPECTED_JSON_OPERATIONS, result);
    }

    @Test
    public void studioSerialization() throws URISyntaxException, UnsupportedEncodingException {
        ContributionsHolder holder = new ContributionsHolder();
        holder.load(getRegistrationInfo("operation-contrib.xml"));
        holder.load(getRegistrationInfo("permission-contrib.xml"));
        holder.load(getRegistrationInfo("component-contrib.xml"));

        StudioSerializer serializer = new StudioSerializer(holder);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serializeInto(baos, "operations,permissions,facets".split(","));

        String result = baos.toString("UTF-8");
        String expected = String.format("{\"operations\": %s, \"facets\": %s, \"permissions\": %s}",
                EXPECTED_JSON_OPERATIONS, EXPECTED_JSON_FACETS, EXPECTED_JSON_PERMISSIONS);
        JSONAssert.assertJsonEquals(expected, result);
    }
}
