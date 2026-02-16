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
 * <p>This package contains the public entry points used by the GenAI Client to configure and obtain a
 * {@link org.machanism.machai.ai.manager.GenAIProvider} instance and to invoke model operations such as generating
 * a response, attaching files, registering function tools, and retrieving token usage.
 *
 * <p>The main workflow is:
 * <ol>
 *   <li>Resolve a provider implementation and model name (typically via
 *   {@link org.machanism.machai.ai.manager.GenAIProviderManager#getProvider(String, org.machanism.macha.core.commons.configurator.Configurator)}).</li>
 *   <li>Optionally install host-side function tools (see {@code org.machanism.machai.ai.tools}).</li>
 *   <li>Build request state by adding instructions, prompts, and optional files.</li>
 *   <li>Execute the request with {@link org.machanism.machai.ai.manager.GenAIProvider#perform()} and inspect
 *   {@link org.machanism.machai.ai.manager.GenAIProvider#usage()}.</li>
 * </ol>
 *
 * <h2>Subpackages</h2>
 * <ul>
 *   <li>{@code org.machanism.machai.ai.manager} – core abstractions and runtime management (provider contract,
 *   reflective provider resolution, and token usage accounting).</li>
 *   <li>{@code org.machanism.machai.ai.provider.*} – concrete provider implementations (for example OpenAI, Web UI
 *   automation, a "none" provider, and other integrations).</li>
 *   <li>{@code org.machanism.machai.ai.tools} – optional host-side tool installers that register common functions
 *   (file I/O, command execution, web fetching) with a provider.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * // Optional: install file/command/web tools, depending on the provider.
 * FunctionToolsLoader.getInstance().setConfiguration(conf);
 * FunctionToolsLoader.getInstance().applyTools(provider);
 *
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this repository.");
 * String answer = provider.perform();
 * Usage usage = provider.usage();
 *
 * provider.close();
 * }</pre>
 */
package org.machanism.machai.ai;
