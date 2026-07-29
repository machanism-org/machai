/**
 * Provides the core processing infrastructure for MachAI gateway workflows that
 * use generative AI providers to inspect, transform, document, and coordinate
 * work across project files.
 *
 * <p>
 * The package centers on {@link org.machanism.machai.gw.processor.AIFileProcessor},
 * a reusable AI-backed file processor that builds provider requests from project
 * layout metadata, system instructions, user prompts, public configuration
 * values, prompt front matter, included prompt fragments, and registered
 * function tools. Higher-level processors extend this base behavior to support
 * guidance-driven source updates and declarative multi-step acts.
 * </p>
 *
 * <h2>Supported processing modes</h2>
 * <ul>
 *   <li><b>General AI file and folder processing</b> through
 *       {@link org.machanism.machai.gw.processor.AIFileProcessor}. The processor
 *       can process individual files, project roots, folders, or files selected
 *       with directory paths, {@code glob:} patterns, or {@code regex:} patterns.</li>
 *   <li><b>Inline guidance processing</b> through
 *       {@link org.machanism.machai.gw.processor.GuidanceProcessor}. Supported
 *       source files are reviewed for {@code @guidance:} comments, and the
 *       extracted mandatory instructions are sent to the configured provider while
 *       preserving guidance markers in their original locations.</li>
 *   <li><b>Act execution</b> through
 *       {@link org.machanism.machai.gw.processor.ActProcessor}. Acts are TOML
 *       definitions that configure prompts, model and runtime properties,
 *       inheritance, episode selection, and ordered multi-step execution.</li>
 *   <li><b>Episode orchestration</b> through
 *       {@link org.machanism.machai.gw.processor.Episodes}. Episodes maintain an
 *       ordered list of act prompts and support normal execution order, explicit
 *       episode selection, repeated episodes, and jumps to another episode by ID
 *       or heading name.</li>
 * </ul>
 *
 * <h2>Prompt markers and special commands</h2>
 * <ul>
 *   <li>{@code >>>} includes external prompt content. Supported include targets
 *       are {@code http://}, {@code https://}, and {@code file://}; included
 *       content is read as UTF-8 and may itself contain further include markers.</li>
 *   <li>{@code .} exits interactive processing with a successful termination
 *       code.</li>
 *   <li>{@code >} accepts the current provider response in interactive mode and
 *       continues without submitting an additional user prompt.</li>
 *   <li>{@code enabledTools} can be supplied in prompt YAML front matter to
 *       restrict the provider tools available for the current request.</li>
 *   <li>{@code public.} configuration properties are exposed for prompt template
 *       substitution, such as <code>${public.projectName}</code>.</li>
 * </ul>
 *
 * <h2>Prompt front matter and process metadata</h2>
 * <p>
 * Prompts may start with YAML front matter delimited by {@code ---}. Supported
 * input parameters include {@code gw.model}, which overrides the provider/model
 * for the request, and {@code enabledTools}, which may be a scalar or YAML list
 * of tool names. Each request also receives a JSON {@code PROCESS_INFO} block
 * containing the processed file path relative to the project directory and the
 * current mode, either {@code INTERACTIVE} or {@code NOT-INTERACTIVE}.
 * </p>
 *
 * <h2>Acts and episodes</h2>
 * <p>
 * Act processing loads TOML definitions from built-in classpath resources under
 * {@code /acts/}, local act directories, direct TOML file references, or remote
 * HTTP/HTTPS locations. Act definitions may inherit from another act with
 * {@code basedOn}; child values can incorporate inherited values with
 * {@code $$super.value$$}. Act names can include an episode selector using
 * {@code #}, comma-separated IDs, and a trailing {@code !} to stop after the
 * requested episodes, for example {@code refactor#1,2!}.
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
