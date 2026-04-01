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
 * MongoDB-backed persistence and semantic retrieval for {@code Bindex} metadata.
 *
 * <p>
 * This package provides the core components for:
 *
 * <ul>
 * <li>Persisting and retrieving {@code Bindex} records stored as JSON inside MongoDB documents</li>
 * <li>Registering Bindexes enriched with classification facets and an embedding vector</li>
 * <li>Performing semantic search by embedding a classified query and using MongoDB vector search</li>
 * </ul>
 *
 * <p>
 * The main entry points are:
 *
 * <ul>
 * <li>{@link org.machanism.machai.bindex.Picker} for registering and searching Bindexes</li>
 * <li>{@link org.machanism.machai.bindex.BindexRepository} for basic CRUD-style access to stored Bindexes</li>
 * </ul>
 */
package org.machanism.machai.bindex;
