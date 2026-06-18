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
 * Provides the EPAM CodeMie integration for Machai generative AI clients.
 *
 * <p>This package contains the {@link org.machanism.machai.ai.provider.Genai}
 * provider implementation that authenticates against CodeMie OAuth 2.0 and
 * OpenID Connect token services, resolves the appropriate grant flow from the
 * supplied credentials, and delegates requests to provider-specific clients
 * configured for CodeMie-hosted endpoints.</p>
 *
 * <p>The package is responsible for obtaining access tokens, applying them to
 * downstream clients, and selecting a compatible delegated provider according to
 * the configured model family. OpenAI-compatible and embedding-capable models are
 * routed through the OpenAI provider implementation, while Anthropic-compatible
 * Claude models are routed through the Claude provider implementation.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 * <li>Reads CodeMie authentication and endpoint settings from the shared provider configuration.</li>
 * <li>Obtains bearer tokens from the configured token endpoint using password or client-credentials grants.</li>
 * <li>Configures delegated providers with CodeMie-specific base URLs and access tokens.</li>
 * <li>Exposes embedding support when the selected delegated provider implements embedding operations.</li>
 * <li>Validates configured model families and rejects unsupported CodeMie model identifiers.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <p>Applications supply a CodeMie username or client id, a password or client
 * secret, and a supported model identifier. The provider retrieves an access
 * token on demand and configures the delegated AI client so that subsequent chat
 * completion or embedding requests are sent through CodeMie-managed services.</p>
 */
package org.machanism.machai.ai.provider.codemie;
