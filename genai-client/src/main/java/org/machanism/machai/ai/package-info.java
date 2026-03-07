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
 * Root package for MachAI's generative-AI client integration layer.
 *
 * <p>This namespace provides a provider-agnostic API for invoking LLM/GenAI backends, plus a set of built-in
 * provider implementations and host-executed function tools.
 *
 * <h2>Package structure</h2>
 * <ul>
 *   <li>{@code org.machanism.machai.ai.manager} – public manager API and SPI for selecting and operating a
 *       {@link org.machanism.machai.ai.manager.GenAIProvider} implementation, including usage aggregation via
 *       {@link org.machanism.machai.ai.manager.Usage}.</li>
 *   <li>{@code org.machanism.machai.ai.provider.*} – concrete provider integrations (for example OpenAI, Gemini,
 *       CodeMie, Anthropic/Claude) and a {@code none} provider for disabled/offline environments.</li>
 *   <li>{@code org.machanism.machai.ai.tools} – host-integrated function tools that can be registered with a provider
 *       to enable controlled file, command, and web operations.</li>
 * </ul>
 *
 * <h2>Typical flow</h2>
 * <ol>
 *   <li>Resolve a provider using {@link org.machanism.machai.ai.manager.GenAIProviderManager} and a model identifier
 *       such as {@code OpenAI:gpt-4o-mini}.</li>
 *   <li>Set system instructions and prompts, optionally attach files and register tools.</li>
 *   <li>Execute the request and capture the response and usage.</li>
 * </ol>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 * provider.instructions("You are a concise assistant.");
 * provider.prompt("Summarize this repository.");
 * String answer = provider.perform();
 * GenAIProviderManager.addUsage(provider.usage());
 * GenAIProviderManager.logUsage();
 * }</pre>
 */
package org.machanism.machai.ai;
