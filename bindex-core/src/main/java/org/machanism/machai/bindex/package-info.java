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
 *      - Use {@code {@literal <}} and {@code {@literal >}} to escape angle brackets in Javadoc.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */

/**
 * Bindex subsystem used by MachAI workflows to create, persist, register, and query project indexes.
 *
 * <p>A Bindex is typically persisted as {@code bindex.json} for a given
 * {@link org.machanism.machai.project.layout.ProjectLayout}. This package provides the orchestration and
 * supporting utilities needed to create that file, load it during processing, register it with a backing store,
 * and later retrieve and assemble relevant Bindexes as inputs to downstream GenAI execution.
 *
 * <h2>Typical workflow</h2>
 * <ol>
 *   <li>Create or update an on-disk {@code bindex.json} for a project.</li>
 *   <li>Register the Bindex with a backing store (for example, a document database and/or vector search index).</li>
 *   <li>Query and select relevant Bindexes using semantic search and optional dependency expansion.</li>
 *   <li>Assemble selected content into prompt-ready inputs for execution.</li>
 * </ol>
 *
 * <h2>Main types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.BindexCreator}: builds and writes {@code bindex.json} for a project.</li>
 *   <li>{@link org.machanism.machai.bindex.BindexProjectProcessor}: utilities for locating and loading
 *       {@code bindex.json} during project processing.</li>
 *   <li>{@link org.machanism.machai.bindex.BindexRegister}: registers an on-disk Bindex with a backing store.</li>
 *   <li>{@link org.machanism.machai.bindex.Picker}: performs semantic search and dependency expansion to select Bindexes.</li>
 *   <li>{@link org.machanism.machai.bindex.ApplicationAssembly}: assembles selected Bindexes into prompt inputs for execution.</li>
 *   <li>{@link org.machanism.machai.bindex.BindexBuilderFactory}: selects a suitable
 *       {@link org.machanism.machai.bindex.builder.BindexBuilder} implementation for a project layout.</li>
 * </ul>
 */
package org.machanism.machai.bindex;
