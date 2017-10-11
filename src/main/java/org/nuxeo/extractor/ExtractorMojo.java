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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.nuxeo.extractor.runtime.ExtractorRuntimeContext;

/**
 * Parse each project to load contributions' descriptors from the MANIFEST.MF and map them to studio contributions
 * registry format.
 * <p>
 * Then, depending of the {@link #token} parameter, JSON output is written in the {@link #output} file or pushed to
 * Nuxeo Studio.
 * </p>
 */
@Mojo(name = "extract", requiresProject = false, defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, inheritByDefault = false, aggregator = true, threadSafe = true)
public class ExtractorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, property = "nsmp.project")
    protected MavenProject project;

    /**
     * List of contributions registries exported. Multiple values must be separate using a coma.
     * <p>
     * Possible Values:
     * </p>
     */
    @Parameter(defaultValue = "*", property = "nsmp.extract")
    protected String extract;

    /**
     * Extract contributions from a jar file. Multiple files must be separate using a coma.
     */
    @Parameter(property = "nsmp.jarFile")
    protected String jarFile;

    /**
     * File output name. JSON Registries will be written in this file in the "output" directory.
     */
    @Parameter(defaultValue = "nuxeo-studio-registries.json", property = "nsmp.output")
    protected String output;

    /**
     * Studio Authentication Token
     */
    @Parameter(property = "nsmp.token")
    protected String token;

    /**
     * Nuxeo Studio Target Project Symbolic Name
     */
    @Parameter(property = "nsmp.symbolicName")
    protected String symbolicName;

    /**
     * Extract fails if nothing is extracted
     */
    @Parameter(defaultValue = "false", readonly = true, property = "nsmp.failOnEmpty")
    protected boolean failOnEmpty;

    /**
     * Nuxeo Connect URL
     */
    @Parameter(defaultValue = "https://connect.nuxeo.com/nuxeo", property = "nsmp.connectUrl")
    protected String connectUrl;

    protected static String getBuildOutputDirectory(MavenProject project) {
        // When project is standalone-pom; use the current directory instead of a build one
        return isStandaloneProject(project) ? Paths.get("").toAbsolutePath().toString()
                : project.getBuild().getOutputDirectory();
    }

    protected static boolean isStandaloneProject(MavenProject project) {
        return project.getId().startsWith("org.apache.maven:standalone-pom:");
    }

    protected ExtractorOptions buildOptions() {
        ExtractorOptions opts = new ExtractorOptions();
        opts.setBuildDirectory(getBuildDirectory());
        opts.setConnectUrl(getConnectUrl());
        opts.setExtract(getExtract());
        opts.setOutput(getOutput());
        opts.setSymbolicName(getSymbolicName());
        opts.setToken(getToken());
        opts.setJarFile(getJarFile());
        opts.setFailOnEmpty(isFailOnEmpty());

        // Add all project as source directory
        getProjects().stream().map(ExtractorMojo::getBuildOutputDirectory).forEach(opts::addSourceDirectory);
        return opts;
    }

    protected void initializeProjectClassLoader() throws MojoExecutionException, IOException {
        Set<String> compileClasspathElements = new HashSet<>();
        if (!isStandaloneProject(project)) {
            try {
                compileClasspathElements.addAll(project.getCompileClasspathElements());
                for (MavenProject child : project.getCollectedProjects()) {
                    compileClasspathElements.addAll(child.getCompileClasspathElements());
                }
            } catch (DependencyResolutionRequiredException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }

        ExtractorRuntimeContext.initCustomClassLoader(compileClasspathElements);
    }

    public String getBuildDirectory() {
        return isStandaloneProject(project) ? Paths.get("").toAbsolutePath().toString()
                : project.getBuild().getDirectory();
    }

    protected List<MavenProject> getProjects() {
        List<MavenProject> projects = new ArrayList<>();
        projects.add(this.getProject());
        if (!isStandaloneProject(this.getProject())) {
            projects.addAll(this.getProject().getCollectedProjects());
        }
        return projects;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            initializeProjectClassLoader();
            ExtractorOptions opts = buildOptions();
            new ContributionsExtractor(opts).publish();
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to publish extractions", e);
        }
    }

    public MavenProject getProject() {
        return project;
    }

    public String getExtract() {
        return extract;
    }

    public String getOutput() {
        return output;
    }

    public String getToken() {
        return token;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public String getConnectUrl() {
        return connectUrl;
    }

    public boolean isFailOnEmpty() {
        return failOnEmpty;
    }

    public String getJarFile() {
        return jarFile;
    }
}
