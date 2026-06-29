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
 * Provides AI-facing tool implementations for discovering, retrieving, filtering,
 * and registering Bindex metadata.
 * <p>
 * The package exposes annotated function tools that can be made available to AI
 * providers through the MachAI tool invocation infrastructure. These tools allow
 * an agent to recommend libraries from natural-language requirements, load a
 * {@link org.machanism.machai.schema.Bindex} descriptor by repository identifier
 * or URL, and register Bindex descriptors from local project files, remote URLs,
 * or structured JSON payloads.
 * </p>
 * <p>
 * This package also contains support utilities for reducing returned Bindex JSON
 * payloads with GraphQL-style field selection expressions. That filtering is
 * useful when tool callers need only selected metadata fields and want to reduce
 * response size for downstream language-model processing.
 * </p>
 *
 * @see org.machanism.machai.bindex.ai.tools.BindexFunctionTools
 * @see org.machanism.machai.bindex.ai.tools.GraphqlJsonFilter
 * @see org.machanism.machai.bindex.core.BindexRepository
 * @see org.machanism.machai.bindex.core.Picker
 */
package org.machanism.machai.bindex.ai.tools;
