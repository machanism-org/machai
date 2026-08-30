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
 * Supplies persistence, semantic search, and recommendation services for Bindex
 * metadata.
 *
 * <p>A Bindex describes a library or integration, including its identity,
 * version, description, dependencies, and {@link
 * org.machanism.machai.schema.Classification} metadata. The types in this
 * package form the core workflow around that description:</p>
 *
 * <ul>
 * <li>{@link BindexRepository} defines operations for saving Bindex documents,
 * retrieving a document by its identifier, and finding relevant documents from
 * classification filters and an embedding vector.</li>
 * <li>{@link MongoBindexRepository} provides the repository implementation using
 * MongoDB. It stores the serialized Bindex together with searchable
 * classification fields and the classification embedding, and also exposes
 * document deletion and registration-identifier lookup.</li>
 * <li>{@link Picker} turns a natural-language request into one or more
 * classifications with a configured GenAI provider, creates an embedding, and
 * delegates semantic matching to a {@link BindexRepository}. It also registers
 * Bindex entries and recursively resolves their dependencies.</li>
 * <li>{@link BindexInfo} is the compact result model used for recommendations;
 * it contains the selected Bindex identifier, version, description, and
 * similarity score.</li>
 * </ul>
 *
 * <p>A typical integration configures a {@link
 * org.machanism.macha.core.commons.configurator.Configurator}, constructs a
 * {@link MongoBindexRepository} with it, and supplies that repository to a
 * {@link Picker}. The configurator selects the MongoDB connection and the GenAI
 * and embedding providers. {@code Picker#pick} uses the GenAI provider to
 * derive {@code Classification} filters from a request, creates an embedding,
 * and returns the matching Bindex summaries. Applications that create a
 * {@code MongoBindexRepository} must call {@link MongoBindexRepository#close()}
 * when it is no longer needed to release its MongoDB client resources.</p>
 *
 * <p>For example:</p>
 * <pre>
 * org.machanism.macha.core.commons.configurator.Configurator config = ...;
 * BindexRepository repository = new MongoBindexRepository(config);
 * Picker picker = new Picker(repository, config);
 * java.util.Collection&lt;BindexInfo&gt; matches =
 *     picker.pick(request, 20, 0.75, config);
 * </pre>
 *
 * @see BindexRepository
 * @see MongoBindexRepository
 * @see Picker
 * @see BindexInfo
 */
package org.machanism.machai.bindex.core;
