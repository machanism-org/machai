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
 * Provides the core repository and semantic-selection components for Bindex
 * metadata.
 *
 * <p>A Bindex is a structured description of a library, integration, or other
 * project component. This package stores those descriptions in MongoDB,
 * maintains the classification data used for searching, and selects relevant
 * entries from natural-language requirements.</p>
 *
 * <p>{@link BindexRepository} defines the persistence and similarity-search
 * contract. {@link MongoBindexRepository} implements that contract with a
 * MongoDB collection: it serializes each {@link org.machanism.machai.schema.Bindex}
 * as JSON, stores searchable metadata and an embedding vector, and retrieves or
 * removes records by Bindex identifier. {@link BindexInfo} is the lightweight
 * result model returned by searches, containing an identifier, version,
 * description, and relevance score.</p>
 *
 * <p>{@link Picker} coordinates registration and recommendation workflows. For
 * registration it creates an embedding from a Bindex classification and passes
 * the record to the repository. For recommendations it asks a configured
 * {@link org.machanism.machai.ai.provider.Genai} provider to classify a user
 * request, embeds that classification, and delegates filtered vector search to
 * the repository. The selected results are returned in descending relevance
 * order and can be resolved with their stored descriptions and dependencies.</p>
 *
 * <p>Typical callers construct a {@link MongoBindexRepository} with a
 * {@link org.machanism.macha.core.commons.configurator.Configurator}, pass it to
 * a {@link Picker}, and configure the repository and AI providers through that
 * configurator. Related tool-integration subpackages expose these operations to
 * AI tool-calling workflows.</p>
 *
 * @see BindexRepository
 * @see MongoBindexRepository
 * @see Picker
 */
package org.machanism.machai.bindex.core;
