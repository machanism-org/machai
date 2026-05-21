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
 * Provides CodeMie-backed AI provider integration for the MachAI application.
 *
 * <p>This package contains the CodeMie provider implementation that authenticates against
 * the EPAM CodeMie OpenID Connect token endpoint, retrieves OAuth 2.0 access tokens, and
 * adapts CodeMie-hosted model endpoints to the application's common {@code Genai}
 * abstraction.</p>
 *
 * <p>The package supports routing requests to OpenAI-compatible and Anthropic-compatible
 * clients depending on the configured chat model. During initialization, it resolves the
 * appropriate authentication flow, acquires an access token lazily, and injects the
 * resulting credentials and base URLs into the delegated provider configuration.</p>
 *
 * <h2>Supported responsibilities</h2>
 * <ul>
 * <li>Resolve CodeMie authentication settings from application configuration.</li>
 * <li>Choose between password grant and client credentials grant token requests.</li>
 * <li>Expose CodeMie endpoints through provider implementations used elsewhere in the application.</li>
 * <li>Bridge CodeMie access tokens into OpenAI-compatible or Anthropic-compatible clients.</li>
 * </ul>
 *
 * <h2>Relevant configuration properties</h2>
 * <ul>
 * <li>{@code GENAI_USERNAME}: user e-mail address or client identifier.</li>
 * <li>{@code GENAI_PASSWORD}: password or client secret.</li>
 * <li>{@code chatModel}: determines which downstream provider implementation is used.</li>
 * <li>{@code AUTH_URL}: optional override for the default CodeMie token endpoint.</li>
 * </ul>
 */
package org.machanism.machai.ai.provider.codemie;
