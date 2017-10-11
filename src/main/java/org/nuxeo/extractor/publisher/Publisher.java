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

package org.nuxeo.extractor.publisher;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.CREATED;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.nuxeo.extractor.ExtractorMojo;
import org.nuxeo.extractor.ExtractorOptions;
import org.nuxeo.extractor.serializer.StudioSerializer;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public abstract class Publisher {

    protected StudioSerializer serializer;

    protected Publisher(StudioSerializer serializer) {
        this.serializer = serializer;
    }

    public static Publisher instance(StudioSerializer serializer, ExtractorOptions opts) {
        Publisher publisher = null;
        if (isNotBlank(opts.getToken()) && isNotBlank(opts.getSymbolicName())) {
            publisher = new Publisher.StudioPublisher(serializer, opts.getConnectUrl(), opts.getSymbolicName(),
                    opts.getToken());
        }

        if (publisher == null) {
            publisher = new Publisher.FilePublisher(serializer, new File(opts.getBuildDirectory(), opts.getOutput()));
        }
        return publisher;
    }

    public abstract void publish(String[] targets) throws IOException;

    /**
     * Publish Registries to a file in {@link ExtractorMojo#output} file
     */
    public static class FilePublisher extends Publisher {

        protected File output;

        public FilePublisher(StudioSerializer serializer, File output) {
            super(serializer);
            this.output = output;
        }

        @Override
        public void publish(String[] targets) throws IOException {
            // Output Studio JSON in the output file
            try (PrintStream printStream = new PrintStream(output)) {
                serializer.serializeInto(printStream, targets);
            }
        }

        public File getOutput() {
            return output;
        }
    }

    /**
     * Publish Registries to dedicated Studio endpoint
     */
    public static class StudioPublisher extends Publisher {

        protected static final String TOKEN_HEADER = "X-Authentication-Token";

        protected static final String REGISTRY_ENDPOINT = "site/studio/v2/project/{symbolicName}/workspace/ws.registries";

        protected String connectUrl;

        protected String symbolicName;

        protected String token;

        public StudioPublisher(StudioSerializer serializer, String connectUrl, String symbolicName, String token) {
            super(serializer);
            this.connectUrl = connectUrl;
            this.symbolicName = symbolicName;
            this.token = token;
        }

        @Override
        public void publish(String[] targets) throws IOException {
            URI operationsUrl = UriBuilder.fromUri(connectUrl).path(REGISTRY_ENDPOINT).build(symbolicName);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                serializer.serializeInto(baos, targets);

                Client client = Client.create();
                ClientResponse post = client.resource(operationsUrl)
                                            .type(APPLICATION_JSON_TYPE)
                                            .header(TOKEN_HEADER, token)
                                            .post(ClientResponse.class, baos.toString("UTF-8"));

                if (post.getStatus() != CREATED.getStatusCode()) {
                    throw new IOException(post.getEntity(String.class));
                }
            }
        }
    }
}