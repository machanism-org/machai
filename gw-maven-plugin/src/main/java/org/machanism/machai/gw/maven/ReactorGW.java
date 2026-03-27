package org.machanism.machai.gw.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.machanism.machai.project.ProjectLayoutManager;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

/* @guidance:
Generate Javadoc that includes descriptions for all Maven plugin parameters, along with usage examples. Be sure to incorporate information from any superclasses.
ProcessModules supports Maven reactor for module processing. All submodules will be processed according to their dependencies, following standard Maven reactor logic.
*/

/**
 * Maven goal that processes documents across a multi-module (reactor) build.
 *
 * <p>
 * This goal supports Maven reactor module processing: all submodules are processed according to their dependencies,
 * following standard Maven reactor logic.
 * </p>
 *
 * <h2>Parameters</h2>
 * <p>
 * This goal does not introduce additional parameters beyond those inherited from {@link AbstractGWGoal}.
 * </p>
 *
 * <h3>Inherited parameters (from {@link AbstractGWGoal})</h3>
 * <ul>
 * <li><b>{@code gw.model}</b> / {@code &lt;model&gt;} ({@code model}): Provider/model identifier to pass to the
 * workflow.</li>
 * <li><b>${basedir}</b> ({@code basedir}): Maven module base directory.</li>
 * <li><b>{@code gw.scanDir}</b> / {@code &lt;scanDir&gt;} ({@code scanDir}): Optional scan root override. When omitted,
 * defaults to the module base directory.</li>
 * <li><b>{@code gw.instructions}</b> / {@code &lt;instructions&gt;} ({@code instructions}): Instruction locations (for
 * example, file paths or classpath locations) consumed by the workflow.</li>
 * <li><b>{@code gw.guidance}</b> / {@code &lt;guidance&gt;} ({@code guidance}): Default guidance text forwarded to the
 * workflow.</li>
 * <li><b>{@code gw.excludes}</b> / {@code &lt;excludes&gt;} ({@code excludes}): Exclude patterns/paths that should be
 * skipped when scanning documentation sources.</li>
 * <li><b>{@code genai.serverId}</b> ({@code serverId}): {@code settings.xml} {@code &lt;server&gt;} id used to read GenAI
 * credentials.</li>
 * <li><b>{@code logInputs}</b> ({@code logInputs}): Whether to log the list of input files passed to the workflow.
 * <p>
 * Default: {@code false}
 * </p>
 * </li>
 * </ul>
 *
 * <h2>Usage examples</h2>
 *
 * <h3>Command line</h3>
 *
 * <pre>
 * mvn gw:reactor
 * </pre>
 *
 * <h3>Override scan root</h3>
 *
 * <pre>
 * mvn gw:reactor -Dgw.scanDir=src\\site
 * </pre>
 *
 * <h3>Use Maven settings credentials for the GenAI provider</h3>
 *
 * <pre>
 * mvn gw:reactor -Dgenai.serverId=my-model-server
 * </pre>
 *
 * <h3>Plugin configuration</h3>
 *
 * <pre>
 * &lt;plugin&gt;
 *   &lt;groupId&gt;org.machanism&lt;/groupId&gt;
 *   &lt;artifactId&gt;gw-maven-plugin&lt;/artifactId&gt;
 *   &lt;version&gt;...&lt;/version&gt;
 *   &lt;configuration&gt;
 *     &lt;!-- inherited from AbstractGWGoal --&gt;
 *     &lt;scanDir&gt;${project.basedir}&lt;/scanDir&gt;
 *     &lt;logInputs&gt;true&lt;/logInputs&gt;
 *   &lt;/configuration&gt;
 * &lt;/plugin&gt;
 * </pre>
 */
@Mojo(name = "reactor", threadSafe = true)
public class ReactorGW extends AbstractGWGoal {

	@Override
	public void execute() throws MojoExecutionException {
		String executionRootDirectory = session.getExecutionRootDirectory();

		PropertiesConfigurator config = getConfiguration();

		GuidanceProcessor documents = new GuidanceProcessor(new File(executionRootDirectory), model, config) {

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

		if (scanDir == null) {
			scanDir = basedir.getAbsolutePath();
		}

		try {
			scanDocuments(documents);
		} catch (ProcessTerminationException e) {
			throw new MojoExecutionException(
					"Process terminated while scanning documents: " + e.getMessage() + " (exit code: " + e.getExitCode()
							+ ")",
					e);
		}
	}

}
