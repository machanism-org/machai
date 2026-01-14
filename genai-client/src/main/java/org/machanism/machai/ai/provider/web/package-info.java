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
 *      - Generate comprehensive package-level Javadoc that clearly describes the packages overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 *      
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * 
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 * 		- Use proper Markdown or HTML formatting for readability.
 * 
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 * 
 * -  Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */

/**
 * Web UI automation-based GenAI provider implementation.
 *
 * <p>This package provides an {@link org.machanism.machai.ai.provider.GenAIProvider} implementation that
 * executes prompt/response workflows by driving a GenAI system through its web user interface. It is
 * intended for environments where direct API access is unavailable or restricted.
 *
 * <p>The provider integrates with <a href="https://ganteater.com">Anteater</a> to run browser-based
 * automation workflows (AE recipes) that are packaged with the application as resources.
 *
 * <h2>Key Types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.provider.web.WebProvider} - Provider entry point that loads an AE
 *   workspace configuration, sets a working directory, and runs a recipe to obtain a text response.</li>
 * </ul>
 *
 * <h2>Typical Usage</h2>
 * <pre>
 * {@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
 * provider.model("config.yaml");
 * provider.setWorkingDir(new File("/path/to/project"));
 * String result = provider.perform();
 * }
 * </pre>
 *
 * <h2>Notes and Limitations</h2>
 * <ul>
 *   <li>Automation depends on the availability and correctness of the configured AE recipes and
 *       workspace configuration.</li>
 *   <li>This provider is not guaranteed to be thread-safe.</li>
 *   <li>Some target web platforms may require additional local configuration (for example, clipboard
 *       access or browser profile settings).</li>
 * </ul>
 */
package org.machanism.machai.ai.provider.web;
