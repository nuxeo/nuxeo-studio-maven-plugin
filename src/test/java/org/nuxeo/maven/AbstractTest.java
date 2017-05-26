package org.nuxeo.maven;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Before;
import org.nuxeo.maven.bundle.BundleWalker;
import org.nuxeo.runtime.model.RegistrationInfo;

public class AbstractTest {
    protected BundleWalker walker;

    @Before
    public void beforeEach() {
        walker = new BundleWalker();
        walker.setBasePath(new File("src/it/simple-bundle/src/main/resources"));
        walker.setPrefix("");
    }

    protected RegistrationInfo getRegistrationInfo(String resourcePath) throws URISyntaxException {
        URL resource = getClass().getClassLoader().getResource(resourcePath);
        assertNotNull(resource);

        RegistrationInfo ri = walker.read(new File(resource.toURI()).toPath());
        assertNotNull(ri);

        return ri;
    }
}
