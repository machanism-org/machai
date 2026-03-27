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
 *     - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose, scope, and usage based on package-info.java files located on child folders.
 *     - Place the package-level Javadoc immediately before the `package` declaration.
 *
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` and &gt; in `<pre>` content for Javadoc. 
 *        Ensure that the code is properly escaped and formatted.
 */

/**
 * Provider-agnostic client API for MachAI generative-AI integrations.
 *
 * <p>This root package groups the public abstractions and runtime components used to configure and execute LLM/GenAI
 * requests while keeping the rest of the application isolated from vendor SDKs and transport details.
 *
 * <h2>Sub-packages</h2>
 * <ul>
 *   <li><strong>Manager API / SPI</strong> ({@code org.machanism.machai.ai.manager}) &ndash; the core provider contract
 *       ({@link org.machanism.machai.ai.manager.Genai}), provider resolution/aggregation
 *       ({@link org.machanism.machai.ai.manager.GenaiProviderManager}), and token usage reporting
 *       ({@link org.machanism.machai.ai.manager.Usage}).</li>
 *   <li><strong>Providers</strong> ({@code org.machanism.machai.ai.provider.*}) &ndash; concrete backend integrations,
 *       including OpenAI ({@code .openai}), Google Gemini ({@code .gemini}), EPAM CodeMie ({@code .codemie}), an offline
 *       no-op provider ({@code .none}), and other supported backends.</li>
 *   <li><strong>Tools</strong> ({@code org.machanism.machai.ai.tools}) &ndash; host-executed capabilities that can be
 *       registered with providers for tool/function calling (file system, command execution, and web access), typically
 *       discovered and applied via {@link org.machanism.machai.ai.tools.FunctionToolsLoader}.</li>
 * </ul>
 *
 * <h2>Typical workflow</h2>
 * <ol>
 *   <li>Select a provider/model (for example {@code OpenAI:gpt-4o-mini}).</li>
 *   <li>Initialize and configure the provider (instructions, prompts, attachments, tools).</li>
 *   <li>Execute the request and record aggregated token usage.</li>
 * </ol>
 *
 * <h2>Example</h2>
 * <pre>
 * Configurator conf = ...;
 * Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * provider.instructions("You are a concise assistant.");
 * provider.prompt("Summarize this repository.");
 *
 * // Optional: register host-side tools (file/command/web) if enabled
 * FunctionToolsLoader.getInstance().setConfiguration(conf);
 * FunctionToolsLoader.getInstance().applyTools(provider);
 *
 * String answer = provider.perform();
 * GenaiProviderManager.addUsage(provider.usage());
 * GenaiProviderManager.logUsage();
 * </pre>
 */
package org.machanism.machai.ai;
