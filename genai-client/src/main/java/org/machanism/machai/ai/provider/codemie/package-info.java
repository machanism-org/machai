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
 * EPAM CodeMie provider integration.
 *
 * <p>This package contains {@link org.machanism.machai.ai.provider.codemie.CodeMieProvider}, a
 * {@link org.machanism.machai.ai.manager.GenAIProvider} implementation that wraps an
 * {@link org.machanism.machai.ai.provider.openai.OpenAIProvider} to communicate with the CodeMie
 * Code Assistant API.
 *
 * <h2>Responsibilities</h2>
 * <ol>
 *   <li>Obtain an OAuth 2.0 access token from CodeMie's OpenID Connect token endpoint.</li>
 *   <li>Configure the underlying OpenAI-compatible client to use CodeMie's API base URL and the retrieved token.</li>
 * </ol>
 *
 * <h2>Authentication</h2>
 * <p>The provider selects the grant type based on {@code GENAI_USERNAME}:
 * <ul>
 *   <li><b>Password grant</b> when the username contains {@code "@"} (typical user e-mail login).</li>
 *   <li><b>Client credentials</b> when the username does not contain {@code "@"} (service-to-service).</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <ul>
 *   <li>{@code GENAI_USERNAME} – user e-mail or client id.</li>
 *   <li>{@code GENAI_PASSWORD} – password or client secret.</li>
 *   <li>{@code AUTH_URL} (optional) – token endpoint override; defaults to
 *       {@link org.machanism.machai.ai.provider.codemie.CodeMieProvider#authUrl}.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * CodeMieProvider provider = new CodeMieProvider();
 * provider.init(conf);
 *
 * // Continue using the provider via the GenAIProvider APIs.
 * }
 * </pre>
 */
package org.machanism.machai.ai.provider.codemie;
