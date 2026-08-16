
/*-
 * @guidance:
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Describe all supported features based on javadoc information from:
 *      	- AIFileProcessor
 *			- GuidanceProcessor
 * 			- ActProcessor
 * 			- Episodes
 *      
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 * - Include Usage Examples Where Helpful:
 *      - Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * - Maintain Consistency and Formatting:
 *      - Follow a consistent style and structure for all Javadoc comments.
 *      - Use proper Markdown or HTML formatting for readability.
 *      - Make sure that the comment text does not contain the pair of characters: an asterisk followed by a forward slash.
 *        If so, always use the HTML entity `*&#47;` to prevent Javadoc compilation from breaking.
 * - Add Javadoc:
 *      - Review the Java class source code and include comprehensive Javadoc comments for all classes,
 *           methods, and fields, adhering to established best practices.
 *      - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *           and any exceptions thrown.
 *      - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;`
 *           and `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc.
 *      - Do not use escaping in `{@code ...}` tags. 
 *      - Escape the closing javadoc tag in javadoc content, as it was breaking javadoc compilation.
 */
/**
 * Provides the project-aware processing layer used by Ghostwriter to traverse
 * projects and submit file, guidance, or act prompts to a configured GenAI
 * provider.
 *
 * <p>{@link AbstractFileProcessor} supplies the common filesystem behavior:
 * recursive module discovery, optional concurrent module processing, include
 * matching with {@code glob:} or {@code regex:} patterns, exclusion rules,
 * non-recursive scans, and delegation to subclasses for each matching file.</p>
 *
 * <h2>AI file processing</h2>
 * <p>{@link AIFileProcessor} adds provider execution and project context. It
 * combines system instructions with prompts, supports YAML front matter, and
 * accepts {@code gw.model} and {@code enabledTools} prompt properties. Prompt
 * and instruction text can substitute public configuration values such as
 * {@code ${public.projectName}}. Lines beginning with {@code >>>} recursively
 * include UTF-8 content from HTTP(S) URLs or project-relative {@code file://}
 * references. In interactive mode, {@code .} terminates processing and {@code >}
 * accepts the current response. Each request also receives JSON metadata for the
 * project-relative file path, processing mode, and operating-system name.</p>
 *
 * <pre>{@code
 * AIFileProcessor processor = new AIFileProcessor(
 *     new java.io.File("."), configurator, "openai:gpt-4.1");
 * processor.setInstructions("Follow the project coding standards.");
 * processor.setDefaultPrompt(">>> file://docs/review.md");
 * processor.processFolder(projectLayout);
 * }</pre>
 *
 * <h2>Guidance processing</h2>
 * <p>{@link GuidanceProcessor} locates {@code @guidance:} comments in supported
 * file types. {@link org.machanism.machai.gw.reviewer.Reviewer} implementations
 * are discovered with {@link java.util.ServiceLoader}; a reviewer extracts the
 * mandatory instructions while retaining the marker in its original source
 * location. A configured default prompt can process supported files without an
 * explicit guidance block. Processing results are exposed as a report containing
 * relative file paths and provider messages through
 * {@link GuidanceProcessor#getReport()}.</p>
 *
 * <h2>Act workflows</h2>
 * <p>{@link ActProcessor} loads TOML acts from classpath resources, local
 * directories, explicit files, or HTTP(S) locations. Custom acts may inherit
 * another act with {@code basedOn} and {@code ${super.value}}. The {@code >}
 * shorthand creates an ad-hoc task; {@code public.prompt} supplies user prompt
 * text; {@code #} selects episodes, comma separates multiple selections, and
 * {@code !} stops normal-order continuation. Act model, instruction, input,
 * thread, exclusion, recursion, and interactive settings are applied to the
 * inherited configuration. Outputs are collected by
 * {@link ActProcessor#getResults()}.</p>
 *
 * <pre>{@code
 * ActProcessor acts = new ActProcessor(
 *     new java.io.File("."), "openai:gpt-4.1", configurator);
 * acts.setActsLocation("acts");
 * acts.setAct("review#1,3! Check concurrency and error handling");
 * acts.processFolder(projectLayout);
 * java.util.List<String> results = acts.getResults();
 * }</pre>
 *
 * <h2>Episodes and context</h2>
 * <p>{@link Episodes} stores ordered prompts, supports regular or selected-order
 * execution, repeats, moves by numeric ID or markdown heading, and exposes
 * serializable episode metadata. {@link ProjectContextKey} names the operating
 * system, project, parent-project, layout, source, test, documentation, and module
 * values registered for project-context tools. {@link GWConstants} centralizes
 * processor configuration keys, and {@link EpisodeNotFoundException} reports an
 * unresolved episode heading.</p>
 *
 * <p>{@link Ghostwriter} is the command-line entry point. Guidance mode is used
 * when no act is selected; act mode runs a named workflow. Embedders should
 * provide a configured {@code Configurator}, model/provider, and
 * {@link org.machanism.machai.project.layout.ProjectLayout}. Providers and
 * reviewers are supplied through the application's configuration and service
 * registrations.</p>
 */
package org.machanism.machai.gw.processor;
