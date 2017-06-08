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

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.maven.publisher.Publisher;

public class TestPublisher {
    ExtractorMojo mojo;

    @Before
    public void setup() throws MojoExecutionException, DependencyResolutionRequiredException {
        mojo = new ExtractorMojo();
        mojo.output = "test-output.json";
        mojo.connectUrl = "https://nos-test-connect.nos.nuxeo.com/nuxeo/site";

        MavenProject mvnProject = mock(MavenProject.class);
        when(mvnProject.getCollectedProjects()).thenReturn(Collections.emptyList());
        when(mvnProject.getCompileClasspathElements()).thenReturn(Collections.emptyList());

        when(mvnProject.getBuild()).thenReturn(mock(Build.class));
        when(mvnProject.getBuild().getDirectory()).thenReturn(FileUtils.getTempDirectoryPath());
        mojo.project = mvnProject;

        mojo.initialize();
    }

    @Test
    public void testPublisherInstanciation() {
        Publisher instance = Publisher.instance(mojo);
        assertTrue(instance instanceof Publisher.FilePublisher);

        mojo.token = "FAKE-TOKEN";
        instance = Publisher.instance(mojo);
        assertTrue(instance instanceof Publisher.FilePublisher);

        mojo.symbolicName = "symbolicName";
        instance = Publisher.instance(mojo);
        assertTrue(instance instanceof Publisher.StudioPublisher);
    }

    @Test
    public void testFilePublisher() throws IOException {
        Publisher instance = Publisher.instance(mojo);
        assertTrue(instance instanceof Publisher.FilePublisher);

        instance.publish(new String[0]);
        File output = ((Publisher.FilePublisher) instance).getOutput();
        assertTrue(output.exists());
    }

    @Test
    public void testStudioPublisher() throws IOException {
        mojo.token = System.getenv("STUDIO_TOKEN");
        mojo.symbolicName = System.getenv("STUDIO_SYM_NAME");

        Publisher instance = Publisher.instance(mojo);
        assumeTrue("Studio Credentials not initialized", instance instanceof Publisher.StudioPublisher);

        instance.publish(new String[0]);
    }
}
