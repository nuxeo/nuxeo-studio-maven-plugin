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
import java.util.List;

import org.junit.Test;
import org.nuxeo.ecm.automation.core.OperationContribution;
import org.nuxeo.ecm.core.event.impl.EventListenerDescriptor;
import org.nuxeo.ecm.core.lifecycle.extensions.LifeCycleDescriptor;
import org.nuxeo.ecm.core.schema.DocumentTypeDescriptor;
import org.nuxeo.ecm.core.schema.FacetDescriptor;
import org.nuxeo.ecm.core.schema.SchemaBindingDescriptor;
import org.nuxeo.ecm.core.security.PermissionDescriptor;
import org.nuxeo.maven.bundle.ContributionsHolder;
import org.nuxeo.maven.mapper.impl.TypeServiceMapper;
import org.nuxeo.maven.serializer.StudioSerializer;
import org.nuxeo.runtime.model.RegistrationInfo;

import net.sf.json.test.JSONAssert;

public class TestSerializer extends AbstractTest {

    public static final String EXPECTED_JSON_OPERATIONS = "[{\"id\":\"Document.MyOperation\",\"aliases\":[],\"signature\":[\"document\",\"document\"],\"category\":\"Others\",\"label\":\"Document.MyOperation\",\"requires\":null,\"since\":\"\",\"description\":\"\",\"params\":[{\"name\":\"dummy\",\"description\":\"\",\"type\":\"string\",\"widget\":null,\"values\":[],\"order\":0,\"required\":true}],\"widgetDefinitions\":null}]";

    public static final String EXPECTED_JSON_FACETS = "[{\"id\": \"Picture\", \"schemas\": [\"file\",\"picture\",\"image_metadata\"]}]";

    public static final String EXPECTED_JSON_PERMISSIONS = "{\"Browse\": \"Browse\", \"ReadProperties\": \"Read Properties\", \"ReadChildren\": \"Read Children\", \"ReadLifeCycle\": \"Read Life Cycle\", \"smallCamelCase\": \"Small Camel Case\"}";

    public static final String EXPECTED_JSON_LIFECYCLES = "{\"default\": {\"states\": [\"project\", \"approved\", \"obsolete\", \"deleted\"],\"transitions\": [\"approve\", \"obsolete\", \"delete\", \"undelete\", \"backToProject\"]}}";

    public static final String EXPECTED_JSON_EVENT = "{\"MyFirstEvent\": \"My First Event\", \"MySecondEvent\": \"My Second Event\"}";

    public static final String EXPECTED_JSON_SCHEMA = "{\"dublincore\":{\"@prefix\":\"dc\",\"description\":\"string\",\"created\":\"date\",\"coverage\":\"string\",\"title\":\"string\",\"complex\":{\"fields\":{\"mime-type\":\"string\",\"data\":\"binary\",\"name\":\"string\",\"length\":\"long\",\"digest\":\"string\",\"encoding\":\"string\"},\"type\":\"complex\"},\"modified\":\"date\",\"nature\":\"string\",\"lastContributor\":\"string\",\"content\":\"blob\",\"source\":\"string\",\"publisher\":\"string\"}}";

    public static final String EXPECTED_JSON_DOCTYPE = "{\"File\": {\"parent\":\"Document\",\"schemas\":[\"common\",\"file\",\"dublincore\",\"uid\"],\"facets\":[\"Downloadable\",\"Versionable\"]}}";

    @Test
    public void testDoctypeMapper() throws URISyntaxException {
        RegistrationInfo ri = getRegistrationInfo("component-contrib.xml");

        TypeServiceMapper dte = new TypeServiceMapper();
        assertTrue(Arrays.stream(ri.getExtensions()).anyMatch(dte::accept));
    }

    @Test
    public void testFacetSerializer() throws URISyntaxException {
        assertSerialization("component-contrib.xml", FacetDescriptor.class, 1, EXPECTED_JSON_FACETS);
    }

    @Test
    public void testPermissionSerializer() throws URISyntaxException {
        assertSerialization("permission-contrib.xml", PermissionDescriptor.class, 5, EXPECTED_JSON_PERMISSIONS);
    }

    @Test
    public void testOperationSerializer() throws URISyntaxException {
        assertSerialization("operation-contrib.xml", OperationContribution.class, 1, EXPECTED_JSON_OPERATIONS);
    }

    @Test
    public void testLifeCycleSerializer() throws URISyntaxException {
        assertSerialization("lifecycle-contrib.xml", LifeCycleDescriptor.class, 1, EXPECTED_JSON_LIFECYCLES);
    }

    @Test
    public void testSchemaSerializer() throws URISyntaxException {
        assertSerialization("schema-contrib.xml", SchemaBindingDescriptor.class, 1, EXPECTED_JSON_SCHEMA);
    }

    @Test
    public void testDocTypeSerializer() throws URISyntaxException {
        assertSerialization("doctype-contrib.xml", DocumentTypeDescriptor.class, 1, EXPECTED_JSON_DOCTYPE);
    }

    @Test
    public void testCoreEventSerializer() throws URISyntaxException {
        ContributionsHolder holder = loadComponent("events-contrib.xml");
        List<EventListenerDescriptor> contributions = holder.getContributions(EventListenerDescriptor.class);
        assertEquals(1, contributions.size());
        assertEquals(5, contributions.get(0).getEvents().size());

        StudioSerializer serializer = new StudioSerializer(holder);
        String result = serializer.serializeAll(EventListenerDescriptor.class);
        assertJsonEquals(EXPECTED_JSON_EVENT, result);
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
