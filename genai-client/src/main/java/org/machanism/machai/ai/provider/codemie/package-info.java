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
 * {@link org.machanism.machai.ai.manager.GenAIProvider} implementation that obtains an OAuth 2.0 access token from an
 * OpenID Connect (OIDC) token endpoint and then configures an OpenAI-compatible backend to call the CodeMie Code Assistant
 * REST API.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Authenticate using either a password grant (end-user) or client credentials (service-to-service).</li>
 *   <li>Populate configuration keys used by the downstream OpenAI-compatible client:</li>
 * </ul>
 * <ul>
 *   <li>{@code OPENAI_BASE_URL} – the CodeMie API base URL.</li>
 *   <li>{@code OPENAI_API_KEY} – the retrieved OAuth 2.0 access token.</li>
 * </ul>
 *
 * <h2>Model routing</h2>
 * <p>After configuring authentication, the provider delegates to an underlying provider based on the {@code chatModel}
 * prefix:</p>
 * <ul>
 *   <li>{@code gpt-*} (or blank/unspecified) – {@link org.machanism.machai.ai.provider.openai.OpenAIProvider}</li>
 *   <li>{@code gemini-*} – {@link org.machanism.machai.ai.provider.gemini.GeminiProvider}</li>
 *   <li>{@code claude-*} – {@link org.machanism.machai.ai.provider.claude.ClaudeProvider}</li>
 * </ul>
 *
 * <h2>Grant selection</h2>
 * <p>The OAuth 2.0 grant type is inferred from {@code GENAI_USERNAME}:</p>
 * <ul>
 *   <li><b>Password grant</b> when the username contains {@code "@"} (typical e-mail login).</li>
 *   <li><b>Client credentials</b> otherwise (service-to-service).</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <ul>
 *   <li>{@code GENAI_USERNAME} – user e-mail or client id.</li>
 *   <li>{@code GENAI_PASSWORD} – password or client secret.</li>
 *   <li>{@code chatModel} – model identifier (for example {@code gpt-4o-mini}).</li>
 *   <li>{@code AUTH_URL} (optional) – token endpoint override.</li>
 * </ul>
 */
package org.machanism.machai.ai.provider.codemie;
