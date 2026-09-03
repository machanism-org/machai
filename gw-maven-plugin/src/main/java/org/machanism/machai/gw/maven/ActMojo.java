package org.machanism.machai.gw.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.machanism.machai.gw.processor.ActProcessor;

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
 *   - **Tip:** For improved performance, use Maven parallel execution: `mvn -T 4 gw:act`.
 *  @since 1.1.2
 */

/**
 * Maven goal that executes a Ghostwriter act against project files.
 *
 * <p>
 * This goal creates an {@link ActProcessor}, resolves configuration from Maven
 * parameters, user configFile and Ghostwriter configuration files, and then
 * scans the selected path. The goal is an aggregator, is thread-safe, and can
 * run without a {@code pom.xml}. When a Maven project is present, inherited
 * behavior from {@link AbstractGWMojo} contributes common Ghostwriter
 * parameters such as the base directory, model, path, instructions, excludes
 * and shared class-scanning tools.
 * </p>
 *
 * <h2>Processing order for {@code gw:act}</h2>
 * <p>
 * Files are processed in reverse order, matching the Ghostwriter CLI behavior:
 * sub-modules are processed first and parent modules are processed afterward.
 * When Maven parallel execution is enabled, modules are processed by
 * Ghostwriter rather than by the Maven reactor. For improved performance, run
 * for example:
 * </p>
 *
 * <pre>{@code
 * mvn -T 4 gw:act
 * }</pre>
 *
 * <h2>Parameters declared by this goal</h2>
 * <ul>
 * <li>{@code gw.act}: action prompt text or the name of a predefined act. If it
 * is omitted, the goal obtains the value from configured properties or, when
 * interactive input is available, prompts for it. Examples:
 * 
 * <pre>{@code
 * mvn gw:act -Dgw.act="Add missing Javadocs"
 * mvn gw:act -Dgw.act=review
 * }</pre>
 * 
 * </li>
 * <li>{@code gw.acts}: optional local directory or URL containing predefined
 * act definitions. It changes the location searched when {@code gw.act} names
 * an act. Examples:
 * 
 * <pre>{@code
 * mvn gw:act -Dgw.acts=.ghostwriter/acts -Dgw.act=review
 * mvn gw:act -Dgw.acts=src/gw/acts -Dgw.act=documentation
 * }</pre>
 * 
 * </li>
 * </ul>
 *
 * <h2>Inherited Maven plugin parameters</h2>
 * <p>
 * The following commonly used parameters are inherited from
 * {@link AbstractGWMojo} and are honored by this goal when present:
 * </p>
 * <ul>
 * <li>{@code gw.path}: file, directory, glob, or other supported path
 * expression to scan. When omitted, scanning uses the configured base
 * directory. Example:
 * 
 * <pre>{@code
 * mvn gw:act -Dgw.act=review -Dgw.path=src/main/java
 * }</pre>
 * 
 * </li>
 * <li>{@code gw.model}: AI provider or model override passed to the act
 * processor. Example:
 * 
 * <pre>{@code
 * mvn gw:act -Dgw.act=review -Dgw.model=gpt-4.1
 * }</pre>
 * 
 * </li>
 * <li>{@code gw.instructions}: additional instructions supplied to the act
 * processor. Example:
 * 
 * <pre>{@code
 * mvn gw:act -Dgw.act=review -Dgw.instructions="Focus on public API compatibility"
 * }</pre>
 * 
 * </li>
 * <li>{@code gw.excludes}: comma-separated files, directories, or patterns to
 * exclude from scanning. Example:
 * 
 * <pre>{@code
 * mvn gw:act -Dgw.act=review -Dgw.excludes=target,*.class
 * }</pre>
 * 
 * </li>
 * </ul>
 *
 * <h3>Additional inherited Maven parameters</h3>
 * <ul>
 * <li>{@code basedir}: Maven module base directory. Maven supplies this
 * automatically from {@code ${basedir}}; it normally does not need to be set
 * explicitly. Plugin XML may override it, for example:
 * {@code <configuration><basedir>${project.basedir}&#60;&#47;basedir>&#60;&#47;configuration>}.</li>
 * <li>{@code project}: current Maven project, supplied by Maven from
 * {@code ${project}}. It is used to discover project metadata and reactor
 * modules. Typical plugin configuration leaves Maven's default in place:
 * {@code <configuration><project>${project}&#60;&#47;project>&#60;&#47;configuration>}.</li>
 * <li>{@code session}: current Maven session, supplied by Maven from
 * {@code ${session}}. It provides the execution root, reactor state, and
 * parallel-build settings. Typical plugin configuration leaves Maven's default
 * in place:
 * {@code <configuration><session>${session}&#60;&#47;session>&#60;&#47;configuration>}.</li>
 * <li>{@code settings}: Maven settings, supplied by Maven from
 * {@code ${settings}}. The goal uses it to resolve the configured server; for
 * example, Maven injects {@code <settings>${settings}&#60;&#47;settings>} when
 * the parameter is not overridden.</li>
 * <li>{@code reactorProjects}: projects in the current reactor, supplied by
 * Maven from {@code ${reactorProjects}}; for example, Maven injects
 * {@code <reactorProjects>${reactorProjects}&#60;&#47;reactorProjects>} for a
 * multi-module build.</li>
 * <li>{@code genai.serverId}: optional Maven server identifier for provider
 * credentials. Example:
 * {@code mvn gw:act -Dgenai.serverId=machai-ai -Dgw.act=review}.</li>
 * <li>{@code gw.config}: optional properties-file path used when no server id
 * is configured. Example:
 * {@code mvn gw:act -Dgw.config=.ghostwriter/config.properties -Dgw.act=review}.</li>
 * <li>{@code params}: optional plugin configuration entries merged into
 * workflow configuration. Example:
 * {@code <configuration><params><timeout>30&#60;&#47;timeout>&#60;&#47;params>&#60;&#47;configuration>}.</li>
 * </ul>
 *
 * <h3>Configuration setting</h3>
 * <p>
 * {@code gw.interactive} is read from the effective Ghostwriter configuration
 * rather than declared as a Maven {@code @Parameter}. It enables or disables
 * prompting when an act value is missing; for example, set
 * {@code gw.interactive=false} in the configuration file selected by
 * {@code gw.config}.
 * </p>
 *
 * <p>
 * If Javadoc text needs to mention the literal closing tag, write it as
 * {@code *&#47;} so generated documentation does not terminate the comment
 * early.
 * </p>
 *
 * @since 1.1.2
 */
@Mojo(name = "act", aggregator = true, threadSafe = true, requiresProject = false, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ActMojo extends AbstractActMojo {

	/**
	 * Updates Maven layout metadata for the matching reactor project.
	 *
	 * @param mavenProjectLayout layout to update
	 * @param model              model used to identify the reactor project
	 */
	@Override
	protected void updateMavenProjectLayout(org.machanism.machai.project.layout.MavenProjectLayout mavenProjectLayout,
			org.apache.maven.model.Model model) {
		super.updateMavenProjectLayout(mavenProjectLayout, model);
	}

	@Override
	public void execute() throws MojoExecutionException {
		performAct(actPrompt);
	}

	/**
	 * Executes the configured act. Kept as an extension point for programmatic
	 * callers that need to customize execution.
	 *
	 * @throws MojoExecutionException if processing fails
	 */
	public void execute(ActMojo ignored) throws MojoExecutionException {
		execute();
	}

}
