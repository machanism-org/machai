/**
 * Provides Maven plugin goals for generative AI-assisted documentation workflows.
 * <p>
 * This package includes Mojos for processing and cleaning documentation files in Maven projects.
 * It integrates generative AI providers for automated documentation generation, scanning, and cleanup workflows, leveraging configurable options for AI-based enhancements and reproducible documentation management within a Maven build process.
 * </p>
 *
 * <h2>Contained Mojos</h2>
 * <ul>
 *   <li><b>Clean</b>: Removes temporary files generated during AI-assisted documentation processes. Ensures clean state for repeated documentation generation workflows.</li>
 *   <li><b>Process</b>: Initiates scanning and generative documentation processes, offering integration with AI providers. Supports parameterization for chat models and detailed control over the document scanning workflow.</li>
 * </ul>
 *
 * <h2>Basic Usage Example</h2>
 * <pre>{@code
 * <plugin>
 *   <groupId>org.machanism.machai</groupId>
 *   <artifactId>gw-maven-plugin</artifactId>
 *   <version>${project.version}</version>
 *   <executions>
 *     <execution>
 *       <goals>
 *         <goal>process</goal>
 *         <goal>clean</goal>
 *       </goals>
 *     </execution>
 *   </executions>
 * </plugin>
 * }</pre>
 *
 * This package does not provide guidance or best practices here; refer to individual goal documentation.
 */
package org.machanism.machai.maven;

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
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
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