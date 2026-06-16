package org.machanism.machai.gw.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.machanism.machai.gw.tools.ProcessTerminationException;
import org.machanism.machai.project.ProjectLayoutManager;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

/* @guidance:
Generate Javadoc that includes descriptions for all Maven plugin parameters, along with usage examples. Be sure to incorporate information from any superclasses.
ProcessModules supports Maven reactor for module processing. All submodules will be processed according to their dependencies, following standard Maven reactor logic.
*/

/**
 * Maven goal that processes documents for the current Maven module.
 *
 * <p>
 * Unlike an aggregator goal, this mojo runs once per module in the active Maven
 * reactor. Maven determines module execution order using the standard reactor
 * dependency graph, so submodules are processed according to their dependencies.
 * </p>
 *
 * <p>
 * The goal reuses the shared scanning and GenAI configuration implemented by
 * {@link AbstractGWMojo}. It initializes a {@link GuidanceProcessor} rooted at
 * the reactor execution directory while binding Maven-specific project layout
 * metadata for each processed module.
 * </p>
 *
 * <h2>Parameters</h2>
 * <p>
 * This goal does not declare additional parameters. It uses the inherited
 * parameters from {@link AbstractGWMojo}.
 * </p>
 *
 * <h3>Inherited parameters</h3>
 * <ul>
 * <li><b>{@code gw.model}</b> ({@code model}) - Provider/model identifier passed
 * to the workflow.</li>
 * <li><b>${basedir}</b> ({@code basedir}) - Maven module base directory used as
 * the default module location.</li>
 * <li><b>{@code gw.path}</b> ({@code path}) - Optional scan root override.
 * If not provided, the goal scans the current module base directory.</li>
 * <li><b>{@code gw.instructions}</b> / {@code <instructions>} ({@code instructions})
 * - Instruction locations consumed by the workflow.</li>
 * <li><b>{@code gw.excludes}</b> / {@code <excludes>} ({@code excludes}) -
 * Exclude patterns or paths skipped during scanning.</li>
 * <li><b>${project}</b> ({@code project}) - The current Maven project injected by
 * Maven.</li>
 * <li><b>${session}</b> ({@code session}) - The current Maven session.</li>
 * <li><b>${settings}</b> ({@code settings}) - Maven settings used to resolve
 * credentials from {@code settings.xml}.</li>
 * <li><b>{@code genai.serverId}</b> ({@code serverId}) - Maven {@code server} id
 * used to resolve GenAI credentials.</li>
 * <li><b>{@code logInputs}</b> ({@code logInputs}) - Whether to log the list of
 * workflow input files. Default: {@code false}.</li>
 * <li><b>${reactorProjects}</b> ({@code reactorProjects}) - Reactor projects
 * available in the current Maven session.</li>
 * </ul>
 *
 * <h2>Usage examples</h2>
 *
 * <h3>Process each module in the current reactor build</h3>
 * <pre>
 * mvn gw:gw-per-module
 * </pre>
 *
 * <h3>Restrict scanning to a module documentation directory</h3>
 * <pre>
 * mvn gw:gw-per-module -Dgw.path=src/site
 * </pre>
 *
 * <h3>Use instructions and excludes while processing reactor modules</h3>
 * <pre>
 * mvn gw:gw-per-module \
 *   -Dgw.instructions=src/site/guidance.md \
 *   -Dgw.excludes=target,node_modules
 * </pre>
 *
 * <h3>Resolve GenAI credentials from Maven settings</h3>
 * <pre>
 * mvn gw:gw-per-module -Dgenai.serverId=my-model-server
 * </pre>
 *
 * <h3>Plugin configuration</h3>
 * <pre>
 * &lt;plugin&gt;
 *   &lt;groupId&gt;org.machanism&lt;/groupId&gt;
 *   &lt;artifactId&gt;gw-maven-plugin&lt;/artifactId&gt;
 *   &lt;version&gt;...&lt;/version&gt;
 *   &lt;configuration&gt;
 *     &lt;model&gt;openai:gpt-4.1&lt;/model&gt;
 *     &lt;paths&gt;${project.basedir}/src/site&lt;/paths&gt;
 *     &lt;instructions&gt;src/site/guidance.md&lt;/instructions&gt;
 *     &lt;logInputs&gt;true&lt;/logInputs&gt;
 *   &lt;/configuration&gt;
 * &lt;/plugin&gt;
 * </pre>
 */
@Mojo(name = "gw-per-module", aggregator = false, threadSafe = true, requiresProject = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GWPerModuleMojo extends AbstractGWMojo {

	@Override
	public void execute() throws MojoExecutionException {
		String executionRootDirectory = session.getExecutionRootDirectory();

		PropertiesConfigurator config = getConfiguration();

		GuidanceProcessor processor = new GuidanceProcessor(new File(executionRootDirectory), model, config) {

			@Override
			public ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
				ProjectLayout projectLayout = ProjectLayoutManager.detectProjectLayout(projectDir);

				if (projectLayout instanceof MavenProjectLayout) {
					MavenProjectLayout mavenProjectLayout = (MavenProjectLayout) projectLayout;
					mavenProjectLayout.projectDir(projectDir);

					Model model = project.getModel();
					mavenProjectLayout.model(model);
				}

				return projectLayout;
			}

			@Override
			protected void processModule(File projectDir, String module) throws IOException {
				// No-op for this implementation
			}
		};

		if (paths == null) {
			paths = basedir.getAbsolutePath();
		}

		if (session.getRequest().isProjectPresent()) {
			classFunctionTools.scanProjectClasses(project);
			processor.addTool(classFunctionTools);
		}

		try {
			scanDocuments(processor);
		} catch (ProcessTerminationException e) {
			throw new MojoExecutionException(
					"Process terminated while scanning documents: " + e.getMessage() + " (exit code: " + e.getExitCode()
							+ ")",
					e);
		}
	}

}
