/**
 * Provides the Ghostwriter reviewer subsystem.
 *
 * <p>This package defines the {@link org.machanism.machai.gw.reviewer.Reviewer} service-provider interface (SPI)
 * and concrete implementations for scanning project files and extracting embedded {@code @guidance} instructions.
 * These instructions are then formatted into normalized prompt fragments for downstream pipeline stages.
 *
 * <p>Each {@code Reviewer} typically targets a specific file type and understands that format's commenting
 * conventions (for example, {@code //} and {@code /* ... *\/} in Java, or {@code <!-- ... -->} in Markdown/HTML).
 * When guidance is found, the reviewer includes project-relative path context (via
 * {@code org.machanism.machai.project.layout.ProjectLayout}) so later stages can relate instructions to the
 * originating file or directory.
 */
package org.machanism.machai.gw.reviewer;

/*-
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
 * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 * 		- Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 * - Use the Java Version Defined in `pom.xml`:
 *     - All code improvements and Javadoc updates must be compatible with the Java version specified in the project's `pom.xml`.
 *     - Do not use features or syntax that require a higher Java version than defined in `pom.xml`.
 */
