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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.nuxeo.maven.bundle.BundleWalker;
import org.nuxeo.maven.runtime.MojoRuntime;

/**
 * Load contributions from several sources, depending of Mojo parameters
 */
public class MojoContributionsLoader {
    protected final ExtractorMojo mojo;

    public MojoContributionsLoader(ExtractorMojo mojo) {
        this.mojo = mojo;
    }

    protected static String getBuildOutputDirectory(MavenProject project) {
        // When project is standalone-pom; use the current directory instead of a build one
        return isStandaloneProject(project) ? Paths.get("").toAbsolutePath().toString()
                : project.getBuild().getOutputDirectory();
    }

    protected static boolean isStandaloneProject(MavenProject project) {
        return project.getId().startsWith("org.apache.maven:standalone-pom:");
    }

    public void load() throws MojoExecutionException {
        if (StringUtils.isNotBlank(mojo.getJarFile())) {
            // Based on external jarFile
            loadFromJarFile(mojo.getJarFile());
        } else {
            // Based on Project (Standalone project, other project)
            loadFromMavenProjects(mojo.getProjects());
        }
    }

    protected void loadFromJarFile(String jarFile) throws MojoExecutionException {
        File file = new File(jarFile);
        if (!file.exists()) {
            throw new MojoExecutionException("Unknown jar file: " + jarFile);
        }

        URI uri = URI.create("jar:file:" + file.getAbsolutePath());
        loadFromURI(uri);
    }

    protected void loadFromURI(URI uri) {
        MojoRuntime.instance.addResourcesSource(uri);
        try (FileSystem fs = FileSystems.newFileSystem(uri, new HashMap<>())) {
            new BundleWalker(fs.getPath("/")).getRegistrationInfos().forEach(mojo.holder::load);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void loadFromMavenProjects(List<MavenProject> projects) {
        projects.forEach(this::loadFromMavenProject);
    }

    protected void loadFromMavenProject(MavenProject project) {
        BundleWalker walker = new BundleWalker(getBuildOutputDirectory(project));
        try {
            walker.getRegistrationInfos().forEach(mojo.holder::load);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
