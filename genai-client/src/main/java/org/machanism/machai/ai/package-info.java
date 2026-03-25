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
 * Provider-agnostic client API and runtime infrastructure for MachAI generative-AI integrations.
 *
 * <p>This root package defines the public abstractions used to build and execute LLM/GenAI requests while isolating
 * the rest of the application from vendor-specific SDKs and transport details. It also hosts the shared runtime pieces
 * required by providers (provider selection, usage accounting, and host-executed tools).
 *
 * <h2>Core concepts</h2>
 * <ul>
 *   <li><strong>Provider</strong>: a concrete backend integration (for example OpenAI, Gemini, Claude) implementing
 *       {@link org.machanism.machai.ai.manager.GenAIProvider}.</li>
 *   <li><strong>Provider manager</strong>: resolves a provider/model identifier to a ready-to-use provider instance via
 *       {@link org.machanism.machai.ai.manager.GenAIProviderManager}.</li>
 *   <li><strong>Usage</strong>: token/credit accounting captured per request and aggregated through
 *       {@link org.machanism.machai.ai.manager.Usage}.</li>
 *   <li><strong>Tools</strong>: a controlled set of host capabilities (file/command/web operations) that can be exposed
 *       to providers when tool/function calling is enabled.</li>
 * </ul>
 *
 * <h2>Package structure</h2>
 * <ul>
 *   <li>{@code org.machanism.machai.ai.manager} &ndash; public manager API and SPI for selecting and operating providers,
 *       including usage aggregation.</li>
 *   <li>{@code org.machanism.machai.ai.provider.*} &ndash; concrete provider implementations and supporting models.</li>
 *   <li>{@code org.machanism.machai.ai.tools} &ndash; host-integrated tools that can be registered with providers.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <ol>
 *   <li>Resolve a provider by name/model (for example {@code OpenAI:gpt-4o-mini}).</li>
 *   <li>Set instructions and a prompt; optionally attach inputs and register tools.</li>
 *   <li>Execute the request and record aggregated usage.</li>
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
