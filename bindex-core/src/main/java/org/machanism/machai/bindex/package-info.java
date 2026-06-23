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
 * Provides MongoDB-backed Bindex registration, persistence, retrieval, and
 * semantic selection services.
 *
 * <p>
 * The package is responsible for managing {@link org.machanism.machai.schema.Bindex}
 * metadata records that describe projects, libraries, integrations, and their
 * classifications. Bindex records are stored in MongoDB as serialized JSON
 * payloads together with searchable fields such as logical id, name, version,
 * languages, layers, domains, integrations, and classification embeddings.
 * </p>
 *
 * <p>
 * {@link org.machanism.machai.bindex.MongoBindexRepository} provides direct
 * repository-style access for reading, deleting, and locating registered Bindex
 * records. It creates the MongoDB client from runtime configuration and exposes
 * the underlying collection for callers that need lower-level operations.
 * </p>
 *
 * <p>
 * {@link org.machanism.machai.bindex.Picker} builds on the repository collection
 * to register Bindex records and to recommend matching records for
 * natural-language requirements. It uses configured GenAI providers to classify
 * user requests, embedding providers to convert classifications into vector
 * representations, and MongoDB vector search to retrieve relevant Bindex
 * entries above a configurable score threshold.
 * </p>
 *
 * <p>
 * Related subpackages expose these capabilities to AI tool-calling workflows,
 * allowing supported GenAI integrations to fetch Bindex metadata, pick
 * libraries, and register new Bindex definitions from files or JSON objects.
 * </p>
 */
package org.machanism.machai.bindex;
