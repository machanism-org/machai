/**
 * Top-level API and SPI extension point for generative AI providers in the Machanism AI client.
 *
 * <p>This package provides core entry points for selecting and invoking providers, integrating tools,
 * and extending the system with additional model backends. Concrete implementations and adapters are
 * organized in sub-packages.
 *
 * <h2>Sub-packages</h2>
 * <ul>
 *   <li>
 *     {@code org.machanism.machai.ai.manager} – Core provider SPI and management infrastructure, such as
 *     {@link org.machanism.machai.ai.manager.GenAIProvider} and
 *     {@link org.machanism.machai.ai.manager.GenAIProviderManager}.
 *   </li>
 *   <li>
 *     {@code org.machanism.machai.ai.openAI} – OpenAI provider integration (model invocation, embeddings,
 *     file handling, and configuration).
 *   </li>
 *   <li>
 *     {@code org.machanism.machai.ai.none} – No-op/stub provider implementations to explicitly disable
 *     generative AI while preserving compatibility.
 *   </li>
 *   <li>
 *     {@code org.machanism.machai.ai.web} – Adapters for remote/web orchestrators and communication
 *     with external AI services.
 *   </li>
 * </ul>
 */
package org.machanism.machai.ai;

/**
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO THIS `package-info.java`!**
 *
 * - Update `package-info.java` based on summary sub-packages information.
 */