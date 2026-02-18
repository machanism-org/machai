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
 * <p>This package provides {@link org.machanism.machai.ai.provider.codemie.CodeMieProvider}, a
 * {@link org.machanism.machai.ai.manager.GenAIProvider} implementation that authenticates against
 * CodeMie (via an OpenID Connect token endpoint) and then delegates requests to an OpenAI-compatible
 * provider configured for the CodeMie Code Assistant API.
 *
 * <h2>Overview</h2>
 * <ul>
 *   <li>Retrieves an OAuth 2.0 access token from the configured token endpoint.</li>
 *   <li>Configures an underlying {@link org.machanism.machai.ai.provider.openai.OpenAIProvider} with the
 *       CodeMie base URL and the retrieved token (as {@code OPENAI_API_KEY}).</li>
 * </ul>
 *
 * <h2>Authentication</h2>
 * <p>The grant type is derived from the configured {@code GENAI_USERNAME} value:
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
 * conf.set("GENAI_USERNAME", "user@example.com");
 * conf.set("GENAI_PASSWORD", "...");
 * conf.set("chatModel", "gpt-4o-mini");
 *
 * CodeMieProvider provider = new CodeMieProvider();
 * provider.init(conf);
 *
 * // Use the provider via the GenAIProvider APIs.
 * }
 * </pre>
 */
package org.machanism.machai.ai.provider.codemie;
