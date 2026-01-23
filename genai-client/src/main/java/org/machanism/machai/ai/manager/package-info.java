/*-
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE PACKAGE AND THIS `package-info.java`!**
 * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * 
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package's overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 *      
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * 
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 * 
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 
 */

/**
 * Provider discovery and tool integration for MachAI GenAI clients.
 *
 * <p>
 * This package defines the {@link org.machanism.machai.ai.manager.GenAIProvider} abstraction and helper utilities
 * for:
 * <ul>
 *   <li>resolving a concrete provider implementation from a {@code Provider:Model} identifier string</li>
 *   <li>optionally augmenting providers with tool functions for controlled local file-system access and command execution</li>
 * </ul>
 *
 * <h2>Provider resolution</h2>
 * {@link org.machanism.machai.ai.manager.GenAIProviderManager#getProvider(String)} resolves the provider portion
 * of an identifier in one of two ways:
 * <ul>
 *   <li>If the provider string contains a dot ({@code .}), it is treated as a fully qualified class name and loaded directly.</li>
 *   <li>Otherwise, it is mapped to {@code org.machanism.machai.ai.provider.<provider-lowercase>.<Provider>Provider}.</li>
 * </ul>
 * After instantiation, the resolved provider receives the model portion via
 * {@link org.machanism.machai.ai.manager.GenAIProvider#model(String)}.
 *
 * <h2>Tool functions</h2>
 * Tool installers in this package register runtime functions using
 * {@link org.machanism.machai.ai.manager.GenAIProvider#addTool(String, String, java.util.function.Function, String...)}:
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.FileFunctionTools}: read/write files and list directories relative to the provider working directory</li>
 *   <li>{@link org.machanism.machai.ai.manager.CommandFunctionTools}: execute shell commands in the provider working directory</li>
 *   <li>{@link org.machanism.machai.ai.manager.SystemFunctionTools}: convenience wrapper that installs both tool sets</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini");
 * provider.instructions("Be concise.");
 * provider.prompt("Explain the CAP theorem in one paragraph.");
 *
 * // Optional: expose local tools to the provider
 * new SystemFunctionTools().applyTools(provider);
 *
 * String response = provider.perform();
 * provider.close();
 * }
 * </pre>
 */
package org.machanism.machai.ai.manager;
