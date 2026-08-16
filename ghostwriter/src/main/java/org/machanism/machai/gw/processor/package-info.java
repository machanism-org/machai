
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
 * Provides the project-aware processing layer for Ghostwriter, a GenAI-assisted
 * tool that scans project layouts and applies prompts to files, folders, and
 * modules.
 *
 * <p>The package separates filesystem traversal from provider interaction while
 * retaining project context for every operation. {@link AbstractFileProcessor}
 * supplies recursive discovery, module traversal, concurrency, include and
 * exclude matching, and glob or regular-expression path selection. Its AI-aware
 * subclass {@link AIFileProcessor} prepares prompts, resolves configuration, and
 * invokes a configured {@code Genai} provider.</p>
 *
 * <h2>Primary processors</h2>
 * <ul>
 *   <li>{@link AIFileProcessor} processes a file or project folder with system
 *       instructions and one or more prompts. It supports YAML front matter,
 *       per-prompt {@code gw.model} selection, {@code enabledTools}, public
 *       property substitution such as {@code ${public.projectName}}, recursive
 *       external includes beginning with {@code >>>}, custom function tools,
 *       interactive commands {@code .} and {@code &gt;}, and JSON process metadata
 *       containing the relative file path, processing mode, and operating-system
 *       name.</li>
 *   <li>{@link GuidanceProcessor} discovers {@code @guidance:} comments in
 *       reviewer-supported file types. Reviewers are loaded with Java's
 *       {@link java.util.ServiceLoader}; matching guidance is sent with the
 *       configured rules, and a report records each processed relative path and
 *       provider message. A default prompt can also process matching files that
 *       contain no explicit guidance.</li>
 *   <li>{@link ActProcessor} loads TOML acts from classpath resources, local
 *       directories, explicit files, or HTTP(S) locations. It supports the
 *       {@code >} ad-hoc task shorthand, default properties, public prompt
 *       values, model and processor settings, {@code basedOn} inheritance with
 *       {@code ${super.value}}, episode selection using {@code #} and comma
 *       separators, and the {@code !} stop suffix. Results are available through
 *       {@link ActProcessor#getResults()}.</li>
 * </ul>
 *
 * <h2>Episodes and shared context</h2>
 * <p>{@link Episodes} stores ordered act prompts and can execute them in regular
 * order or in an explicitly selected order. It supports repeat and move requests,
 * resolves heading-based episode names, and exposes serializable act and episode
 * metadata. {@link ProjectContextKey} identifies operating-system, project,
 * parent-project, layout, source, test, documentation, and module values made
 * available to project-context tools. {@link GWConstants} centralizes processor
 * configuration keys, while {@link EpisodeNotFoundException} identifies an
 * unresolved named episode.</p>
 *
 * <h2>Command-line usage</h2>
 * <p>{@link Ghostwriter} is the command-line entry point. Without an act option it
 * uses guidance processing; act mode runs a named TOML workflow. Paths may be
 * directories, files, {@code glob:} patterns, or {@code regex:} patterns.</p>
 *
 * <pre>{@code
 * Configurator configurator = ...;
 * File projectDir = new File(".");
 * ProjectLayout layout = ...;
 *
 * GuidanceProcessor guidance = new GuidanceProcessor(projectDir, "openai:gpt-4.1", configurator);
 * guidance.setInstructions("Follow the project coding standards.");
 * guidance.scanDocuments(projectDir, {@code "glob:**&#47;*.java"});
 * {@code List<Map<String, Object>> report = guidance.getReport();}
 * }</pre>
 *
 * <pre>{@code
 * ActProcessor acts = new ActProcessor(projectDir, "openai:gpt-4.1", configurator);
 * acts.setActsLocation("acts");
 * acts.setAct("review#1,3! Check concurrency and error handling");
 * acts.processFolder(layout);
 * {@code List<String> results = acts.getResults();}
 * }</pre>
 *
 * <p>Applications embedding the package should provide a configured model/provider
 * and a {@link ProjectLayout}; providers and reviewer implementations are supplied
 * through the project's configuration and service registrations.</p>
 */
package org.machanism.machai.gw.processor;
