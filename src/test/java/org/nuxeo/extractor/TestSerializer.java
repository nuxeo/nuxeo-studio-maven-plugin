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

package org.nuxeo.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.nuxeo.ecm.automation.core.OperationChainContribution;
import org.nuxeo.ecm.automation.core.OperationContribution;
import org.nuxeo.ecm.core.schema.DocumentTypeDescriptor;
import org.nuxeo.ecm.core.schema.SchemaBindingDescriptor;
import org.nuxeo.extractor.bundle.BundleWalker;
import org.nuxeo.extractor.bundle.ContributionsHolder;
import org.nuxeo.extractor.bundle.RegistrationInfo;
import org.nuxeo.extractor.mapper.descriptors.EventListenerDescriptor;
import org.nuxeo.extractor.mapper.descriptors.FacetDescriptor;
import org.nuxeo.extractor.mapper.descriptors.LifeCycleDescriptor;
import org.nuxeo.extractor.mapper.descriptors.PermissionDescriptor;
import org.nuxeo.extractor.mapper.impl.TypeServiceMapper;
import org.nuxeo.extractor.runtime.ExtractorContext;
import org.nuxeo.extractor.serializer.StudioSerializer;
import org.xml.sax.SAXException;

import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.parser.XSOMParser;

import net.sf.json.test.JSONAssert;

public class TestSerializer extends AbstractExtractorTest {

    public static final String EXPECTED_JSON_OPERATIONS = "[{\"id\":\"Document.MyOperation\",\"aliases\":[],\"signature\":[\"document\",\"document\"],\"category\":\"Others\",\"label\":\"Document.MyOperation\",\"requires\":null,\"since\":\"\",\"description\":\"\",\"params\":[{\"name\":\"dummy\",\"description\":\"\",\"type\":\"string\",\"widget\":null,\"values\":[],\"order\":0,\"required\":true}],\"widgetDefinitions\":null}]";

    public static final String EXPECTED_JSON_FACETS = "[{\"id\": \"Picture\", \"schemas\": [\"file\",\"picture\",\"image_metadata\"]}]";

    public static final String EXPECTED_JSON_PERMISSIONS = "{\"Browse\": \"Browse\", \"ReadProperties\": \"Read Properties\", \"ReadChildren\": \"Read Children\", \"ReadLifeCycle\": \"Read Life Cycle\", \"smallCamelCase\": \"Small Camel Case\"}";

    public static final String EXPECTED_JSON_LIFECYCLES = "{\"default\": {\"states\": [\"project\", \"approved\", \"obsolete\", \"deleted\"],\"transitions\": [\"approve\", \"obsolete\", \"delete\", \"undelete\", \"backToProject\"]}}";

    public static final String EXPECTED_JSON_EVENT = "{\"MyFirstEvent\": \"My First Event\", \"MySecondEvent\": \"My Second Event\"}";

    public static final String EXPECTED_JSON_SCHEMA = "{\"dublincore\":{\"@prefix\":\"dc\",\"description\":\"string\",\"created\":\"date\",\"coverage\":\"string\",\"title\":\"string\",\"complex\":{\"fields\":{\"mime-type\":\"string\",\"data\":\"binary\",\"name\":\"string\",\"length\":\"long\",\"digest\":\"string\",\"encoding\":\"string\"},\"type\":\"complex\"},\"modified\":\"date\",\"nature\":\"string\",\"lastContributor\":\"string\",\"content\":\"blob\",\"source\":\"string\",\"publisher\":\"string\"}}";

    public static final String EXPECTED_JSON_DOCTYPE = "{\"File\": {\"parent\":\"Document\",\"schemas\":[\"common\",\"file\",\"dublincore\",\"uid\"],\"facets\":[\"Downloadable\",\"Versionable\"]}}";

    public static final String EXPECTED_JSON_CHAIN = "[{\"id\":\"HelloWorldOperationChain\",\"aliases\":[],\"signature\":[\"documents\",\"documents\",\"documents\",\"document\",\"documents\",\"blob\",\"documents\",\"bloblist\",\"documents\",\"void\",\"document\",\"documents\",\"document\",\"document\",\"document\",\"blob\",\"document\",\"bloblist\",\"document\",\"void\",\"blob\",\"documents\",\"blob\",\"document\",\"blob\",\"blob\",\"blob\",\"bloblist\",\"blob\",\"void\",\"bloblist\",\"documents\",\"bloblist\",\"document\",\"bloblist\",\"blob\",\"bloblist\",\"bloblist\",\"bloblist\",\"void\",\"void\",\"documents\",\"void\",\"document\",\"void\",\"blob\",\"void\",\"bloblist\",\"void\",\"void\"],\"category\":\"Chain\",\"label\":\"HelloWorldOperationChain\",\"requires\":null,\"since\":\"\",\"description\":null,\"params\":[],\"widgetDefinitions\":null},{\"id\":\"dummyMail\",\"aliases\":[],\"signature\":[\"documents\",\"documents\",\"documents\",\"document\",\"documents\",\"blob\",\"documents\",\"bloblist\",\"documents\",\"void\",\"document\",\"documents\",\"document\",\"document\",\"document\",\"blob\",\"document\",\"bloblist\",\"document\",\"void\",\"blob\",\"documents\",\"blob\",\"document\",\"blob\",\"blob\",\"blob\",\"bloblist\",\"blob\",\"void\",\"bloblist\",\"documents\",\"bloblist\",\"document\",\"bloblist\",\"blob\",\"bloblist\",\"bloblist\",\"bloblist\",\"void\",\"void\",\"documents\",\"void\",\"document\",\"void\",\"blob\",\"void\",\"bloblist\",\"void\",\"void\"],\"category\":\"Chain\",\"label\":\"dummyMail\",\"requires\":null,\"since\":\"\",\"description\":null,\"params\":[],\"widgetDefinitions\":null}]";

    @Test
    public void testDoctypeMapper() throws URISyntaxException {
        RegistrationInfo ri = getRegistrationInfo("component-contrib.xml");

        TypeServiceMapper dte = new TypeServiceMapper();
        assertTrue(ri.getExtensions().stream().anyMatch(dte::accept));
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
    public void testOperationChainSerializer() throws URISyntaxException {
        assertSerialization("chains-contrib.xml", OperationChainContribution.class, 2, EXPECTED_JSON_CHAIN);
    }

    @Test
    public void testMissingContributions() {
        ContributionsHolder holder = new ContributionsHolder();
        List<DocumentTypeDescriptor> contributions = holder.getContributions(DocumentTypeDescriptor.class);

        assertEquals(0, contributions.size());

        StudioSerializer serializer = new StudioSerializer(holder, opts);
        String s = serializer.serializeDescriptors(DocumentTypeDescriptor.class);

        assertEquals(null, s);

        ByteArrayOutputStream io = new ByteArrayOutputStream();
        serializer.serializeInto(io, new String[] { "chains", "docTypes" });

        assertEquals("{}", new String(io.toByteArray()));
    }

    @Test
    public void testCoreEventSerializer() throws URISyntaxException {
        ContributionsHolder holder = loadComponent("events-contrib.xml");
        List<EventListenerDescriptor> contributions = holder.getContributions(EventListenerDescriptor.class);
        assertEquals(1, contributions.size());
        assertEquals(5, contributions.get(0).getEvents().size());

        StudioSerializer serializer = new StudioSerializer(holder, opts);
        String result = serializer.serializeDescriptors(EventListenerDescriptor.class);
        assertJsonEquals(EXPECTED_JSON_EVENT, result);
    }

    @Test(expected = RuntimeException.class)
    public void testExceptionThrownWhenEmpty() throws URISyntaxException {
        // Enable fail on empty parameter
        opts.failOnEmpty = true;

        ContributionsHolder holder = new ContributionsHolder();
        holder.load(getRegistrationInfo("operation-contrib.xml"));
        holder.load(getRegistrationInfo("permission-contrib.xml"));
        holder.load(getRegistrationInfo("component-contrib.xml"));

        StudioSerializer serializer = new StudioSerializer(holder, opts);
        OutputStream os = new ByteArrayOutputStream();
        serializer.serializeInto(os, new String[] { "misssing", "empty", "events" });
    }

    @Test
    public void studioSerialization() throws URISyntaxException, UnsupportedEncodingException {
        ContributionsHolder holder = new ContributionsHolder();
        holder.load(getRegistrationInfo("operation-contrib.xml"));
        holder.load(getRegistrationInfo("permission-contrib.xml"));
        holder.load(getRegistrationInfo("component-contrib.xml"));

        StudioSerializer serializer = new StudioSerializer(holder, opts);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serializeInto(baos, "operations,permissions,facets".split(","));

        String result = baos.toString("UTF-8");
        String expected = String.format("{\"operations\": %s, \"facets\": %s, \"permissions\": %s}",
                EXPECTED_JSON_OPERATIONS, EXPECTED_JSON_FACETS, EXPECTED_JSON_PERMISSIONS);
        JSONAssert.assertJsonEquals(expected, result);
    }

    protected URI getJarURI() {
        URL resource = getClass().getClassLoader().getResource("test-project-core-1.0-SNAPSHOT.jar");
        assertNotNull(resource);
        return URI.create("jar:file:" + resource.getFile());
    }

    @Test
    public void jarFileSerialization() throws IOException {
        URI uri = getJarURI();
        ExtractorContext.instance.addExternalSource(uri);

        ContributionsHolder holder = new ContributionsHolder();

        try (FileSystem jfs = FileSystems.newFileSystem(uri, new HashMap<>())) {
            BundleWalker walker = new BundleWalker(jfs.getPath("/"));
            walker.getRegistrationInfos().forEach(holder::load);
        }

        StudioSerializer serializer = new StudioSerializer(holder, opts);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serializeInto(baos, "schemas,lifecycles".split(","));

        String result = baos.toString("UTF-8");
        assertNotNull(result);
    }

    @Test
    public void loadSchemaForDependency() throws IOException, SAXException {
        URI uri = getJarURI();
        URL file = ExtractorContext.instance.getResourceFromFile(uri, "blalba");
        assertNull(file);

        file = ExtractorContext.instance.getResourceFromFile(uri, "schemas/aceinfo.xsd");
        assertNotNull(file);

        XSOMParser parser = new XSOMParser();
        parser.parse(file);
        XSSchemaSet xsSchemas = parser.getResult();
        assertNotNull(xsSchemas);
        assertEquals(2, xsSchemas.getSchemaSize());
    }
}
