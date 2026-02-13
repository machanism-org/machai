package org.machanism.machai.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.gw.FileProcessor;
import org.machanism.machai.project.layout.MavenProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

/*@guidance: 
 * - Generate javadoc with a description all maven plugin parameters and examples of usage included superclass information.. 
 * - **Processing Order for `gw:gw`:**
 *   - The `gw:gw` goal processes files in reverse order, similar to the Ghostwriter CLI.
 *   - Sub-modules are processed first, followed by parent modules.
 *   - For more details, see: https://www.machanism.org/guided-file-processing/index.html 
 * 
 * Example of usage without project pom.xml file:
 * 
 * ```batch
 * @echo off
 * 
 * :: Set the project home directory variable
 * SET PROJECT_HOME=...
 * 
 * SET PARAMS=^
 *  -Dgw.genai=CodeMie:gpt-5-2-2025-12-11^
 *  -Dgw.scanDir=glob:.^
 *  -Dgw.guidance=file:%PROJECT_HOME%\guidance.txt^
 *  -Dgw.instructions=file:%PROJECT_HOME%\instructions.txt^
 *  -Dgw.logInputs=true^
 * 
 * call mvn %PARAMS%^
 *  -DGENAI_USERNAME=...^
 *  -DGENAI_PASSWORD=...^
 *  org.machanism.machai:gw-maven-plugin:0.0.9:gw
 * ```
 */

/**
 * Maven goal {@code gw:gw} for processing guided documents in a Maven project.
 *
 * <h2>Processing order</h2>
 * <p>
 * This goal processes files in reverse order, similar to the Ghostwriter CLI.
 * Sub-modules are processed first, followed by parent modules. For more details,
 * see:
 * <a href="https://www.machanism.org/guided-file-processing/index.html">Guided file
 * processing</a>.
 * </p>
 *
 * <h2>Parameters</h2>
 * <p>
 * This goal supports all parameters defined by {@link AbstractGWGoal}, plus the
 * additional parameter defined below.
 * </p>
 *
 * <h3>Inherited parameters (from {@link AbstractGWGoal})</h3>
 * <p>
 * The following parameters are inherited from {@link AbstractGWGoal} and are
 * available when running {@code gw:gw}. Refer to {@link AbstractGWGoal} for the
 * complete list and semantics.
 * </p>
 *
 * <h3>Goal-specific parameters</h3>
 * <ul>
 * <li><strong>{@code gw.threads}</strong> (type: {@code boolean}, default:
 * {@code true})
 * <ul>
 * <li><strong>Description:</strong> Enables or disables multi-threaded module
 * processing.</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <h2>Usage</h2>
 *
 * <h3>Run with defaults</h3>
 *
 * <pre>
 * mvn gw:gw
 * </pre>
 *
 * <h3>Disable multi-threaded module processing</h3>
 *
 * <pre>
 * mvn gw:gw -Dgw.threads=false
 * </pre>
 *
 * <h3>Typical configuration in {@code pom.xml}</h3>
 *
 * <pre>{@code
 * <plugin>
 *   <groupId>org.machanism</groupId>
 *   <artifactId>gw-maven-plugin</artifactId>
 *   <version>${gw-maven-plugin.version}</version>
 *   <executions>
 *     <execution>
 *       <goals>
 *         <goal>gw</goal>
 *       </goals>
 *     </execution>
 *   </executions>
 * </plugin>
 * }</pre>
 *
 * <h3>Usage without a project {@code pom.xml}</h3>
 *
 * <pre>{@code
 * @echo off
 *
 * :: Set the project home directory variable
 * SET PROJECT_HOME=...
 *
 * SET PARAMS=^
 *  -Dgw.genai=CodeMie:gpt-5-2-2025-12-11^
 *  -Dgw.scanDir=glob:.^
 *  -Dgw.guidance=file:%PROJECT_HOME%\guidance.txt^
 *  -Dgw.instructions=file:%PROJECT_HOME%\instructions.txt^
 *  -Dgw.logInputs=true^
 *
 * call mvn %PARAMS%^
 *  -DGENAI_USERNAME=...^
 *  -DGENAI_PASSWORD=...^
 *  org.machanism.machai:gw-maven-plugin:0.0.9:gw
 * }</pre>
 */
@Mojo(name = "gw", threadSafe = true, aggregator = true, requiresProject = false)
public class GW extends AbstractGWGoal {

	/** Enables or disables multi-threaded module processing. */
	@Parameter(property = "gw.threads", defaultValue = "true")
	private boolean threads;

	@Override
	public void execute() throws MojoExecutionException {
		PropertiesConfigurator config = getConfiguration();

		FileProcessor processor = new FileProcessor(genai, config) {

			@Override
			protected ProjectLayout getProjectLayout(File projectDir) throws FileNotFoundException {
				ProjectLayout projectLayout = super.getProjectLayout(projectDir);
				projectLayout.projectDir(projectDir);

				if (projectLayout instanceof MavenProjectLayout) {
					MavenProjectLayout mavenProjectLayout = (MavenProjectLayout) projectLayout;
					mavenProjectLayout.effectivePomRequired(false);

					Model model = mavenProjectLayout.getModel();
					for (MavenProject mavenProject : session.getAllProjects()) {
						if (StringUtils.equals(mavenProject.getArtifactId(), model.getArtifactId())) {
							mavenProjectLayout.model(mavenProject.getModel());
							break;
						}
					}
				}

				return projectLayout;
			}
		};

		List<MavenProject> modules = session.getAllProjects();
		boolean nonRecursive = project.getModules().size() > 1 && modules.size() == 1;
		processor.setNonRecursive(nonRecursive);

		processor.setModuleMultiThread(threads);
		scanDocuments(processor);
	}

}
