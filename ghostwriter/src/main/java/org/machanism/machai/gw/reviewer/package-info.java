/**
 * Provides file-format-specific reviewers that detect and normalize embedded
 * {@code @guidance} instructions for the Ghostwriter documentation pipeline.
 *
 * <p>The package is centered on the {@link org.machanism.machai.gw.reviewer.Reviewer}
 * service-provider interface. Implementations inspect source or documentation files,
 * recognize the comment syntax used by a particular format, and return formatted prompt
 * fragments that include project-relative path context for downstream processing. A
 * reviewer returns {@code null} when its input does not contain applicable guidance;
 * callers can therefore try reviewers by supported extension without having to parse
 * each file format themselves.</p>
 *
 * <p>Supported reviewers and their recognized file types include:</p>
 * <ul>
 *   <li>{@link org.machanism.machai.gw.reviewer.JavaReviewer} for Java source files and
 *       {@code package-info.java} package guidance. Ordinary Java files include their
 *       source content in the generated prompt, while package-info files provide the
 *       package path context used for package-level documentation.</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.MarkdownReviewer} for Markdown files with
 *       HTML comment guidance.</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.HtmlReviewer} for HTML, HTM, and XML files.</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.PythonReviewer} for Python comments and
 *       triple-quoted guidance blocks.</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.TypeScriptReviewer} for TypeScript line and
 *       block comments.</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.PumlReviewer} for PlantUML files containing
 *       the guidance tag.</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.TextReviewer} for dedicated
 *       {@code @guidance.txt} files.</li>
 * </ul>
 *
 * <p>Typical usage is to choose a reviewer based on the file extension, call
 * {@link org.machanism.machai.gw.reviewer.Reviewer#perform(java.io.File, java.io.File)},
 * and pass any non-{@code null} prompt fragment to the guidance processor. The first
 * argument to {@code perform} is the project root and the second is the file being
 * reviewed; the root allows the resulting prompt to describe the file by a stable,
 * project-relative path.</p>
 *
 * <pre>{@code
 * Reviewer reviewer = new JavaReviewer();
 * String prompt = reviewer.perform(projectDirectory, sourceFile);
 * if (prompt != null) {
 *     // Submit prompt to the documentation or guidance-processing workflow.
 * }
 * }</pre>
 *
 * <p>Reviewer implementations generally return {@code null} when no relevant guidance is
 * present and may throw {@link java.io.IOException} when file content cannot be read.</p>
 */
package org.machanism.machai.gw.reviewer;

/*-
 * @guidance:
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
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
 *      - Escape the closing javadoc tag in javadoc content, as it was breaking javadoc compilation.
 */
