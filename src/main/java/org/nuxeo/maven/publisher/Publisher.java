package org.nuxeo.maven.publisher;

import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.nuxeo.maven.ExtractorMojo;
import org.nuxeo.maven.serializer.StudioSerializer;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public abstract class Publisher {

    protected StudioSerializer serializer;

    protected Publisher(StudioSerializer serializer) {
        this.serializer = serializer;
    }

    public static Publisher instance(ExtractorMojo mojo) {
        Publisher publisher = null;
        if (isNotBlank(mojo.getToken()) && isNotBlank(mojo.getSymbolicName())) {
            publisher = new Publisher.StudioPublisher(mojo.getSerializer(), mojo.getConnectUrl(),
                    mojo.getSymbolicName(), mojo.getToken());
        }

        if (publisher == null) {
            publisher = new Publisher.FilePublisher(mojo.getSerializer(),
                    new File(mojo.getProject().getBuild().getDirectory(), mojo.getOutput()));
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

        protected static final String REGISTRY_ENDPOINT = "studio/api/projects/{symbolicName}/operations";

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
            // https://nos-test-connect.nos.nuxeo.com/nuxeo/site/studio/api/projects/${symbolicName}/operations
            URI operationsUrl = UriBuilder.fromUri(connectUrl).path(REGISTRY_ENDPOINT).build(symbolicName);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                serializer.serializeInto(baos, targets);

                Client client = Client.create();
                ClientResponse post = client.resource(operationsUrl).header(TOKEN_HEADER, token).post(
                        ClientResponse.class, baos.toString("UTF-8"));

                if (post.getStatus() != NO_CONTENT.getStatusCode()) {
                    throw new IOException(post.getEntity(String.class));
                }
            }
        }
    }
}