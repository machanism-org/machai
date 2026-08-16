package org.machanism.machai.gw.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.UsageStatistics;
import org.machanism.machai.gw.processor.GWConstants;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.machanism.machai.gw.tools.ProcessTerminationException;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Maven goal that scans project files for Ghostwriter guidance comments and
 * executes the configured AI-assisted processing workflow.
 *
 * <p>
 * This goal is exposed as {@code gw:gw}. It can run in a normal Maven project,
 * across a multi-module build, or without a {@code pom.xml}. When Maven parallel
 * execution is enabled, module traversal is coordinated by {@code gw} rather
 * than by the Maven reactor. Sub-modules are processed before their parent
 * modules, matching the reverse-order behavior of the Ghostwriter CLI and the
 * {@code gw:act} goal.
 * </p>
 *
 * <h2>Parameters inherited from {@link AbstractGWMojo}</h2>
 * <dl>
 * <dt>{@code model}</dt>
 * <dd>Provider/model identifier used by the workflow. Example:
 * {@code mvn gw:gw -Dgw.model=openai:gpt-4o-mini}.</dd>
 * <dt>{@code basedir}</dt>
 * <dd>Maven module base directory, injected from {@code ${basedir}} by default.
 * Example plugin configuration: {@code <basedir>${project.basedir}</basedir>}.</dd>
 * <dt>{@code path}</dt>
 * <dd>Optional scan root override. Example:
 * {@code mvn gw:gw -Dgw.path=src/main/java}.</dd>
 * <dt>{@code instructions}</dt>
 * <dd>Additional workflow instructions or instruction file locations. Example:
 * {@code mvn gw:gw -Dgw.instructions=docs/gw-instructions.md}.</dd>
 * <dt>{@code excludes}</dt>
 * <dd>Path or glob-like patterns skipped during scanning. Example plugin
 * configuration: {@code <excludes><exclude>target/**</exclude></excludes>}.</dd>
 * <dt>{@code project}</dt>
 * <dd>The current Maven project, injected from {@code ${project}} when a project
 * is present. Example plugin configuration: {@code <project>${project}</project>}.
 * </dd>
 * <dt>{@code session}</dt>
 * <dd>The current Maven session, injected from {@code ${session}} and used to
 * resolve reactor projects, execution root, and parallel execution settings.
 * Example plugin configuration: {@code <session>${session}</session>}.</dd>
 * <dt>{@code settings}</dt>
 * <dd>Maven settings, injected from {@code ${settings}}, used for credentials
 * resolution. Example plugin configuration: {@code <settings>${settings}</settings>}.
 * </dd>
 * <dt>{@code serverId}</dt>
 * <dd>Maven {@code settings.xml} server id used to resolve GenAI credentials.
 * Example: {@code mvn gw:gw -Dgenai.serverId=my-ai-provider} or
 * {@code <serverId>my-ai-provider</serverId>}.</dd>
 * <dt>{@code reactorProjects}</dt>
 * <dd>Read-only list of reactor projects injected from {@code ${reactorProjects}}
 * for multi-module builds. Example plugin configuration:
 * {@code <reactorProjects>${reactorProjects}</reactorProjects>}.</dd>
 * </dl>
 *
 * <p>
 * The parameters above are inherited from {@link AbstractGWMojo}; the superclass
 * also supplies configuration creation and document-scanning support through
 * {@code getConfiguration()} and {@code scanDocuments(GuidanceProcessor)}.
 * Parameters may be supplied either as Maven properties (for example,
 * {@code -Dgw.model=openai:gpt-4o-mini}) or in the plugin configuration.
 * </p>
 *
 * <h2>Usage examples</h2>
 * <pre>{@code
 * mvn gw:gw
 * mvn -T 4 gw:gw
 * mvn gw:gw -Dgw.path=src/test/java -Dgw.instructions=docs/guidance.md
 * mvn gw:gw -Dgenai.serverId=my-ai-provider
 * }</pre>
 *
 * <p>
 * To include the literal closing Javadoc delimiter in generated documentation,
 * escape it as {@code *&#47;}.
 * </p>
 *
 * @since 1.1.2
 */
/*@guidance:
 * # Generate detailed Javadoc documentation for the Maven plugin, including:
 * - A clear description of all Maven plugin parameters.
 * - Examples of usage for each parameter.
 * - Information inherited from any superclass or parent class.
 * - Escape the closing javadoc tag in javadoc content, as it was breaking javadoc compilation.
 * - Note: When using Maven parallel execution, the modules will be processed by `gw` 
 *   instead of the Maven reactor.     
 * - **Processing Order for the `gw:act` Goal:**
 *   - The `gw:act` goal processes files in reverse order, similar to the Ghostwriter CLI.
 *   - Sub-modules are processed first, followed by parent modules.
 *   - This goal can be executed without a `pom.xml` file.
 *   - **Tip:** For improved performance, use Maven parallel execution: `mvn -T 4 gw:gw`.
 *  @since 1.1.2
 */

@Mojo(name = "gw", threadSafe = true, aggregator = true, requiresProject = false, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GWMojo extends AbstractGWMojo {

    /**
     * Executes the {@code gw:gw} goal.
     *
     * <p>
     * The method builds the effective processor configuration, creates a
     * {@link GuidanceProcessor}, resolves Maven module layout information when
     * available, configures non-recursive and parallel execution behavior, and then
     * delegates scanning to {@link #scanDocuments(GuidanceProcessor)}.
     * </p>
     *
     * @throws MojoExecutionException if configuration, scanning, or processing
     *                                fails
     */
    @Override
    public void execute() throws MojoExecutionException {
        UsageStatistics.init();

        PropertiesConfigurator config = getConfiguration();

        String model = config.get(GWConstants.MODEL_PROP_NAME, this.model);
        GuidanceProcessor processor = new GuidanceProcessor(basedir, model, config) {

            @Override
            public ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
                ProjectLayout projectLayout = super.getProjectLayout(projectDir);
                projectLayout.projectDir(projectDir);

                if (projectLayout instanceof MavenProjectLayout) {
                    MavenProjectLayout mavenProjectLayout = (MavenProjectLayout) projectLayout;

                    Model model = mavenProjectLayout.getModel();
                    MavenProject matchingProject = resolveProjectByArtifactId(session.getAllProjects(), model);
                    if (matchingProject != null) {
                        if (session.getRequest().isProjectPresent()) {
                            classFunctionTools.scanProjectClasses(matchingProject);
                        }

                        mavenProjectLayout.model(matchingProject.getModel());
                    }
                }

                return projectLayout;
            }
        };

        List<MavenProject> modules = session.getAllProjects();
        boolean nonRecursive = project.getModules().size() > 1 && modules.size() == 1;
        processor.setNonRecursive(nonRecursive);

        if (session.isParallel()) {
            int threads = session.getRequest().getDegreeOfConcurrency();
            logger.info("Threads: {}", threads);
            processor.setThreads(threads);
        }

        try {
            scanDocuments(processor);
        } catch (ProcessTerminationException e) {
            getLog().error("Process terminated: " + e.getMessage() + " (exit code: " + e.getExitCode() + ")");
            throw new MojoExecutionException(
                    "Process terminated: " + e.getMessage() + " (exit code: " + e.getExitCode() + ")", e);
        }
    }

    /**
     * Resolves the Maven project from the current session that matches the
     * artifact id of the supplied effective model.
     *
     * @param allProjects    all Maven projects available in the session
     * @param effectiveModel effective model discovered from a project layout
     * @return the matching Maven project, or {@code null} when no unique match is
     *         available
     * @throws IllegalStateException if multiple session projects share the same
     *                               artifact id
     */
    private static MavenProject resolveProjectByArtifactId(List<MavenProject> allProjects, Model effectiveModel) {
        if (allProjects == null || allProjects.isEmpty() || effectiveModel == null) {
            return null;
        }

        String effectiveArtifactId = StringUtils.trimToNull(effectiveModel.getArtifactId());
        if (effectiveArtifactId == null) {
            return null;
        }

        Set<String> matching = new HashSet<>();
        for (MavenProject mavenProject : allProjects) {
            if (mavenProject == null) {
                continue;
            }
            if (Strings.CS.equals(mavenProject.getArtifactId(), effectiveArtifactId)) {
                matching.add(toCoord(mavenProject));
            }
        }

        if (matching.isEmpty()) {
            return null;
        }

        if (matching.size() > 1) {
            throw new IllegalStateException(
                    "Multiple Maven projects in session have artifactId='" + effectiveArtifactId + "': " + matching);
        }

        for (MavenProject mavenProject : allProjects) {
            if (mavenProject != null && Strings.CS.equals(mavenProject.getArtifactId(), effectiveArtifactId)) {
                return mavenProject;
            }
        }
        return null;
    }

    /**
     * Formats a Maven project as a coordinate string used in duplicate-artifact
     * diagnostics.
     *
     * @param project project to format
     * @return coordinate in {@code groupId:artifactId:version@basedir} form
     */
    private static String toCoord(MavenProject project) {
        if (project == null) {
            return "<null>";
        }
        String groupId = Objects.toString(project.getGroupId(), "");
        String artifactId = Objects.toString(project.getArtifactId(), "");
        String version = Objects.toString(project.getVersion(), "");
        String basedir = Objects.toString(project.getBasedir(), "");
        return groupId + ":" + artifactId + ":" + version + "@" + basedir;
    }

}
