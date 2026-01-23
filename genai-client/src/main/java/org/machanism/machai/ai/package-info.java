/**
 * Top-level public API and service provider interface (SPI) for generative AI providers in the Machanism AI
 * client.
 *
 * <p>This package contains the core abstractions used to integrate and use generative AI implementations.
 * Provider integrations typically implement SPI types (in {@code org.machanism.machai.ai.manager}) and expose
 * provider-specific modules under {@code org.machanism.machai.ai.provider.*}.
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
