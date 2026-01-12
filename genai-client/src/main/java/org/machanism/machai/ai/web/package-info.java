/**
 * Provides classes for automating interaction with web-based user interfaces,
 * especially when direct GenAI API access is unavailable.
 * <p>
 * This package enables communication with third-party web services,
 * including <a href="https://solutionshub.epam.com/solution/ai-dial">AI DIAL</a>
 * and <a href="https://www.youtube.com/@EPAMAIRunCodeMie">EPAM AI/Run CodeMie</a>,
 * using Anteater workspace and recipe automation. Use these classes to send prompts,
 * receive responses, and perform tasks by programmatically controlling GUIs.
 * <p>
 * <b>Usage:</b>
 * <pre>
 * {@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
 * provider.model("config.yaml");
 * provider.setWorkingDir(new File("/path/to/project"));
 * String result = provider.perform();
 * }
 * </pre>
 * <p>
 * <b>Limitations:</b> Configuration may require additional plugins or clipboard handling on some platforms.
 * Please consult the target platform and AE documentation for details.
 * <p>
 * <b>Thread Safety:</b> Implementations in this package are not guaranteed to be thread-safe.
 * <p>
 * <b>Extensibility:</b> The main class in this package, {@link WebProvider},
 * extends {@link org.machanism.machai.ai.none.NoneProvider} and incorporates
 * Anteater workspace integration for flexible automation. See class-level Javadoc
 * in source files for more details and advanced usage.
 */
package org.machanism.machai.ai.web;

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
 *      - Generate comprehensive package-level Javadoc that clearly describes the package�s overall purpose and usage.
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
 * -  Update Javadoc with Code Changes:
 * 		- Revise Javadoc comments whenever code is modified to ensure documentation remains accurate and up to date.
 * 
 * -  Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */