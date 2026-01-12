/**
 * Provides CLI commands for Machai, enabling GenAI-powered operations including application assembly, library picking, bindex file generation, processing project documents, and cleaning temporary directories.
 * <p>
 * <b>Key Features:</b>
 * <ul>
 *     <li>Shell commands for assembling projects and selecting libraries with GenAI assistance.</li>
 *     <li>Bindex management: creation and registration via CLI to integrate project metadata.</li>
 *     <li>Document processing: automated scanning and analysis of source/project files using GenAI models.</li>
 *     <li>Cleanup utilities to remove temporary Machai directories.</li>
 * </ul>
 * <p>
 * Typical usage involves instantiating MachaiCLI, running shell commands to assemble applications, pick dependencies, process documentation, and maintain pristine working directories.
 * <p>
 * <b>Example:</b>
 * <pre>
 * {@code
 * MachaiCLI.main(new String[]{ });
 * // In CLI shell, use commands such as:
 * // assemby pick "My web app requirements"
 * // bindex --dir /path/to/app
 * // process --scan /src/main/java --genai OpenAI:gpt-5.1
 * // clean --dir /tmp/myapp
 * }
 * </pre>
 * <p>
 * Each command is implemented as a shell component class in this package, with Javadoc-provided examples and details.
 * <p>
 * See class-level documentation in respective classes for more information and code examples.
 */
package org.machanism.machai.cli;

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
