/**
 * Provides the file-format-specific reviewer implementations used to discover and
 * normalize embedded {@code @guidance} instructions for the Ghostwriter documentation
 * pipeline.
 *
 * <p>The package defines the {@link org.machanism.machai.gw.reviewer.Reviewer} service
 * provider interface and its format adapters. A reviewer understands the comment or
 * document syntax of a supported format, determines whether the file contains a
 * guidance tag, and returns a prompt fragment containing the context required by the
 * downstream guidance processor. Implementations return {@code null} when a file does
 * not contain applicable guidance and may throw {@link java.io.IOException} when its
 * content cannot be read. The {@link org.machanism.machai.gw.reviewer.TextReviewer}
 * is the exception to tag detection: it accepts only files named
 * {@code @guidance.txt} and uses their non-blank contents as guidance.</p>
 *
 * <p>The available reviewers are:</p>
 * <ul>
 *   <li>{@link org.machanism.machai.gw.reviewer.JavaReviewer}, which handles Java
 *       source files, including package-level guidance in {@code package-info.java};
 *   <li>{@link org.machanism.machai.gw.reviewer.MarkdownReviewer}, which handles
 *       Markdown files with guidance in HTML comments;
 *   <li>{@link org.machanism.machai.gw.reviewer.HtmlReviewer}, which handles HTML,
 *       HTM, and XML comment syntax;
 *   <li>{@link org.machanism.machai.gw.reviewer.PythonReviewer}, which handles Python
 *       line comments and triple-quoted guidance blocks;
 *   <li>{@link org.machanism.machai.gw.reviewer.TypeScriptReviewer}, which handles
 *       TypeScript line and block comments;
 *   <li>{@link org.machanism.machai.gw.reviewer.PumlReviewer}, which handles PlantUML
 *       ({@code .puml}) files containing a guidance tag; and
 *   <li>{@link org.machanism.machai.gw.reviewer.TextReviewer}, which handles files
 *       named {@code @guidance.txt}.
 * </ul>
 *
 * <p>Each implementation exposes its supported extensions through
 * {@link org.machanism.machai.gw.reviewer.Reviewer#getSupportedFileExtensions()}.
 * A caller can use those extensions to select a reviewer, then invoke
 * {@link org.machanism.machai.gw.reviewer.Reviewer#perform(java.io.File, java.io.File)}.
 * The first argument is the project root and the second is the file being reviewed;
 * the root is used to provide stable project-relative path context in the result.
 * Prompt templates are loaded from the {@code document-prompts} resource bundle.</p>
 *
 * <p>For example, a caller can try the reviewer associated with a file's extension and
 * forward only applicable results:</p>
 *
 * <pre>{@code
 * Reviewer reviewer = new JavaReviewer();
 * String prompt = reviewer.perform(projectDirectory, sourceFile);
 * if (prompt != null) {
 *     guidanceProcessor.process(prompt);
 * }
 * }</pre>
 *
 * <p>Reviewers are intentionally format-specific: they identify guidance according to
 * the syntax they support rather than treating every file as plain text. This keeps
 * extraction rules isolated and allows additional formats to be introduced by adding
 * another {@link org.machanism.machai.gw.reviewer.Reviewer} implementation.</p>
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
