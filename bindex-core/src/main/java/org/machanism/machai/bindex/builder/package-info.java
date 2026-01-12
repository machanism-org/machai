/**
 * Provides core classes and utilities for building and managing bindex-related constructs
 * within the Machai framework. This package contains extensible builder patterns and supporting
 * structures used to assemble, configure, and facilitate the creation and management of
 * intelligent project index (bindex) documents using GenAI-enhanced workflows.
 *
 * <p>
 * Main Capabilities:
 * <ul>
 *   <li>Managing creation and lifecycle of bindex objects, including domain-specific indexing documents for Maven, JavaScript, Python, and other project types.</li>
 *   <li>Providing flexible builder patterns, allowing AI-driven and customizable generation of bindex content based on project context.</li>
 *   <li>Enabling extension and deep customization, including project context extraction, manifest and source analysis, and prompt-driven document assembly.</li>
 * </ul>
 *
 * <p>
 * Overview of Major Classes:
 * <ul>
 *   <li>{@link org.machanism.machai.bindex.builder.BindexBuilder} – The primary builder for generating bindex documents from generic project layouts with GenAIProvider support.
 *   <li>{@link org.machanism.machai.bindex.builder.JScriptBindexBuilder} – Builder for JavaScript/TypeScript/Vue projects. Reads manifest (package.json), sources, and analyzes project context for GenAI-driven index creation.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.MavenBindexBuilder} – Builder for Maven-based Java projects. Reads pom.xml, aggregates sources/resources, cleans model metadata, and prompts GenAI for bindex generation.</li>
 *   <li>{@link org.machanism.machai.bindex.builder.PythonBindexBuilder} – Builder for Python projects using pyproject.toml, extracting sources and manifest for AI prompts and bindex assembly.</li>
 * </ul>
 *
 * <p>
 * Usage example:
 * <pre>
 *     // Example for a Maven project:
 *     MavenProjectLayout layout = ...;
 *     MavenBindexBuilder builder = new MavenBindexBuilder(layout);
 *     builder.genAIProvider(provider);
 *     Bindex bindex = builder.build();
 * </pre>
 *
 * <pre>
 *     // Example for a JavaScript project:
 *     JScriptProjectLayout layout = ...;
 *     JScriptBindexBuilder builder = new JScriptBindexBuilder(layout);
 *     builder.genAIProvider(provider);
 *     Bindex bindex = builder.build();
 * </pre>
 *
 * <pre>
 *     // Example for a Python project:
 *     ProjectLayout layout = ...;
 *     PythonBindexBuilder builder = new PythonBindexBuilder(layout);
 *     builder.genAIProvider(provider);
 *     Bindex bindex = builder.build();
 * </pre>
 *
 * <p>
 * The classes in this package are intended as tools for application developers and automation designers
 * who need to define, manipulate, and extend bindex features across multiple languages, frameworks,
 * and build systems within the Machai ecosystem.
 */
package org.machanism.machai.bindex.builder;

/* @guidance:
*
* **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THIS PACKAGE, INCLUDING THIS `package-info.java`!**
*
* - Use clear and concise descriptions:
*     - Write meaningful summaries that explain the purpose, behavior, and usage of each element.
*     - Avoid vague statements; be specific about functionality and intent.
* - Update `package-info.java`:
*     - Analyze the source code in this package.
*     - Generate comprehensive package-level Javadoc that describes the package’s overall purpose and usage.
*     - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
* - Include usage examples where helpful:
*     - Provide code snippets or examples in Javadoc comments for complex classes or methods.
* - Maintain consistency and formatting:
*     - Follow a consistent style and structure for all Javadoc comments.
*     - Use proper Markdown or HTML formatting for readability.
* - Keep Javadoc up to date with code changes:
*     - Revise Javadoc comments whenever code is modified to ensure documentation remains accurate and current.
* - Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` blocks within Javadoc.
*/
