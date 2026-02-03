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
 * Provides the Bindex subsystem: creation, persistence, registration, and retrieval of a machine-consumable index
 * ({@code bindex.json}) for a software project used by MachAI workflows.
 *
 * <p>This package centers around producing an on-disk {@code bindex.json} file for a
 * {@link org.machanism.machai.project.layout.ProjectLayout}, and then optionally registering that file in a backing
 * store (MongoDB with vector search) so that relevant projects can be retrieved via semantic search.
 *
 * <h2>Key responsibilities</h2>
 * <ul>
 *   <li><strong>Create/update</strong> a project's {@code bindex.json} representation.</li>
 *   <li><strong>Locate/load</strong> a {@link org.machanism.machai.schema.Bindex} from disk during project processing.</li>
 *   <li><strong>Register</strong> an on-disk Bindex into a searchable store.</li>
 *   <li><strong>Pick</strong> relevant Bindexes for a user query and expand dependencies.</li>
 *   <li><strong>Assemble</strong> selected Bindex content into LLM prompt inputs.</li>
 * </ul>
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
 *
 * <h2>Typical workflow</h2>
 * <pre>{@code
 * GenAIProvider provider = ...;
 * ProjectLayout layout = ...;
 * String dbUri = ...; // MongoDB connection string (optional depending on environment)
 *
 * // 1) Create or update bindex.json
 * new BindexCreator(provider)
 *     .update(true)
 *     .processFolder(layout);
 *
 * // 2) Register bindex.json for semantic retrieval
 * try (BindexRegister register = new BindexRegister(provider, dbUri)) {
 *     register.update(true).processFolder(layout);
 * }
 *
 * // 3) Pick relevant Bindexes and assemble prompt inputs
 * try (Picker picker = new Picker(provider, dbUri)) {
 *     List<Bindex> relevant = picker.pick("How do I configure logging?");
 *     new ApplicationAssembly(provider)
 *         .projectDir(layout.getProjectDir())
 *         .assembly("Generate a logging configuration example", relevant);
 * }
 * }</pre>
 *
 * <p>For builder implementations, see {@link org.machanism.machai.bindex.builder}.
 */
package org.machanism.machai.bindex;
