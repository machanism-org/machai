/**
 * Core document scanning and AI-assisted review automation.
 *
 * <p>
 * This package provides the entry point and orchestration for scanning a project directory,
 * extracting {@code @guidance:} directives from supported files via pluggable reviewers, and
 * assembling prompt context for a configured
 * {@link org.machanism.machai.ai.manager.GenAIProvider}.
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.gw.Ghostwriter} – Command-line entry point that configures a provider
 *       and starts a scan.</li>
 *   <li>{@link org.machanism.machai.gw.FileProcessor} – Scans directories, invokes reviewers, aggregates
 *       guidance (optionally including parent directory guidance), and records prompt inputs.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>
 * {@code
 * // Scan the current user directory.
 * Ghostwriter.main(new String[] {});
 *
 * // Scan an explicit project root.
 * GenAIProvider provider = GenAIProviderManager.getProvider(null);
 * FileProcessor processor = new FileProcessor(provider);
 * processor.setUseParentsGuidances(true);
 * processor.scanDocuments(new File("/path/to/project/root"));
 * }
 * </pre>
 *
 * <p>
 * Temporary prompt input logs are written under the project directory in
 * {@code .machai/docs-inputs}.
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
package org.machanism.machai.gw;

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
