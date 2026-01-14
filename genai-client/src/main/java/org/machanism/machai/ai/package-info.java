/**
 * Top-level API and SPI extension points for generative AI providers in the Machanism AI client.
 *
 * <p>This package provides the primary namespace for interacting with generative AI capabilities and for
 * extending the client with additional providers.
 *
 * <h2>Sub-packages</h2>
 * <ul>
 *   <li>
 *     {@code org.machanism.machai.ai.manager} – Provider SPI and management infrastructure.
 *   </li>
 *   <li>
 *     {@code org.machanism.machai.ai.openai} – OpenAI provider integration.
 *   </li>
 *   <li>
 *     {@code org.machanism.machai.ai.none} – No-op/stub provider implementations for explicitly disabling
 *     generative AI while preserving compatibility.
 *   </li>
 *   <li>
 *     {@code org.machanism.machai.ai.web} – Adapters for remote/web orchestrators and communication with
 *     external AI services.
 *   </li>
 * </ul>
 */
package org.machanism.machai.ai;

// @guidance:
//
// **IMPORTANT: ADD JAVADOC TO THIS `package-info.java`!**
//
// - Update `package-info.java` based on summary sub-packages information.
