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
 * Provides the EPAM CodeMie integration for Machai generative AI providers.
 *
 * <p>This package contains the CodeMie-specific {@link org.machanism.machai.ai.provider.Genai}
 * implementation used to authenticate with the CodeMie OpenID Connect token endpoint,
 * obtain OAuth 2.0 bearer tokens, and configure a delegated AI provider with the
 * CodeMie Code Assistant API base URL. The provider supports both interactive-style
 * password grants and service-to-service client-credentials grants, selecting the
 * grant flow from the configured username or client identifier.</p>
 *
 * <p>Model routing is handled by the CodeMie provider implementation. OpenAI-compatible,
 * Gemini-compatible, and embedding model identifiers are delegated to the OpenAI provider
 * implementation configured for CodeMie endpoints, while Claude-compatible model identifiers
 * are delegated to the Anthropic provider implementation. Unsupported model identifiers are
 * rejected during initialization so configuration issues are reported before request execution.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 * <li>Reads CodeMie credentials, token endpoint overrides, and model settings from the shared provider configuration.</li>
 * <li>Requests access tokens from the configured OAuth 2.0 token endpoint using form-encoded requests.</li>
 * <li>Applies retrieved bearer tokens to delegated OpenAI-compatible or Anthropic-compatible clients.</li>
 * <li>Configures delegated providers with the CodeMie Code Assistant API base URL.</li>
 * <li>Exposes embedding operations when the selected delegated provider supports embeddings.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <p>Applications configure a CodeMie username or client id, a password or client secret,
 * and a supported model identifier. During initialization, the provider resolves the
 * authentication endpoint, obtains an access token on demand, and delegates chat completion
 * or embedding calls to the provider implementation that matches the configured model family.</p>
 */
package org.machanism.machai.ai.provider.codemie;
