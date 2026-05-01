/**
 * Ghostwriter processors and command-line orchestration.
 *
 * <p>
 * This package contains the filesystem-oriented processing pipeline used by the Ghostwriter CLI.
 * It includes the command-line entry point, base traversal support for single-module and
 * multi-module repositories, AI-backed processors for guidance-driven and act-driven workflows,
 * and shared constants and exceptions required to configure and execute scans.
 * </p>
 *
 * <p>
 * Processing in this package is rooted in {@link org.machanism.machai.project.layout.ProjectLayout}
 * metadata. Implementations inspect project directories, derive source, test, documentation, and
 * module structure from the detected layout, select matching files or directories, and assemble
 * prompts that are sent to a configured
 * {@link org.machanism.machai.ai.provider.Genai} provider. The package operates directly on the
 * filesystem and discovered project metadata rather than building the project or resolving its
 * dependency graph.
 * </p>
 *
 * <h2>Primary processing types</h2>
 * <ul>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.AbstractFileProcessor} provides shared traversal,
 *     module recursion, path filtering, exclusion handling, and scan-pattern support for concrete
 *     processors.
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.AIFileProcessor} adds prompt construction, provider
 *     initialization, request-input logging, optional interactive execution, and function-tool
 *     integration.
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.GuidanceProcessor} extracts embedded
 *     {@code @guidance:} directives from supported files through registered
 *     {@link org.machanism.machai.gw.reviewer.Reviewer} implementations and submits the resulting
 *     work to the configured AI provider.
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.ActProcessor} executes reusable TOML-defined acts,
 *     including inherited act definitions, external act locations, prompt episodes, and
 *     act-specific runtime configuration.
 *   </li>
 * </ul>
 *
 * <h2>Supporting types</h2>
 * <ul>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.Ghostwriter} is the CLI bootstrap class that parses
 *     command-line arguments, loads configuration, selects the processing mode, and starts scans.
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.GWConstants} centralizes property names and related
 *     constants shared across the processing pipeline.
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.ActNotFound} reports that a requested act could not
 *     be resolved from bundled or external act locations.
 *   </li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <p>
 * A Ghostwriter invocation creates either a
 * {@link org.machanism.machai.gw.processor.GuidanceProcessor} or an
 * {@link org.machanism.machai.gw.processor.ActProcessor}, applies configuration such as model
 * selection, instructions, excludes, interactive mode, and scan targets, and then scans one or
 * more project-relative paths or path patterns. Matching files are processed individually, while
 * project-level context is injected into prompts so downstream AI providers can make
 * repository-aware decisions.
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
