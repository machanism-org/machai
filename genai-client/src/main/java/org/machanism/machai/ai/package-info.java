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
 * <p>This package defines the top-level namespace for the GenAI Client. It exposes core entry points for:
 * <ul>
 *   <li>selecting and instantiating a concrete provider implementation for a given provider/model identifier,</li>
 *   <li>driving a request by supplying instructions, prompts, and optional file inputs,</li>
 *   <li>optionally registering host-side function tools that a provider may invoke during execution,</li>
 *   <li>inspecting token usage for the most recent run.</li>
 * </ul>
 *
 * <h2>Typical workflow</h2>
 * <ol>
 *   <li>Resolve a provider and model with
 *   {@link org.machanism.machai.ai.manager.GenAIProviderManager#getProvider(String, org.machanism.macha.core.commons.configurator.Configurator)}.</li>
 *   <li>Optionally install host-side tools via {@link org.machanism.machai.ai.tools.FunctionToolsLoader}.</li>
 *   <li>Build request state using {@link org.machanism.machai.ai.manager.GenAIProvider#instructions(String)},
 *   {@link org.machanism.machai.ai.manager.GenAIProvider#prompt(String)}, and optional attachments.</li>
 *   <li>Execute with {@link org.machanism.machai.ai.manager.GenAIProvider#perform()} and read metrics with
 *   {@link org.machanism.machai.ai.manager.GenAIProvider#usage()}.</li>
 * </ol>
 *
 * <h2>Subpackages</h2>
 * <ul>
 *   <li>{@code org.machanism.machai.ai.manager} – provider contract, reflective provider resolution, and usage accounting.</li>
 *   <li>{@code org.machanism.machai.ai.provider.*} – concrete provider implementations (for example OpenAI, web automation, or a no-op provider).</li>
 *   <li>{@code org.machanism.machai.ai.tools} – optional host-side tool installers (file I/O, command execution, web access).</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * // Optional: install host-side tools, depending on the provider and host policy.
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
