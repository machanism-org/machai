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
 * Provides integration with EPAM CodeMie-hosted generative AI services.
 *
 * <p>This package contains the CodeMie provider implementation used by MachAI to
 * authenticate against the CodeMie OpenID Connect token endpoint, retrieve OAuth 2.0
 * access tokens, and configure downstream provider clients that communicate with
 * CodeMie-hosted model APIs.</p>
 *
 * <p>The package adapts CodeMie authentication and endpoint details to the shared
 * {@code Genai} abstraction used throughout the application. Depending on the configured
 * chat model, it delegates requests to OpenAI-compatible or Anthropic-compatible client
 * implementations while injecting CodeMie-specific base URLs and bearer tokens.</p>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 * <li>Resolve CodeMie authentication configuration from application properties.</li>
 * <li>Acquire access tokens using password or client-credentials OAuth 2.0 grants.</li>
 * <li>Configure downstream provider adapters with CodeMie base URLs and credentials.</li>
 * <li>Select compatible provider implementations based on configured model families.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <p>Applications configure MachAI with CodeMie credentials and a supported model name,
 * then initialize the provider through the shared provider abstraction. The provider
 * obtains an access token lazily when a client is first needed and forwards requests to
 * the appropriate delegated implementation.</p>
 */
package org.machanism.machai.ai.provider.codemie;
