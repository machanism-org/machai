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
 * Generates, persists, registers, and retrieves MachAI "Bindex" documents.
 *
 * <p>A Bindex is a JSON document (typically named {@code bindex.json}) that describes a software project or a
 * reusable library. It captures:
 * <ul>
 *   <li>Identity (for example {@code id}, {@code name}, {@code version})</li>
 *   <li>Classification data (domains, layers, languages, integrations) used for semantic retrieval</li>
 *   <li>Dependencies to other Bindexes to enable transitive expansion</li>
 * </ul>
 *
 * <p>The package supports both local project workflows (create/update {@code bindex.json}) and registry workflows
 * (store and search Bindexes in MongoDB).
 *
 * <h2>Main responsibilities</h2>
 * <ul>
 *   <li>Creation/update of {@code bindex.json} from project context via AI-assisted builders.</li>
 *   <li>Registration and lookup of Bindexes in a MongoDB-backed store.</li>
 *   <li>Semantic retrieval (vector search over classification embeddings) with dependency expansion.</li>
 *   <li>Assembly of retrieved Bindex content into prompt inputs for downstream LLM-assisted tasks.</li>
 * </ul>
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.BindexCreator} creates or updates {@code bindex.json} for a project.</li>
 *   <li>{@link org.machanism.machai.bindex.BindexBuilderFactory} selects a builder based on the detected
 *       {@link org.machanism.machai.project.layout.ProjectLayout}.</li>
 *   <li>{@link org.machanism.machai.bindex.BindexRegister} registers a local {@code bindex.json} in the registry.</li>
 *   <li>{@link org.machanism.machai.bindex.BindexRepository} provides MongoDB persistence primitives.</li>
 *   <li>{@link org.machanism.machai.bindex.Picker} performs semantic search and expands results via dependencies.</li>
 *   <li>{@link org.machanism.machai.bindex.ApplicationAssembly} builds prompt-ready inputs from selected Bindexes.</li>
 * </ul>
 *
 * <h2>Typical workflow</h2>
 * <ol>
 *   <li>Generate or update {@code bindex.json} using {@link org.machanism.machai.bindex.BindexCreator}.</li>
 *   <li>Register it via {@link org.machanism.machai.bindex.BindexRegister}.</li>
 *   <li>Retrieve relevant documents for a query via {@link org.machanism.machai.bindex.Picker}.</li>
 *   <li>Assemble prompt input using {@link org.machanism.machai.bindex.ApplicationAssembly}.</li>
 * </ol>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Configurator config = ...;
 * ProjectLayout layout = ...;
 *
 * // 1) Create/update bindex.json
 * new BindexCreator("openai", config).update(true).processFolder(layout);
 *
 * // 2) Register and 3) Retrieve
 * Picker picker = new Picker("openai", null, config);
 * List<Bindex> selected = picker.pick("Find libraries for server-side logging");
 *
 * // 4) Assemble prompt input for an LLM workflow
 * new ApplicationAssembly("openai", config, layout.getProjectDir())
 *     .assembly("Generate an example app using the selected libraries", selected);
 * }</pre>
 */
package org.machanism.machai.bindex;
