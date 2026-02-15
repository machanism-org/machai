/*-
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE CURRENT FILE PARENT FOLDER AND THIS `package-info.java`!**.	
 * Ghostwriter works with **all types of project files—including source code, documentation, project site content, and other relevant files**.
 * Ensure that your content generation and documentation efforts consider the full range of file types present in the project.
 * FileProcessor does not support dependency resolution during project processing. 
 * It processes projects using a child-first approach only.
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
 *      - All code improvements and Javadoc updates must be compatible with the Java version `maven.compiler.release` specified in the project's `pom.xml`.
 *      - Do not use features or syntax that require a higher Java version than defined in `pom.xml`.
 */

/**
 * Project scanning and prompt orchestration for Ghostwriter.
 *
 * <p>
 * This package contains the command-line entry point ({@link org.machanism.machai.gw.processor.Ghostwriter}) and the
 * {@link org.machanism.machai.gw.processor.FileProcessor} responsible for traversing a project tree, selecting files to
 * process, and invoking a {@link org.machanism.machai.gw.reviewer.Reviewer} per supported file type.
 * </p>
 *
 * <p>
 * Scanning is traversal-based and intentionally avoids dependency resolution and project builds. When a project layout
 * defines nested modules, modules are processed in a child-first order (modules first, then parent-directory files).
 * </p>
 *
 * <h2>Processing flow</h2>
 * <ol>
 * <li>Discover modules from the active {@link org.machanism.machai.project.layout.ProjectLayout}.</li>
 * <li>Traverse files and directories, applying exclude rules and optional scan patterns.</li>
 * <li>For each supported file, extract embedded directives and compose the full prompt.</li>
 * <li>Invoke the configured {@link org.machanism.machai.ai.manager.GenAIProvider} to generate output.</li>
 * </ol>
 */
package org.machanism.machai.gw.processor;
