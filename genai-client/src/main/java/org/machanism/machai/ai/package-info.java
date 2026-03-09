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
 * <p>This package defines a provider-agnostic API for invoking LLM/GenAI backends, along with built-in provider
 * implementations and host-executed tools.
 *
 * <h2>Key responsibilities</h2>
 * <ul>
 *   <li>Provide a consistent interface to configure instructions, prompts, and attachments regardless of the
 *       underlying vendor.</li>
 *   <li>Centralize provider discovery/selection and aggregate per-request usage accounting.</li>
 *   <li>Expose a controlled tool surface (for example file, command, and web operations) that providers can call
 *       when tool/function calling is enabled.</li>
 * </ul>
 *
 * <h2>Package structure</h2>
 * <ul>
 *   <li>{@code org.machanism.machai.ai.manager} – public manager API and SPI for selecting and operating a
 *       {@link org.machanism.machai.ai.manager.GenAIProvider} implementation, including usage aggregation via
 *       {@link org.machanism.machai.ai.manager.Usage}.</li>
 *   <li>{@code org.machanism.machai.ai.provider.*} – concrete provider integrations (for example OpenAI, Gemini,
 *       CodeMie, Anthropic/Claude) and a {@code none} provider for disabled/offline environments.</li>
 *   <li>{@code org.machanism.machai.ai.tools} – host-integrated tools that can be registered with a provider to enable
 *       controlled file, command, and web operations.</li>
 * </ul>
 *
 * <h2>Typical flow</h2>
 * <ol>
 *   <li>Select a provider through {@link org.machanism.machai.ai.manager.GenAIProviderManager} using a provider/model
 *       identifier (for example {@code OpenAI:gpt-4o-mini}).</li>
 *   <li>Configure system instructions and prompts; optionally attach input files and register tools.</li>
 *   <li>Execute the request, then record the response and associated usage.</li>
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
