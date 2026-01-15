/**
 * Top-level API and SPI extension points for generative AI providers in the Machanism AI client.
 *
 * <p>This package contains the provider-facing abstractions and acts as the root namespace for plugging in
 * concrete generative AI implementations.
 *
 * <h2>Sub-packages</h2>
 * <ul>
 *   <li>{@code org.machanism.machai.ai.manager} – Provider SPI and provider lifecycle/management utilities.</li>
 *   <li>{@code org.machanism.machai.ai.provider.openai} – OpenAI provider integration.</li>
 *   <li>{@code org.machanism.machai.ai.provider.none} – No-op provider implementation (disables GenAI while
 *       preserving API compatibility).</li>
 *   <li>{@code org.machanism.machai.ai.provider.web} – Provider implementation backed by a remote/web
 *       orchestrator.</li>
 * </ul>
 */
package org.machanism.machai.ai;

/*- 
 * @guidance:
 * **IMPORTANT: ADD JAVADOC TO THIS `package-info.java`!**
 *
 * - Update `package-info.java` based on summary sub-packages information.
 */

