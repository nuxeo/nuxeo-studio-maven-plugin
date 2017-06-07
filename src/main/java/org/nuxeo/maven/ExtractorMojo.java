package org.nuxeo.maven;

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
import org.nuxeo.maven.serializer.StudioSerializer;

/**
 * Parse each project to load contributions' descriptors from the MANIFEST.MF and map them to Studio format. Then,
 * depending of the {@link #token} parameter, JSON output is written in the {@link #output} file or pushed to Nuxeo
 * Studio.
 */
@Mojo(name = "extract", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE, inheritByDefault = false, aggregator = true, threadSafe = true)
public class ExtractorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, property = "nsmp.project")
    protected MavenProject project;

    /**
     * List of Studio registries exported. Multiple values must be separate using a coma.
     * </p>
     * Possible Values:
     * <ul>
     * <li>operations</li>
     * <li>doctypes</li>
     * <li>schemas</li>
     * <li>facets</li>
     * <li>events</li>
     * <li>permissions</li>
     * <li>lifecycles</li>
     * </ul>
     * Default: "operations"
     */
    @Parameter(defaultValue = "operations", property = "extract")
    protected String extract;

    /**
     * File output name. JSON Registries will be written in this file in the "output" directory.
     */
    @Parameter(defaultValue = "nuxeo-studio-registries.json", property = "output")
    protected String output;

    /**
     * Studio Authentication Token
     */
    @Parameter(property = "token")
    protected String token;

    /**
     * Nuxeo Studio Target Project Symbolic Name
     */
    @Parameter(property = "symbolicName")
    protected String symbolicName;

    /**
     * Nuxeo Connect URL
     */
    @Parameter(defaultValue = "https://connect.nuxeo.com/nuxeo/site", property = "connectUrl")
    protected String connectUrl;

    protected StudioSerializer serializer;

    protected ContributionsHolder holder;

    protected void initialize() throws MojoExecutionException {
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
            Publisher.instance(this).publish(extract.split(",\\s*"));
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
