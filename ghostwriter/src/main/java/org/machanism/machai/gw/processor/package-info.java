/**
 * Provides the AI-backed processing layer for MachAI gateway workflows.
 *
 * <p>
 * This package contains reusable processors that connect project-layout
 * information, prompt configuration, inline source guidance, declarative act
 * definitions, episode orchestration, provider selection, and function-tool
 * registration into a single workflow for inspecting or updating project files.
 * The central abstraction is {@link org.machanism.machai.gw.processor.AIFileProcessor},
 * which prepares requests for a configured generative AI provider and can be
 * specialized for source guidance or multi-step act execution.
 * </p>
 *
 * <h2>Core processors</h2>
 * <ul>
 *   <li><b>General AI file processing</b> is implemented by
 *       {@link org.machanism.machai.gw.processor.AIFileProcessor}. It processes
 *       individual files, project roots, folders, or scanned files selected by
 *       directory paths, {@code glob:} patterns, or {@code regex:} patterns. It
 *       builds provider requests from project context, system instructions,
 *       prompts, public configuration values, YAML prompt front matter, included
 *       prompt fragments, and registered function tools.</li>
 *   <li><b>Inline guidance processing</b> is implemented by
 *       {@link org.machanism.machai.gw.processor.GuidanceProcessor}. It scans
 *       supported files for {@code @guidance:} comments using registered
 *       reviewers, sends the extracted mandatory instructions to the provider,
 *       preserves the guidance markers in their original locations, optionally
 *       applies a default prompt to files without guidance, and records per-file
 *       results in a report.</li>
 *   <li><b>Declarative act execution</b> is implemented by
 *       {@link org.machanism.machai.gw.processor.ActProcessor}. It loads TOML act
 *       definitions from built-in resources, local act directories, direct TOML
 *       paths, or HTTP/HTTPS locations, applies defaults and inheritance, binds
 *       user prompt values, configures processor settings, and executes one or
 *       more prompts against the selected project scope.</li>
 *   <li><b>Episode orchestration</b> is implemented by
 *       {@link org.machanism.machai.gw.processor.Episodes}. It maintains ordered
 *       episode prompts, derives optional episode names from markdown headings,
 *       supports regular execution, explicit episode selection, repeated
 *       episodes, and jumps to another episode by numeric ID or heading name.</li>
 * </ul>
 *
 * <h2>Prompt markers, parameters, and runtime metadata</h2>
 * <ul>
 *   <li>{@code >>>} includes external prompt content. Supported include targets
 *       are {@code http://}, {@code https://}, and {@code file://}; included
 *       content is read as UTF-8 and may contain additional include markers.</li>
 *   <li>{@code .} exits interactive processing with a successful termination
 *       code.</li>
 *   <li>{@code >} accepts the current provider response in interactive mode and
 *       continues without submitting an additional user prompt. In act commands,
 *       it is also used as shorthand for an ad-hoc {@code task} act.</li>
 *   <li>{@code enabledTools} can be supplied in prompt YAML front matter as a
 *       scalar or list to restrict the provider tools available for the current
 *       request.</li>
 *   <li>{@code gw.model} can be supplied in prompt YAML front matter to override
 *       the provider/model for a request.</li>
 *   <li>{@code public.} configuration properties are exposed for prompt template
 *       substitution, such as <code>${public.projectName}</code>.</li>
 *   <li>Each provider request receives a JSON {@code PROCESS_INFO} block with
 *       {@code PROCESSED_FILE_REL_PATH} and {@code PROCESS_MODE}, where the mode
 *       is {@code INTERACTIVE} or {@code NOT-INTERACTIVE}.</li>
 * </ul>
 *
 * <h2>Acts and episodes</h2>
 * <p>
 * Act definitions use TOML and may declare {@code basedOn} inheritance. Child
 * values can incorporate inherited values with {@code $$super.value$$}. Default
 * values are read from the {@code default} section, and the user-facing prompt
 * is exposed as {@code public.prompt}. Act names may include an episode selector
 * after {@code #}; multiple episode IDs are separated with {@code ,}, and a
 * trailing {@code !} stops execution after the requested episodes. For example,
 * {@code refactor#1,3! Improve error handling} runs episodes 1 and 3 of the
 * {@code refactor} act and then stops normal-order execution.
 * </p>
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * AIFileProcessor processor = new AIFileProcessor(projectDir, configurator, "openai:gpt-4.1");
 * processor.setInstructions(">>> file://docs/system-instructions.md");
 * processor.setDefaultPrompt("Analyze ${public.projectName} and summarize required changes.");
 * processor.processFolder(projectLayout);
 * }</pre>
 *
 * <pre>{@code
 * GuidanceProcessor guidanceProcessor = new GuidanceProcessor(new File("."), "openai:gpt-4.1", configurator);
 * guidanceProcessor.scanDocuments(new File("."), "glob:src/main/java/**");
 * List<Map<String, Object>> report = guidanceProcessor.getReport();
 * }</pre>
 *
 * <pre>{@code
 * ActProcessor actProcessor = new ActProcessor(new File("."), "openai:gpt-4.1", configurator);
 * actProcessor.setActsLocation("acts");
 * actProcessor.setAct("refactor#1,2! Improve error handling in the service layer");
 * actProcessor.processFolder(projectLayout);
 * List<String> results = actProcessor.getResults();
 * }</pre>
 */
package org.machanism.machai.gw.processor;
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
