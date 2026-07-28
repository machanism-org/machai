/**
 * Provides the core Ghostwriter processing pipeline for applying AI-assisted
 * operations to project files, folders, guidance comments, and declarative act
 * templates.
 * <p>
 * The package centers on {@link org.machanism.machai.gw.processor.AIFileProcessor},
 * which prepares provider instructions, resolves prompt references, registers
 * project-layout context, and invokes configured GenAI providers. Specialized
 * processors build on that foundation:
 * </p>
 * <ul>
 * <li>{@link org.machanism.machai.gw.processor.GuidanceProcessor} scans supported
 * source files for {@code @guidance:} comment blocks, extracts their mandatory
 * instructions through reviewer implementations, and records per-file processing
 * reports.</li>
 * <li>{@link org.machanism.machai.gw.processor.ActProcessor} loads TOML-based act
 * definitions from built-in resources, local directories, or HTTP(S) locations,
 * applies inheritance and defaults, and executes configured prompt episodes.</li>
 * <li>{@link org.machanism.machai.gw.processor.Episodes} stores ordered act
 * prompts, supports explicit episode selection, regular execution order,
 * repeat requests, and jumps to episodes by identifier or heading name.</li>
 * </ul>
 * <h2>Supported prompt and processing features</h2>
 * <ul>
 * <li>Prompt include lines prefixed with {@code >>>}, resolving UTF-8 content
 * from {@code http://}, {@code https://}, or {@code file://} references.</li>
 * <li>YAML prompt front matter for options such as {@code gw.model} and
 * {@code enabledTools}.</li>
 * <li>Public-property substitution for prompt content while preserving runtime
 * placeholders such as {@code ${public.ai.model}} for the application
 * substitution layer.</li>
 * <li>Interactive commands: {@code >} to continue without an additional prompt
 * and {@code .} to terminate processing successfully.</li>
 * <li>Structured process metadata supplied to providers, including
 * {@code PROCESS_INFO.PROCESSED_FILE_REL_PATH} and {@code PROCESS_INFO.PROCESS_MODE}.</li>
 * <li>Act configuration markers including {@code $$super.value$$} for inherited
 * property merging, {@code #} for episode selection, {@code !} to stop after
 * selected episodes, and comma-separated episode lists.</li>
 * </ul>
 * <h2>Usage examples</h2>
 * <p>
 * Processing a project folder with a direct prompt:
 * </p>
 * <pre>{@code
 * Configurator configurator = ...;
 * ProjectLayout layout = ...;
 * AIFileProcessor processor = new AIFileProcessor(new File("."), configurator, "openai:gpt-4.1");
 * processor.setDefaultPrompt("Summarize the project structure and key modules.");
 * processor.processFolder(layout);
 * }</pre>
 * <p>
 * Running guidance-based file processing:
 * </p>
 * <pre>{@code
 * GuidanceProcessor processor = new GuidanceProcessor(new File("."), "openai:gpt-4.1", configurator);
 * processor.scanDocuments(layout.getProjectDir(), "glob:**&#47;*.java");
 * List<Map<String, Object>> report = processor.getReport();
 * }</pre>
 * <p>
 * Executing selected act episodes and stopping after them:
 * </p>
 * <pre>{@code
 * ActProcessor processor = new ActProcessor(new File("."), "openai:gpt-4.1", configurator);
 * processor.setAct("refactor#1,3! Improve null-safety and documentation.");
 * processor.processFolder(layout);
 * List<String> results = processor.getResults();
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
