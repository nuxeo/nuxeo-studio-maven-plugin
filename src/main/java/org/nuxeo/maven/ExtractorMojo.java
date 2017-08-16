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
import org.nuxeo.maven.publisher.Publisher;
import org.nuxeo.maven.runtime.MojoRuntime;
import org.nuxeo.maven.serializer.StudioSerializer;

/**
 * Parse each project to load contributions' descriptors from the MANIFEST.MF and map them to studio contributions
 * registry format.
 * <p>
 * Then, depending of the {@link #token} parameter, JSON output is written in the {@link #output} file or pushed to
 * Nuxeo Studio.
 * </p>
 */
@Mojo(name = "extract", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE, inheritByDefault = false, aggregator = true, threadSafe = true)
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
     * Nuxeo Connect URL
     */
    @Parameter(defaultValue = "https://connect.nuxeo.com/nuxeo/site", property = "nsmp.connectUrl")
    protected String connectUrl;

    protected StudioSerializer serializer;

    protected ContributionsHolder holder;

    protected void initialize() throws MojoExecutionException {
        System.setProperty(NUXEO_HOME, project.getBuild().getOutputDirectory());
        holder = new ContributionsHolder();
        serializer = new StudioSerializer(holder);

        MojoRuntime.initCustomClassLoader(project);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        initialize();

        try {
            // Load contributions from current project and collected (child) ones
            loadContributions(project);
            for (MavenProject child : project.getCollectedProjects()) {
                loadContributions(child);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read bundle contributions", e);
        }

        try {
            String[] targets = "*".equals(extract) ? holder.getManager().getRegisteredTargets() : extract.split(",\\s*");

            Publisher.instance(this).publish(targets);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to publish extractions", e);
        }
    }

    protected void loadContributions(MavenProject project) throws IOException {
        BundleWalker walker = new BundleWalker(project.getBasedir());
        walker.getRegistrationInfos().forEach(holder::load);
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

    public StudioSerializer getSerializer() {
        return serializer;
    }

    public ContributionsHolder getHolder() {
        return holder;
    }
}
