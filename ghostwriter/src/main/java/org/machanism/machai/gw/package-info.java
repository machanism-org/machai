/**
 * Provides core document scanning, guidance extraction, and AI-driven review automation.
 * <p>
 * This package contains the main classes responsible for scanning user, source, test, and document directories, extracting guidance and meta-information, and coordinating with AI providers for documentation generation and review processing. Key elements include:
 * <ul>
 *     <li>{@link Ghostwriter} - The main application entry point for entire scan and review operations.</li>
 *     <li>{@link FileProcessor} - Handles deep scanning of directories/files, guidance parsing and context management, and interfaces with AI provider and reviewers.</li>
 * </ul>
 * <p>
 * Typical usage involves creating a {@code FileProcessor} instance and running a scan for documentation input and review, optionally customized by project layout and guidance inheritance flags. See examples below:
 * <pre>
 * {@code
 * // Run document scan on default user directory
 * Ghostwriter.main(new String[] {});
 *
 * // Scan a specific root folder and obtain review guidance:
 * GenAIProvider provider = GenAIProviderManager.getProvider(null);
 * FileProcessor processor = new FileProcessor(provider);
 * processor.scanDocuments(new File("/path/to/project/root"));
 * }
 * </pre>
 * <p>
 * Guidance inheritance and parent context management can be modified via {@code setInheritance(true)} or {@code setUseParentsGuidances(true)} on the FileProcessor. Javadoc comments throughout this package are maintained to reflect code and project changes for clarity, usability, and up-to-date documentation.
 *
 * <p><strong>Note:</strong> This package is designed for extensibility, supporting custom reviewers, prompt bundles, and project analyzer plug-ins.
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
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 * 
 * -  Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */