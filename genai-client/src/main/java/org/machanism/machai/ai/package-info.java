/*-
 * @guidance:
 *
 * **IMPORTANT: UPDATE THIS `package-info.java`!**
 *
 * - Use Clear and Concise Descriptions:
 *     - Write meaningful summaries that explain the purpose, behavior, and usage of the package and its elements.
 *     - Avoid vague statements; be specific about functionality and intent.
 *
 * - Update `package-info.java`:
 *     - Analyze the source code within this package.
 *     - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose, scope, and usage.
 *     - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *     - Place the package-level Javadoc immediately before the `package` declaration.
 *
 * - Include Usage Examples Where Helpful:
 *     - Provide code snippets or examples in Javadoc comments for complex classes or methods, if relevant to the package.
 *
 * - Maintain Consistency and Formatting:
 *     - Follow a consistent style and structure for all Javadoc comments.
 *     - Use proper Markdown or HTML formatting for readability.
 *
 * - Add Javadoc:
 *     - Summarize the purpose and scope of child packages within the parent package-level Javadoc.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted.
 */

/**
 * Provider-neutral API for interacting with generative-AI (GenAI) models.
 *
 * <p>This package defines the top-level public entry points used by the GenAI Client to create and use a
 * {@link org.machanism.machai.ai.manager.GenAIProvider}.
 * Providers implement a common workflow:
 *
 * <ol>
 *   <li>Create and initialize a provider instance from configuration.</li>
 *   <li>Select a model.</li>
 *   <li>Accumulate conversation state (instructions, prompts, optional files).</li>
 *   <li>Execute the run and obtain a textual response, and optionally compute embeddings.</li>
 * </ol>
 *
 * <h2>Subpackages</h2>
 * <ul>
 *   <li>{@code org.machanism.machai.ai.manager} – core abstractions and runtime management:
 *     provider contract, reflective provider resolution, and token usage accounting.</li>
 *   <li>{@code org.machanism.machai.ai.provider.*} – concrete provider implementations (for example OpenAI, Web UI
 *     automation, a "none" provider, and other integrations).</li>
 *   <li>{@code org.machanism.machai.ai.tools} – host-side tool installers for registering common functions (file I/O,
 *     command execution, web fetching) with a provider.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this repository.");
 *
 * String answer = provider.perform();
 * provider.close();
 * }</pre>
 */
package org.machanism.machai.ai;
