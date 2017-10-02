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

import static org.nuxeo.common.Environment.NUXEO_HOME;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.nuxeo.maven.bundle.BundleWalker;
import org.nuxeo.maven.bundle.ContributionsHolder;
import org.nuxeo.maven.bundle.FakeRuntimeService;
import org.nuxeo.maven.publisher.Publisher;
import org.nuxeo.maven.runtime.MojoRuntime;
import org.nuxeo.maven.serializer.StudioSerializer;
import org.nuxeo.runtime.api.Framework;

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

    static {
        // Fake Nuxeo Runtime initialization
        try {
            System.setProperty(NUXEO_HOME, Files.createTempDirectory("nuxeo").toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Framework.initialize(new FakeRuntimeService());
    }

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
     * Extract contributions from a jar file
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

    protected StudioSerializer serializer;

    protected ContributionsHolder holder;

    protected void initialize() throws MojoExecutionException, IOException {
        holder = new ContributionsHolder();
        serializer = new StudioSerializer(this, holder);

        if (!MojoContributionsLoader.isStandaloneProject(project)) {
            MojoRuntime.initCustomClassLoader(project);
        }
    }

    public String getBuildDirectory() {
        return MojoContributionsLoader.isStandaloneProject(project) ? Paths.get("").toAbsolutePath().toString()
                : project.getBuild().getDirectory();
    }

    protected List<MavenProject> getProjects() {
        List<MavenProject> projects = new ArrayList<>();
        projects.add(project);
        if (!MojoContributionsLoader.isStandaloneProject(project)) {
            projects.addAll(project.getCollectedProjects());
        }
        return projects;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            initialize();

            // Load contributions from current project and collected (child) ones
            new MojoContributionsLoader(this).load();

            String[] targets = "*".equals(extract) ? holder.getManager().getRegisteredTargets()
                    : extract.split(",|\\s\\s*");

            Publisher.instance(this).publish(targets);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to publish extractions", e);
        }
    }

    protected void loadContributions(MavenProject project) {
        BundleWalker walker = new BundleWalker(getBuildOutputDirectory(project));
        try {
            walker.getRegistrationInfos().forEach(holder::load);
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    public StudioSerializer getSerializer() {
        return serializer;
    }

    public ContributionsHolder getHolder() {
        return holder;
    }
}
