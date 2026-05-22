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
 * Provides the CodeMie-backed {@code Genai} provider implementation for MachAI.
 *
 * <p>This package contains the adapter responsible for authenticating against EPAM
 * CodeMie token services, resolving the correct OAuth 2.0 grant flow from supplied
 * credentials, and configuring downstream AI clients to send requests through the
 * CodeMie-hosted API endpoints.</p>
 *
 * <p>The package bridges CodeMie-specific authentication and endpoint configuration
 * with the application's shared provider abstraction. After obtaining an access
 * token, it configures delegated provider implementations for supported model
 * families, including OpenAI-compatible GPT models and Anthropic-compatible Claude
 * models exposed through the CodeMie platform.</p>
 *
 * <h2>Capabilities</h2>
 * <ul>
 * <li>Reads CodeMie authentication settings from the application configuration.</li>
 * <li>Obtains bearer tokens from the configured OpenID Connect token endpoint.</li>
 * <li>Supports password and client-credentials OAuth 2.0 grant types.</li>
 * <li>Configures provider-specific base URLs and API keys for delegated clients.</li>
 * <li>Selects the appropriate downstream provider according to the configured model.</li>
 * </ul>
 *
 * <h2>Usage overview</h2>
 * <p>Applications configure credentials, an optional token endpoint override, and a
 * supported chat model. The provider then retrieves an access token when the
 * delegated client is first created and uses that token to authorize requests to
 * CodeMie-managed generative AI services.</p>
 */
package org.machanism.machai.ai.provider.codemie;
