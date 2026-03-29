/**
 * Project scanning, guidance extraction, and prompt orchestration for the Ghostwriter command-line
 * tool.
 *
 * <p>
 * The {@code org.machanism.machai.gw.processor} package contains the Ghostwriter CLI entry point
 * along with the processors responsible for scanning a project directory tree, extracting embedded
 * {@code @guidance:} directives (via reviewers), and invoking the configured
 * {@link org.machanism.machai.ai.manager.Genai GenAI provider}.
 * </p>
 *
 * <h2>Core responsibilities</h2>
 * <ul>
 *   <li>
 *     Traverse a project directory (optionally multi-module) and select files using include
 *     matchers and excludes.
 *   </li>
 *   <li>
 *     Compose prompts from project metadata, per-file content, and extracted guidance.
 *   </li>
 *   <li>
 *     Execute prompts against a configured provider and optionally log composed inputs.
 *   </li>
 * </ul>
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.Ghostwriter}: CLI entry point that reads
 *     configuration, parses command-line options, and runs a scan.
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.AbstractFileProcessor}: common traversal utilities,
 *     include/exclude matching, and module handling.
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.AIFileProcessor}: prompt composition and provider
 *     invocation (including optional input logging).
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.GuidanceProcessor}: default scanning mode that uses
 *     {@link org.machanism.machai.gw.reviewer.Reviewer reviewers} to extract per-file guidance.
 *   </li>
 *   <li>
 *     {@link org.machanism.machai.gw.processor.ActProcessor}: "act" mode which executes predefined
 *     TOML-based prompt templates against matching files.
 *   </li>
 * </ul>
 *
 * <h2>Processing model</h2>
 * <p>
 * Processing is strictly filesystem-based: projects are not built and dependencies are not
 * resolved during scanning. When a path matcher is used (via {@code glob:} or {@code regex:}),
 * inclusion decisions are made against project-relative paths and known build/tooling directories
 * are ignored.
 * </p>
 */
package org.machanism.machai.gw.processor;
/*-
 * @guidance:
 *
 * **IMPORTANT: UPDATE OR ADD JAVADOC FOR ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
 *
 * - Update Existing Javadoc and Add Missing Javadoc:
 *      - Review all classes in the folder.
 *      - Update any existing Javadoc to ensure it is accurate, comprehensive, and follows best practices.
 *      - Add Javadoc to any classes, methods, or fields where it is missing.
 *      - Ensure that all Javadoc is up-to-date and provides clear, meaningful documentation.
 * - Use Clear and Concise Descriptions:
 *      - Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 *      - Avoid vague statements; be specific about functionality and intent.
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 * - Include Usage Examples Where Helpful:
 *      - Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * - Maintain Consistency and Formatting:
 *      - Follow a consistent style and structure for all Javadoc comments.
 *      - Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *      - Review the Java class source code and include comprehensive Javadoc comments for all classes,
 *           methods, and fields, adhering to established best practices.
 *      - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *           and any exceptions thrown.
 *      - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;`
 *           and `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc.
 *      - Do not use escaping in `{@code ...}` tags.    
 * - Use the Java Version Defined in `pom.xml`:
 *      - All code improvements and Javadoc updates must be compatible with the Java version specified in the project's `pom.xml`.
 *      - Do not use features or syntax that require a higher Java version than defined in `pom.xml`.
 */
