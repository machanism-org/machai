/**
 * Ghostwriter CLI file-processing pipeline.
 *
 * <p>
 * This package contains the command-line entry point
 * ({@link org.machanism.machai.gw.processor.Ghostwriter}) together with the processor hierarchy used to scan a
 * project directory, resolve project-layout metadata, select matching files, load prompt content from embedded
 * {@code @guidance:} directives or TOML-based acts, and execute those prompts through a configured
 * {@link org.machanism.machai.ai.provider.Genai} provider.
 * </p>
 *
 * <h2>Processing model</h2>
 * <p>
 * Processing is filesystem-based only. The package operates on discovered files and directories without building the
 * project or resolving dependency graphs. Project structure information is obtained from
 * {@link org.machanism.machai.project.layout.ProjectLayout} and injected into prompts so downstream AI providers can
 * make decisions using repository context.
 * </p>
 *
 * <h2>Primary types</h2>
 * <ul>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.AbstractFileProcessor} provides traversal, module recursion,
 *     include/exclude matching, and shared scanning utilities.
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.AIFileProcessor} adds AI-provider integration, prompt assembly,
 *     request-input logging, optional interactive execution, and function-tool registration.
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.GuidanceProcessor} extracts embedded guidance from source files via
 *     registered {@link org.machanism.machai.gw.reviewer.Reviewer} implementations.
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.ActProcessor} loads and applies reusable act templates defined in
 *     TOML files, including inheritance and configurable runtime properties.
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.ActNotFound} signals that a requested act definition could not be
 *     resolved from either built-in or external act locations.
 *   </li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <p>
 * The {@link org.machanism.machai.gw.processor.Ghostwriter} CLI bootstraps configuration, creates either a
 * {@link org.machanism.machai.gw.processor.GuidanceProcessor} or
 * {@link org.machanism.machai.gw.processor.ActProcessor}, and then scans one or more directories or path patterns.
 * Individual processors invoke the configured AI provider for matching files and may optionally log request inputs or
 * run in interactive mode.
 * </p>
 */
package org.machanism.machai.gw.processor;
/*-
 * @guidance:
 *
 * **IMPORTANT: UPDATE OR ADD JAVADOC FOR ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
 *
 * - Update Existing Javadoc and Add Missing Javadoc:
 *      - Review all classes in the folder.
 *      - Update any existing Javadoc to ensure it is accurate, comprehensive, and follows best practices.
 *      - Add Javadoc to any classes, methods, or fields where it is missing.
 *      - Ensure that all Javadoc is up-to-date and provides clear, meaningful documentation.
 * - Use Clear and Concise Descriptions:
 *      - Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 *      - Avoid vague statements; be specific about functionality and intent.
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
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
 * - Use the Java Version Defined in `pom.xml`:
 *      - All code improvements and Javadoc updates must be compatible with the Java version specified in the project's `pom.xml`.
 *      - Do not use features or syntax that require a higher Java version than defined in `pom.xml`.
 */
