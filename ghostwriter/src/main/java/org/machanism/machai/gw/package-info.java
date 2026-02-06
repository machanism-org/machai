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
 * - Use the Java Version Defined in `pom.xml`:
 *      - All code improvements and Javadoc updates must be compatible with the Java version specified in the project's `pom.xml`.
 *      - Do not use features or syntax that require a higher Java version than defined in `pom.xml`.
 */
/**
 * Command-line orchestration and workspace scanning for Ghostwriter.
 *
 * <p>
 * This package provides the CLI entry point ({@link org.machanism.machai.gw.Ghostwriter}) and the core processing
 * engine ({@link org.machanism.machai.gw.FileProcessor}) used to traverse a project workspace, extract embedded
 * {@code @guidance:} blocks from supported files, and submit per-file prompts to a configured GenAI provider.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Parse runtime parameters (root directory, scan patterns, excludes, provider/model, instructions).</li>
 *   <li>Discover modules and standard source/test/doc directories via {@code ProjectLayout}.</li>
 *   <li>Traverse the filesystem, applying include matchers and excludes.</li>
 *   <li>Delegate file parsing to {@code Reviewer} implementations loaded via {@code ServiceLoader}.</li>
 *   <li>Compose provider inputs (system instructions, OS and project layout context, file guidance, output format).</li>
 *   <li>Optionally log composed provider inputs for traceability.</li>
 * </ul>
 */
package org.machanism.machai.gw;
