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

package org.nuxeo.maven.runtime;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.nuxeo.ecm.core.schema.SchemaManagerImpl;
import org.nuxeo.runtime.RuntimeService;
import org.nuxeo.runtime.model.RegistrationInfo;
import org.nuxeo.runtime.model.RuntimeContext;
import org.nuxeo.runtime.model.StreamRef;
import org.osgi.framework.Bundle;

/**
 * Simple Fake {@link RuntimeContext} that load all {@link MavenProject} child's class path (Maven compile resolution
 * scope) elements to a custom {@link ClassLoader}. It is required, in order to be able to resolve external Type from
 * plugin. For instance, it is needed by {@link org.nuxeo.ecm.automation.core.OperationContribution}.
 * <p>
 * The only implemented method is {@link MojoRuntime#loadClass(java.lang.String)}, otherwise an
 * {@link UnsupportedOperationException} is thrown.
 * </p>
 * <p>
 * Runtime will load types from current class loader if
 * {@link MojoRuntime#initCustomClassLoader(org.apache.maven.project.MavenProject)} has never been called.
 * </p>
 */
public class MojoRuntime implements RuntimeContext {

    public static MojoRuntime instance = new MojoRuntime();

    private static ClassLoader custom;

    public static SchemaManagerImpl schemaManager = new SchemaManagerImpl();

    private Set<URI> extResourcesSources = new HashSet<>();

    private MojoRuntime() {
    }

    /**
     * Add a custom class loader based on {@code project} parameter. It loads current classes and project child's
     * classes.
     *
     * @param project Current Project as target class loader
     * @throws MojoExecutionException in case of any checked exception
     */
    public static void initCustomClassLoader(MavenProject project) throws MojoExecutionException {
        try {
            Set<String> compileClasspathElements = new HashSet<>();
            compileClasspathElements.addAll(project.getCompileClasspathElements());
            for (MavenProject child : project.getCollectedProjects()) {
                compileClasspathElements.addAll(child.getCompileClasspathElements());
            }

            List<URL> urlElements = new ArrayList<>();
            for (String s : compileClasspathElements) {
                urlElements.add(new File(s).toURI().toURL());
            }

            custom = new URLClassLoader(urlElements.toArray(new URL[0]),
                    Thread.currentThread().getContextClassLoader());
        } catch (DependencyResolutionRequiredException | MalformedURLException e) {
            throw new MojoExecutionException("Unable to load compile dependencies", e);
        }
    }

    public void addResourcesSource(URI source) {
        extResourcesSources.add(source);
    }

    protected ClassLoader getClassloader() {
        if (custom != null) {
            return custom;
        } else {
            return Thread.currentThread().getContextClassLoader();
        }
    }

    @Override
    public Class<?> loadClass(String s) throws ClassNotFoundException {
        return getClassloader().loadClass(s);
    }

    @Override
    public RuntimeService getRuntime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getBundle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getResource(String name) {
        return getLocalResource(name);
    }

    @Override
    public URL getLocalResource(String name) {
        URL loadedResource = getClassloader().getResource(name);
        if (loadedResource != null) {
            return loadedResource;
        }

        try {
            File file = new File(name);
            if (file.exists()) {
                return file.toURI().toURL();
            }
        } catch (MalformedURLException e) {
            // Should never happen
        }

        return extResourcesSources.stream()
                                  .map(s -> this.getResourceFromFile(s, name))
                                  .filter(Objects::nonNull)
                                  .findFirst()
                                  .orElse(null);
    }

    public URL getResourceFromFile(URI uri, String name) {
        try {
            try (FileSystem fs = FileSystems.newFileSystem(uri, new HashMap<>())) {
                Path path = fs.getPath(name);

                if (Files.exists(path)) {
                    return path.toUri().toURL();
                }
            }
        } catch (IOException e) {
            // Ignore
        }

        return null;
    }

    @Override
    public RegistrationInfo deploy(URL url) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public RegistrationInfo deploy(StreamRef streamRef) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void undeploy(URL url) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void undeploy(StreamRef streamRef) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDeployed(URL url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDeployed(StreamRef streamRef) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RegistrationInfo deploy(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void undeploy(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDeployed(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy() {

    }

    public void clearExternalSources() {
        extResourcesSources.clear();
    }
}
