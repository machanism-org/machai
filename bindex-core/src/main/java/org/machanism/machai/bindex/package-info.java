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

package org.machanism.machai.bindex;

/**
 * Provides the Bindex subsystem: creation, persistence, registration, and retrieval of a machine-consumable index
 * ({@code bindex.json}) for a software project used by MachAI workflows.
 *
 * <p>This package produces an on-disk {@code bindex.json} file for a
 * {@link org.machanism.machai.project.layout.ProjectLayout}. It can also register that file in a backing store
 * (MongoDB with vector search) so that relevant projects can be retrieved via semantic search.
 *
 * <h2>Main types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.BindexCreator}: creates or updates {@code bindex.json} for a project.</li>
 *   <li>{@link org.machanism.machai.bindex.BindexProjectProcessor}: shared utilities for locating and loading
 *       {@code bindex.json} during project processing.</li>
 *   <li>{@link org.machanism.machai.bindex.BindexRegister}: registers an on-disk Bindex with the backing store.</li>
 *   <li>{@link org.machanism.machai.bindex.Picker}: performs semantic search and dependency expansion to select Bindexes.</li>
 *   <li>{@link org.machanism.machai.bindex.ApplicationAssembly}: assembles selected Bindexes into inputs for prompt execution.</li>
 *   <li>{@link org.machanism.machai.bindex.BindexBuilderFactory}: selects a suitable
 *       {@link org.machanism.machai.bindex.builder.BindexBuilder} implementation for a given project layout.</li>
 * </ul>
 */
