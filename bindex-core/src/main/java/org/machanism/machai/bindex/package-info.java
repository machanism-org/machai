/**
 * Provides the public entry points for generating, assembling, selecting, and registering a Bindex (a project index)
 * used by Machanism-based AI workflows.
 *
 * <p>This package coordinates an end-to-end lifecycle:
 * <ol>
 *   <li>Build or update a {@code bindex.json} file for a project directory.</li>
 *   <li>Optionally register that Bindex document in a backing store (e.g., a vector database) for semantic lookup.</li>
 *   <li>Optionally perform semantic selection to retrieve relevant Bindex documents.</li>
 *   <li>Assemble one or more Bindexes into LLM prompt inputs.</li>
 * </ol>
 *
 * <h2>Main entry points</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.BindexCreator}: generates or updates {@code bindex.json} on disk for a
 *       project.</li>
 *   <li>{@link org.machanism.machai.bindex.BindexRegister}: registers Bindex documents in a backing store for later
 *       semantic retrieval.</li>
 *   <li>{@link org.machanism.machai.bindex.Picker}: performs semantic search and selection of Bindex documents.</li>
 *   <li>{@link org.machanism.machai.bindex.ApplicationAssembly}: assembles LLM inputs (prompts) that incorporate one or
 *       more Bindex documents.</li>
 * </ul>
 *
 * <h2>Builder selection</h2>
 * <p>{@link org.machanism.machai.bindex.BindexBuilderFactory} chooses a
 * {@link org.machanism.machai.bindex.builder.BindexBuilder} implementation based on the provided
 * {@link org.machanism.machai.project.layout.ProjectLayout}.
 *
 * <h2>Typical workflow</h2>
 * <pre>{@code
 * GenAIProvider provider = ...;
 * ProjectLayout layout = ...;
 *
 * // 1) Create/update bindex.json
 * new BindexCreator(provider)
 *     .update(true)
 *     .processFolder(layout);
 *
 * // 2) Optionally register the Bindex for semantic lookup
 * try (BindexRegister register = new BindexRegister(provider, dbUrl)) {
 *     register.update(true).processFolder(layout);
 * }
 *
 * // 3) Optionally select relevant Bindexes and assemble prompts
 * try (Picker picker = new Picker(provider, dbUrl)) {
 *     List<Bindex> relevant = picker.pick("how to configure logging?");
 *     new ApplicationAssembly(provider)
 *         .projectDir(layout.getProjectDir())
 *         .assembly("Create a config example", relevant);
 * }
 * }</pre>
 *
 * <p>For builder implementations and schema/prompt utilities, see {@link org.machanism.machai.bindex.builder}.
 */
package org.machanism.machai.bindex;

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
