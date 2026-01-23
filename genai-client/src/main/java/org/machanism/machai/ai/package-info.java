/**
 * Provider-agnostic public API for the Machanism generative AI (GenAI) client.
 *
 * <p>This package contains the stable abstractions that application code should depend on when configuring and
 * using the Machanism GenAI client. Provider-specific integrations (for example, OpenAI or a remote/web
 * orchestrator) live in sub-packages so implementations can evolve independently of the core API.
 *
 * <h2>How to use this package</h2>
 * <p>Applications typically:
 * <ol>
 *   <li>Select or configure a provider implementation (often via {@code org.machanism.machai.ai.manager}).</li>
 *   <li>Interact with provider-neutral types defined in this package and its immediate sub-packages.</li>
 *   <li>Swap providers without changing application logic.</li>
 * </ol>
 *
 * <h2>Design principles</h2>
 * <ul>
 *   <li><b>Provider neutrality</b>: keep application integrations stable while changing provider implementations.</li>
 *   <li><b>Separation of concerns</b>: isolate authentication, transport, and provider configuration details.</li>
 *   <li><b>Composability</b>: support multiple providers and a no-op provider for disabled environments.</li>
 * </ul>
 *
 * <h2>Related sub-packages</h2>
 * <ul>
 *   <li>{@code org.machanism.machai.ai.manager} – provider discovery, lifecycle, and management utilities.</li>
 *   <li>{@code org.machanism.machai.ai.provider.none} – no-op provider (disables GenAI while preserving API compatibility).</li>
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
