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
 * Supplies the AI-facing tools and support code for working with Bindex
 * (bundle index) metadata.
 * <p>
 * {@link BindexFunctionTools} exposes annotated operations for retrieving a
 * Bindex by coordinates or by a local or remote JSON location, recommending
 * libraries from a natural-language requirement, registering Bindex data, and
 * loading the Bindex schema and generation prompt resources. The tool methods
 * accept the application {@link org.machanism.macha.core.commons.configurator.Configurator}
 * so repository and picker operations use the caller's configuration.
 * </p>
 * <p>
 * {@link GraphqlJsonFilter} provides the JSON projection used by the retrieval
 * tool. A GraphQL selection document can limit the returned top-level fields;
 * nested selections are parsed but the current implementation does not recurse
 * into nested objects. For example, a query such as
 * {@code { name version classification { languages } }} selects the top-level
 * {@code name}, {@code version}, and {@code classification} fields.
 * </p>
 * <p>
 * A typical tool integration discovers the annotated methods through the
 * {@link org.machanism.machai.ai.tools.FunctionTools} contract, invokes
 * {@code get_bindex} with an identifier, and optionally supplies a selection
 * query when only a subset of metadata is needed. Registration operations
 * persist the descriptor through a configured
 * {@link org.machanism.machai.bindex.core.BindexRepository}.
 * </p>
 *
 * @see BindexFunctionTools
 * @see GraphqlJsonFilter
 * @see org.machanism.machai.bindex.core.BindexRepository
 * @see org.machanism.machai.bindex.core.Picker
 */
package org.machanism.machai.bindex.ai.tools;
