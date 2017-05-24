package org.nuxeo.maven;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

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
import org.nuxeo.maven.serializer.StudioSerializer;

/**
 * Nuxeo Extractor Mojo
 * </p>
 * Goal of the Mojo is to parse each project to load contributions' descriptors from the MANIFEST.MF and map them to
 * Studio format. Then, depending of the {@link token} parameter, JSON output is written in the {@link output} file or
 * pushed to Nuxeo Studio.
 */
@Mojo(name = "nuxeo-extractor", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE, inheritByDefault = false, aggregator = true)
public class ExtractorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
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

    private StudioSerializer serializer;

    private ContributionsHolder holder;

    private void initialize() throws MojoExecutionException {
        holder = new ContributionsHolder();
        serializer = new StudioSerializer(holder);

        MojoRuntime.initProjectClassLoader(project);
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

            // Output Studio JSON in the output file
            try (PrintStream printStream = new PrintStream(new File(project.getBuild().getDirectory(), output))) {
                serializer.serializeInto(printStream, extract.split(",\\s*"));
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read a file", e);
        }
    }

    protected void loadContributions(MavenProject project) throws IOException {
        BundleWalker walker = new BundleWalker(project.getBasedir());
        walker.getRegistrationInfos().forEach(holder::load);
    }
}
