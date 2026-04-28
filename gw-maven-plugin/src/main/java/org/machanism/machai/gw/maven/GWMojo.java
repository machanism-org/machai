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
import org.machanism.machai.ai.tools.CommandFunctionTools.ProcessTerminationException;
import org.machanism.machai.gw.processor.GWConstants;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

/*@guidance: 
 * - Generate javadoc with a description all maven plugin parameters and examples of usage included superclass information.. 
 * - **Processing Order for `gw:gw`:**
 *   - The `gw:gw` goal processes files in reverse order, similar to the Ghostwriter CLI.
 *   - Sub-modules are processed first, followed by parent modules.
 *   - For more details, see: https://www.machanism.org/guided-file-processing/index.html 
 *   - It can be run without pom.xml.
 */

/**
 * Maven goal {@code gw:gw} that runs Ghostwriter guided file processing for a
 * project.
 *
 * <p>
 * This goal is an aggregator and can be executed even when a {@code pom.xml} is
 * not present in the current directory (it sets {@code requiresProject=false}).
 * </p>
 *
 * <h2>Processing order</h2>
 * <p>
 * The {@code gw:gw} goal processes files in reverse order, similar to the
 * Ghostwriter CLI: sub-modules are processed first, followed by parent modules.
 * For more details, see:
 * <a href="https://www.machanism.org/guided-file-processing/index.html">Guided
 * file processing</a>.
 * </p>
 *
 * <h2>Parameters</h2>
 * <p>
 * This goal defines the following parameter in addition to those inherited from
 * {@link AbstractGWMojo}.
 * </p>
 *
 * <dl>
 * <dt><b>{@code -Dgw.threads}</b> / {@code &lt;threads&gt;}</dt>
 * <dd>Enables or disables multi-threaded module processing.
 * <p>
 * Default: {@code false}
 * </p>
 * </dd>
 * </dl>
 *
 * <h3>Inherited parameters (from {@link AbstractGWMojo})</h3>
 * <p>
 * The following parameters are defined on {@link AbstractGWMojo} and are
 * available to this goal. Refer to {@link AbstractGWMojo} for the authoritative
 * list and exact semantics.
 * </p>
 *
 * <dl>
 * <dt><b>{@code -Dgw.model}</b> / {@code &lt;model&gt;}</dt>
 * <dd>Provider/model identifier forwarded to the workflow. Example:
 * {@code openai:gpt-4o-mini}.</dd>
 *
 * <dt><b>{@code -Dgw.scanDir}</b> / {@code &lt;scanDir&gt;}</dt>
 * <dd>Optional scan root override. When omitted, defaults to the execution root
 * directory.</dd>
 *
 * <dt><b>{@code -Dgw.instructions}</b> / {@code &lt;instructions&gt;}</dt>
 * <dd>Instruction locations (for example, file paths or classpath locations)
 * consumed by the workflow.</dd>
 *
 * <dt><b>{@code -Dgw.excludes}</b> / {@code &lt;excludes&gt;}</dt>
 * <dd>Exclude patterns/paths to skip when scanning documentation sources.</dd>
 *
 * <dt><b>{@code -Dgenai.serverId}</b> / {@code &lt;serverId&gt;}</dt>
 * <dd>{@code settings.xml} {@code &lt;server&gt;} id used to read GenAI
 * credentials.</dd>
 *
 * <dt><b>{@code -DlogInputs}</b> / {@code &lt;logInputs&gt;}</dt>
 * <dd>Whether to log the list of input files passed to the workflow.
 * <p>
 * Default: {@code false}
 * </p>
 * </dd>
 * </dl>
 *
 * <h2>Usage examples</h2>
 *
 * <p>
 * Run in the current directory:
 * </p>
 *
 * <pre>
 * mvn gw:gw
 * </pre>
 *
 * <p>
 * Run without a {@code pom.xml} (this goal sets {@code requiresProject=false}):
 * </p>
 *
 * <pre>
 * cd path\\to\\project
 * mvn gw:gw
 * </pre>
 *
 * <p>
 * Enable multi-threaded processing:
 * </p>
 *
 * <pre>
 * mvn gw:gw -Dgw.threads=true
 * </pre>
 *
 * <p>
 * Disable multi-threaded processing (default):
 * </p>
 *
 * <pre>
 * mvn gw:gw -Dgw.threads=false
 * </pre>
 *
 * <p>
 * Run against a specific module:
 * </p>
 *
 * <pre>
 * mvn -pl :my-module gw:gw
 * </pre>
 *
 * @see AbstractGWMojo
 */
@Mojo(name = "gw", threadSafe = true, aggregator = true, requiresProject = false, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GWMojo extends AbstractGWMojo {

	@Override
	public void execute() throws MojoExecutionException {
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
		
		boolean isParallel = session.isParallel();
		if (isParallel) {
			int data = session.getRequest().getDegreeOfConcurrency();
			processor.setDegreeOfConcurrency(data);
		}

		try {
			scanDocuments(processor);
		} catch (ProcessTerminationException e) {
			getLog().error("Process terminated: " + e.getMessage() + " (exit code: " + e.getExitCode() + ")");
			throw new MojoExecutionException(
					"Process terminated: " + e.getMessage() + " (exit code: " + e.getExitCode() + ")", e);
		}
	}

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
