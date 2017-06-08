package org.nuxeo.maven.runtime;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.nuxeo.runtime.RuntimeService;
import org.nuxeo.runtime.model.RegistrationInfo;
import org.nuxeo.runtime.model.RuntimeContext;
import org.nuxeo.runtime.model.StreamRef;
import org.osgi.framework.Bundle;

/**
 * Simple Fake {@link RuntimeContext} that load all {@link MavenProject} child's class path (Maven compile resolution
 * scope) elements to a custom {@link ClassLoader}. It is required, in order to be able to resolve external Type from
 * plugin. For instance, it is needed by {@link org.nuxeo.ecm.automation.core.OperationContribution}.
 * </p>
 * The only implemented method is {@link MojoRuntime#loadClass(java.lang.String)}, otherwise an
 * {@link UnsupportedOperationException} is thrown.
 * </p>
 * Runtime will load types from current class loader if
 * {@link MojoRuntime#initCustomClassLoader(org.apache.maven.project.MavenProject)} has never been called.
 */
public class MojoRuntime implements RuntimeContext {

    public static MojoRuntime instance = new MojoRuntime();

    private static ClassLoader custom;

    private MojoRuntime() {
    }

    /**
     * Add a custom class loader based on {@code project} parameter. It loads current classes and project child's
     * classes.
     *
     * @param project Current Project as target class loader
     * @throws MojoExecutionException
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

    @Override
    public Class<?> loadClass(String s) throws ClassNotFoundException {
        if (custom != null) {
            return custom.loadClass(s);
        } else {
            return Thread.currentThread().getContextClassLoader().loadClass(s);
        }
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
    public URL getResource(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getLocalResource(String s) {
        throw new UnsupportedOperationException();
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
}
