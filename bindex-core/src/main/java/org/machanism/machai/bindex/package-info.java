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
 */

/**
 * Public API for generating, persisting, registering, and selecting a <em>Bindex</em>: a machine-consumable JSON
 * index of a software project used by MachAI workflows.
 *
 * <p>The package defines a small pipeline:
 * <ol>
 *   <li><strong>Create/update</strong> a {@code bindex.json} file for a project directory.</li>
 *   <li><strong>Register</strong> the Bindex in a backing store (MongoDB with vector search) to enable semantic lookup.</li>
 *   <li><strong>Pick</strong> relevant Bindexes using semantic search.</li>
 *   <li><strong>Assemble</strong> one or more selected Bindexes into LLM inputs for downstream execution.</li>
 * </ol>
 *
 * <h2>Main types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.BindexCreator}: creates or updates {@code bindex.json} for a
 *       {@link org.machanism.machai.project.layout.ProjectLayout}.</li>
 *   <li>{@link org.machanism.machai.bindex.BindexRegister}: registers an on-disk Bindex in the vector-backed store.</li>
 *   <li>{@link org.machanism.machai.bindex.Picker}: performs semantic search, fetches matched Bindex documents, and
 *       expands transitive dependencies.</li>
 *   <li>{@link org.machanism.machai.bindex.ApplicationAssembly}: composes prompts that include selected Bindex content.</li>
 *   <li>{@link org.machanism.machai.bindex.BindexBuilderFactory}: selects a suitable
 *       {@link org.machanism.machai.bindex.builder.BindexBuilder} for a given project layout.</li>
 * </ul>
 *
 * <h2>Typical workflow</h2>
 * <pre>{@code
 * GenAIProvider provider = ...;
 * ProjectLayout layout = ...;
 * String dbUri = ...; // MongoDB connection string, optional depending on environment
 *
 * // 1) Create/update bindex.json
 * new BindexCreator(provider)
 *     .update(true)
 *     .processFolder(layout);
 *
 * // 2) Register bindex.json for semantic search
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
