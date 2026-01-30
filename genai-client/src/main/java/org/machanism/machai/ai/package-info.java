/**
 * Provider-agnostic public API root for the Machanism generative AI (GenAI) client.
 *
 * <p>This package is the stable entry point for integrating GenAI into application code while remaining independent
 * of any specific GenAI vendor or transport. Application code should rely on the provider-neutral abstractions exposed
 * by this package and its immediate sub-packages, so provider implementations can be swapped without changing
 * business logic.
 *
 * <h2>How it fits together</h2>
 * <ul>
 *   <li><b>Provider implementations</b> live under {@code org.machanism.machai.ai.provider}.</li>
 *   <li><b>Provider selection and lifecycle</b> utilities live under {@code org.machanism.machai.ai.manager}.
 *       These APIs discover available providers and expose the chosen provider through a common interface.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <ol>
 *   <li>Choose and configure a provider (for example, OpenAI, a web/orchestrator-backed provider, or a no-op
 *       provider for disabled environments).</li>
 *   <li>Use {@code org.machanism.machai.ai.manager.GenAIProviderManager} to initialize the provider and obtain a
 *       {@code org.machanism.machai.ai.manager.GenAIProvider} instance.</li>
 *   <li>Execute provider-neutral operations through the {@code GenAIProvider} interface.</li>
 * </ol>
 *
 * <h2>Related sub-packages</h2>
 * <ul>
 *   <li>{@code org.machanism.machai.ai.manager} – provider discovery, lifecycle, and management utilities.</li>
 *   <li>{@code org.machanism.machai.ai.provider.none} – no-op provider (disables GenAI while preserving API
 *       compatibility).</li>
 *   <li>{@code org.machanism.machai.ai.provider.openai} – OpenAI provider integration.</li>
 *   <li>{@code org.machanism.machai.ai.provider.web} – provider backed by a remote/web orchestrator.</li>
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
