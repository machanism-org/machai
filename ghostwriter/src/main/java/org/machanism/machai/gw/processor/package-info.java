
/*-
 * @guidance:
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Describe all supported features based on javadoc information from:
 *      	- AIFileProcessor
 *			- GuidanceProcessor
 * 			- ActProcessor
 *			- Episodes
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
 * Provides project-aware processors that use configured generative-AI providers to execute prompts, named acts,
 * and inline guidance instructions against source files, folders, and project layouts.
 *
 * <p>
 * This package is the core processing layer for Ghostwriter-style automation. It combines project layout metadata,
 * configurable model selection, prompt preprocessing, runtime function tools, and execution reporting so callers can run
 * repeatable AI-assisted maintenance tasks over a project. The main entry points are:
 * </p>
 *
 * <ul>
 *   <li>{@link org.machanism.machai.gw.processor.AIFileProcessor}, the base AI-backed file processor. It supports
 *       project/folder/file processing, prompt YAML front matter, recursive include markers, public property substitution,
 *       interactive commands, function-tool registration, scan path matching, and process metadata injection.</li>
 *   <li>{@link org.machanism.machai.gw.processor.GuidanceProcessor}, a specialization that scans supported files for
 *       {@code @guidance:} comment blocks, extracts those mandatory instructions through registered reviewers, dispatches
 *       them to the AI provider, and records a per-file report.</li>
 *   <li>{@link org.machanism.machai.gw.processor.ActProcessor}, a specialization that loads TOML-based act definitions
 *       from built-in resources, local directories, explicit TOML files, or HTTP(S) locations. It supports ad-hoc task
 *       shorthand, inheritance, default properties, public prompt values, episode selection, stop markers, and result
 *       collection.</li>
 *   <li>{@link org.machanism.machai.gw.processor.Episodes}, the act episode coordinator. It stores ordered prompt
 *       episodes, supports regular and explicitly requested execution order, handles repeat and move-to-episode control
 *       flow, resolves heading-based episode names, and exposes act/episode metadata for prompts.</li>
 * </ul>
 *
 * <h2>Prompt and processing features</h2>
 * <p>
 * The processors support several marker and metadata conventions:
 * </p>
 * <ul>
 *   <li>Prompt include lines beginning with {@code >>>}, which may reference UTF-8 {@code http://}, {@code https://},
 *       or {@code file://} content. Included content is parsed recursively.</li>
 *   <li>Interactive commands {@code .} to exit successfully and {@code &gt;} to accept the current provider response and
 *       continue without another prompt.</li>
 *   <li>YAML front matter in prompts delimited by {@code ---}, including {@code gw.model} for per-prompt model selection
 *       and {@code enabledTools} for restricting the provider toolset.</li>
 *   <li>Public configuration placeholders with the {@code public.} prefix, such as {@code ${public.projectName}}, which
 *       are substituted in prompts at runtime.</li>
 *   <li>Process metadata injected as JSON, including {@code PROCESSED_FILE_REL_PATH} and {@code PROCESS_MODE}.</li>
 * </ul>
 *
 * <h2>Act features</h2>
 * <p>
 * Acts are TOML templates that can define instructions, inputs, model settings, defaults, and other runtime properties.
 * Act names may include episode selection with {@code #}, multiple episode identifiers separated by {@code ,}, and the
 * stop suffix {@code !}. A command beginning with {@code &gt;} is treated as a shorthand ad-hoc task. Act definitions may
 * inherit other definitions through {@code basedOn}; inherited values can be merged with {@code ${super.value}}.
 * </p>
 *
 * <h2>Usage examples</h2>
 * <pre>{@code
 * Configurator configurator = ...;
 * ProjectLayout layout = ...;
 * File projectDir = new File(".");
 *
 * GuidanceProcessor guidance = new GuidanceProcessor(projectDir, "openai:gpt-4.1", configurator);
 * guidance.scanDocuments(projectDir, "glob:**&#47;*.java");
 * List<Map<String, Object>> report = guidance.getReport();
 * }</pre>
 *
 * <pre>{@code
 * ActProcessor acts = new ActProcessor(new File("."), "openai:gpt-4.1", configurator);
 * acts.setActsLocation("acts");
 * acts.setAct("review#1,3! Check concurrency and error handling");
 * acts.processFolder(layout);
 * List<String> results = acts.getResults();
 * }</pre>
 *
 * <pre>{@code
 * AIFileProcessor processor = new AIFileProcessor(new File("."), configurator, "openai:gpt-4.1");
 * processor.setInstructions("Follow the project coding standards.");
 * processor.setDefaultPrompt("Review the project using >>> file://docs/checklist.md");
 * processor.processFolder(layout);
 * }</pre>
 *
 * <p>
 * Supporting package types provide shared constants, exceptions, project-context keys, and base scanning behavior used by
 * these processors.
 * </p>
 */
package org.machanism.machai.gw.processor;
