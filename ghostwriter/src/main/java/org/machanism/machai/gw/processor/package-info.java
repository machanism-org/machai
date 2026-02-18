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
 * Provides the Ghostwriter command-line application entry point and the core processor that
 * traverses project directories, extracts per-file {@code @guidance} directives, and delegates
 * the resulting prompts to a configured GenAI provider.
 *
 * <p>
 * The primary entry point is {@link org.machanism.machai.gw.processor.Ghostwriter}, which parses CLI options,
 * loads configuration, and creates a {@link org.machanism.machai.gw.processor.FileProcessor} instance.
 * The {@code FileProcessor} walks a project tree (including multi-module layouts) using a child-first traversal
 * strategy and invokes a configured {@link org.machanism.machai.ai.manager.GenAIProvider} with composed prompts.
 * </p>
 *
 * <h2>Key Concepts</h2>
 * <ul>
 * <li><b>Reviewer-based extraction</b>: Guidance is extracted per file by
 * {@link org.machanism.machai.gw.reviewer.Reviewer} implementations selected by file extension.</li>
 * <li><b>Project-layout awareness</b>: Traversal honors {@link org.machanism.machai.project.layout.ProjectLayout}
 * metadata for sources, tests, documents, and modules.</li>
 * <li><b>No dependency resolution</b>: Scanning is filesystem-based and does not run builds or resolve dependencies.</li>
 * </ul>
 *
 * <h2>Typical Usage</h2>
 * <p>Run from the command line:</p>
 * <pre>{@code
 * java -jar gw.jar . --genai OpenAI:gpt-5-mini --threads true
 * }</pre>
 */
package org.machanism.machai.gw.processor;
