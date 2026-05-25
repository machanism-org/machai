/*-
 * @guidance:
 * **IMPORTANT: ADD OR UPDATE JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
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
 * MongoDB-backed Bindex persistence, semantic search, and AI tool integrations.
 *
 * <p>
 * This package provides the core infrastructure used to store, retrieve, and
 * semantically discover {@link org.machanism.machai.schema.Bindex} metadata.
 * It combines repository-style MongoDB access with GenAI-driven classification
 * and embedding workflows so application requirements can be translated into
 * relevant Bindex matches.
 *
 * <p>
 * The package centers on two primary responsibilities:
 *
 * <ul>
 * <li><strong>Repository access</strong> through
 * {@link org.machanism.machai.bindex.BindexRepository}, which reads and writes
 * serialized Bindex payloads stored in MongoDB documents.</li>
 * <li><strong>Selection and registration workflows</strong> through
 * {@link org.machanism.machai.bindex.Picker}, which classifies free-text
 * requests, generates embeddings, performs vector search, and registers new
 * Bindex entries enriched with searchable metadata.</li>
 * </ul>
 *
 * <p>
 * Subpackages extend these capabilities for AI-assisted usage scenarios,
 * including function-tool adapters that expose Bindex retrieval and library
 * recommendation operations to supported GenAI providers.
 */
package org.machanism.machai.bindex;
