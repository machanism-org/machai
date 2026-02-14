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
 * APIs for creating, persisting, registering, and retrieving MachAI "Bindex" documents.
 *
 * <p>A Bindex is a JSON representation (typically {@code bindex.json}) describing a project/library, including
 * its metadata, classification, and dependencies. This package provides a workflow to:
 * <ul>
 *   <li>Generate or update {@code bindex.json} for a local project directory.</li>
 *   <li>Load an existing {@code bindex.json} during processing.</li>
 *   <li>Register Bindex documents into a MongoDB-backed store for later retrieval.</li>
 *   <li>Semantically select relevant Bindex documents for a user query using vector search over classification
 *       embeddings and include transitive dependencies.</li>
 *   <li>Assemble selected Bindex content into prompt inputs for downstream LLM-assisted workflows.</li>
 * </ul>
 *
 * <h2>Typical workflow</h2>
 * <ol>
 *   <li>Create or update {@code bindex.json} via {@link org.machanism.machai.bindex.BindexCreator}.</li>
 *   <li>Register the document via {@link org.machanism.machai.bindex.BindexRegister}.</li>
 *   <li>Retrieve relevant Bindexes for a query via {@link org.machanism.machai.bindex.Picker}.</li>
 *   <li>Assemble prompt-ready inputs via {@link org.machanism.machai.bindex.ApplicationAssembly}.</li>
 * </ol>
 *
 * <h2>Builder selection</h2>
 * {@link org.machanism.machai.bindex.BindexBuilderFactory} chooses an appropriate
 * {@link org.machanism.machai.bindex.builder.BindexBuilder} implementation based on the detected
 * {@link org.machanism.machai.project.layout.ProjectLayout} (for example Maven, Python, or JavaScript layouts).
 */
package org.machanism.machai.bindex;
