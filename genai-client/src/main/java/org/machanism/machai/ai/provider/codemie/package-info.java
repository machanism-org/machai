/*-
 * @guidance:
 *
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
 *      - Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 *      - Do not use escaping in `{@code ...}` tags.    
 */

/**
 * Provides integration with the EPAM CodeMie Code Assistant platform.
 *
 * <p>This package contains a {@link org.machanism.machai.ai.provider.Genai} implementation that acquires OAuth 2.0
 * access tokens from the CodeMie OpenID Connect token endpoint, configures an OpenAI-compatible base URL and API key,
 * and delegates request execution to the appropriate downstream provider for the selected model family.</p>
 *
 * <h2>Supported model routing</h2>
 * <ul>
 * <li>Models whose identifiers start with {@code gpt-} are delegated to the OpenAI-compatible provider.</li>
 * <li>Models whose identifiers start with {@code gemini-} are delegated to the Gemini provider.</li>
 * <li>Models whose identifiers start with {@code claude-} are delegated to the Claude provider.</li>
 * </ul>
 *
 * <h2>Authentication behavior</h2>
 * <ul>
 * <li>If {@code GENAI_USERNAME} contains {@code @}, the provider uses the OAuth 2.0 password grant flow.</li>
 * <li>Otherwise, the provider uses the client credentials grant flow.</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <ul>
 * <li>{@code GENAI_USERNAME}: user e-mail address or client identifier.</li>
 * <li>{@code GENAI_PASSWORD}: password or client secret.</li>
 * <li>{@code chatModel}: target model identifier used to choose the downstream provider.</li>
 * <li>{@code AUTH_URL}: optional override for the default CodeMie token endpoint.</li>
 * </ul>
 */
package org.machanism.machai.ai.provider.codemie;
