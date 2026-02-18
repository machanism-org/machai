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
 * APIs for generating, persisting, registering, and retrieving MachAI Bindex documents.
 *
 * <p>A Bindex is a JSON document (typically {@code bindex.json}) that describes a software project or library,
 * including identifying information (for example {@code name} and {@code version}), structured classification
 * (domains, layers, languages, and integrations), and dependency coordinates.
 *
 * <p>This package provides building blocks for these common activities:
 * <ul>
 *   <li>Read and write {@code bindex.json} in a project directory.</li>
 *   <li>Create or update a Bindex for a project by inspecting its layout and enriching the result using a
 *       {@link org.machanism.machai.ai.manager.GenAIProvider}.</li>
 *   <li>Register and look up Bindex documents in a MongoDB-backed registry.</li>
 *   <li>Semantically retrieve relevant Bindex documents for a user query using vector search over embeddings of
 *       classification data, and expand the result set with transitive dependencies.</li>
 *   <li>Assemble retrieved Bindex content into prompt inputs for downstream LLM-assisted workflows.</li>
 * </ul>
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.BindexCreator} generates or updates {@code bindex.json} for a project.</li>
 *   <li>{@link org.machanism.machai.bindex.BindexRegister} registers a Bindex in the backing store.</li>
 *   <li>{@link org.machanism.machai.bindex.Picker} performs semantic retrieval and dependency expansion.</li>
 *   <li>{@link org.machanism.machai.bindex.ApplicationAssembly} turns selected Bindexes into prompt inputs.</li>
 *   <li>{@link org.machanism.machai.bindex.BindexBuilderFactory} selects a builder implementation based on the
 *       detected {@link org.machanism.machai.project.layout.ProjectLayout}.</li>
 * </ul>
 *
 * <h2>Typical workflow</h2>
 * <ol>
 *   <li>Create or update {@code bindex.json} via {@link org.machanism.machai.bindex.BindexCreator}.</li>
 *   <li>Register the document via {@link org.machanism.machai.bindex.BindexRegister}.</li>
 *   <li>Retrieve relevant Bindexes for a query via {@link org.machanism.machai.bindex.Picker}.</li>
 *   <li>Assemble prompt-ready inputs via {@link org.machanism.machai.bindex.ApplicationAssembly}.</li>
 * </ol>
 */
package org.machanism.machai.bindex;
