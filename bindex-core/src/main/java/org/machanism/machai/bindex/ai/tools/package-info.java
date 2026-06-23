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
 * Provides AI function-tool integrations for discovering, retrieving, and registering
 * Bindex metadata records.
 *
 * <p>
 * This package exposes Bindex capabilities through the MachAI tool framework so that
 * generative-AI workflows can request structured library and project metadata at
 * runtime. The primary entry point is {@link org.machanism.machai.bindex.ai.tools.BindexFunctionTools},
 * which implements {@link org.machanism.machai.ai.tools.FunctionTools} and publishes
 * annotated tools for the following operations:
 * </p>
 *
 * <ul>
 *   <li>retrieving a Bindex record by identifier,</li>
 *   <li>selecting relevant libraries from a prompt and minimum relevance score,</li>
 *   <li>registering a Bindex record from a project-local JSON file, and</li>
 *   <li>registering a Bindex record supplied directly as a JSON object.</li>
 * </ul>
 *
 * <p>
 * Tool implementations use {@link org.machanism.machai.bindex.core.Picker} to access
 * Bindex persistence and recommendation behavior, and they rely on
 * {@link org.machanism.macha.core.commons.configurator.Configurator} for runtime
 * configuration such as default recommendation scores. The package is intended for
 * use by applications that wire MachAI tool providers into an LLM execution context,
 * enabling models to resolve additional dependency or artifact context without
 * hard-coding repository access in prompts.
 * </p>
 *
 * @since 0.0.2
 */
package org.machanism.machai.bindex.ai.tools;
