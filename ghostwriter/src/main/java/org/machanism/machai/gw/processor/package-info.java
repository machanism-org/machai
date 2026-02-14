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
 * - Use the Java Version Defined in `pom.xml`:
 *      - All code improvements and Javadoc updates must be compatible with the Java version `maven.compiler.release` specified in the project's `pom.xml`.
 *      - Do not use features or syntax that require a higher Java version than defined in `pom.xml`.
 */

/**
 * Project scanning and orchestration for Ghostwriter.
 *
 * <p>
 * This package contains the command-line entry point ({@link org.machanism.machai.gw.processor.Ghostwriter}) and the
 * {@link org.machanism.machai.gw.processor.FileProcessor} that traverse a project tree, identify supported files, and
 * dispatch each file to an appropriate {@link org.machanism.machai.gw.reviewer.Reviewer} implementation.
 * </p>
 *
 * <p>
 * Reviewers extract embedded {@code @guidance} directives from supported file formats. The extracted guidance, together
 * with optional system instructions and project metadata, forms the prompt sent to a configured
 * {@link org.machanism.machai.ai.manager.GenAIProvider}.
 * </p>
 *
 * <p>
 * For multi-module projects, modules are processed in a child-first order. Processing is traversal-based and does not
 * attempt to build the project or resolve inter-module dependencies.
 * </p>
 */
package org.machanism.machai.gw.processor;
