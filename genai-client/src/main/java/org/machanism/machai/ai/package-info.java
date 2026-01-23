/**
 * Top-level public API for the Machanism AI generative AI client.
 *
 * <p>This package contains provider-agnostic abstractions for configuring and interacting with one or more
 * generative AI providers. Application code should generally depend on types in this package, while
 * provider-specific details are isolated to sub-packages.
 *
 * <h2>Key concepts</h2>
 * <ul>
 *   <li><b>Provider selection</b> – choose a concrete provider implementation (for example, OpenAI, a remote web
 *       orchestrator, or a no-op provider) that backs the client API.</li>
 *   <li><b>Provider-agnostic API</b> – keep application integrations stable while swapping provider implementations.</li>
 * </ul>
 *
 * <h2>Sub-packages</h2>
 * <ul>
 *   <li>{@code org.machanism.machai.ai.manager} – Service provider interface (SPI) for provider implementations and
 *       provider lifecycle/management utilities.</li>
 *   <li>{@code org.machanism.machai.ai.provider.none} – No-op provider implementation (disables GenAI while
 *       preserving API compatibility).</li>
 *   <li>{@code org.machanism.machai.ai.provider.openai} – OpenAI provider integration.</li>
 *   <li>{@code org.machanism.machai.ai.provider.web} – Provider implementation backed by a remote/web orchestrator.</li>
 * </ul>
 */
package org.machanism.machai.ai;

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
 * 		- Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */
