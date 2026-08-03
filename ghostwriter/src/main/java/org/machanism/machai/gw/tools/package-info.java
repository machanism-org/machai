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

/**
 * Provides Ghostwriter tool implementations that expose project automation,
 * file-system access, process execution, web integration, and workflow control
 * capabilities to the Machai function-tool runtime.
 * <p>
 * Classes in this package generally implement {@link org.machanism.machai.ai.tools.FunctionTools}
 * and publish callable operations through {@link org.machanism.machai.ai.tools.Tool}
 * annotated methods. These tools are intended to be invoked by an AI provider or
 * orchestration layer with a project directory and configuration context supplied
 * by the host application.
 * </p>
 *
 * <h2>Main responsibilities</h2>
 * <ul>
 * <li><b>Act execution:</b> {@link org.machanism.machai.gw.tools.ActFunctionTools}
 * and {@link org.machanism.machai.gw.tools.ActSpecFunctionTools} load, run,
 * retrieve results for, repeat, and navigate reusable Ghostwriter Acts and
 * episodes.</li>
 * <li><b>Command execution:</b> {@link org.machanism.machai.gw.tools.CommandFunctionTools}
 * runs approved system commands, captures logs, and provides log paging and
 * regular-expression search utilities, with command validation delegated to
 * {@link org.machanism.machai.gw.tools.CommandSecurityChecker}.</li>
 * <li><b>File operations:</b> {@link org.machanism.machai.gw.tools.FileFunctionTools}
 * reads, writes, lists, and patches project files using project-relative paths.</li>
 * <li><b>Guidance processing:</b> {@link org.machanism.machai.gw.tools.GuidanceFunctionTools}
 * discovers files containing embedded guidance comments, processes them
 * synchronously or asynchronously, retrieves background results, and exposes
 * prompt templates for guidance workflows.</li>
 * <li><b>Project context:</b> {@link org.machanism.machai.gw.tools.ProjectContextFunctionTools}
 * stores, retrieves, pushes, and pops variables used across tool calls and Act
 * episodes.</li>
 * <li><b>Web and REST access:</b> {@link org.machanism.machai.gw.tools.WebFunctionTools}
 * fetches web content and invokes REST endpoints with optional headers,
 * authentication, timeout, selector, and charset handling.</li>
 * <li><b>Runtime control:</b> package-specific exceptions signal tool denial,
 * task completion, process termination, repeated episodes, or movement to another
 * episode without conflating those control-flow events with ordinary failures.</li>
 * </ul>
 *
 * <h2>Path and configuration model</h2>
 * <p>
 * Tool methods commonly receive a {@link java.io.File} project directory and
 * interpret user-provided file or directory values relative to that directory.
 * Configuration values are supplied through
 * {@link org.machanism.macha.core.commons.configurator.Configurator}; selected
 * inputs, such as command strings, Act properties, URLs, and HTTP headers, may be
 * resolved through the configured substitution mechanism at runtime.
 * </p>
 *
 * <h2>Usage example</h2>
 * <p>
 * The host runtime normally discovers these tools reflectively from their
 * annotations. Direct use is also possible in integration tests or embedded
 * hosts:
 * </p>
 *
 * <pre>
 * FileFunctionTools files = new FileFunctionTools();
 * Object content = files.readFile("README.md", "UTF-8", projectDir);
 *
 * CommandFunctionTools commands = new CommandFunctionTools();
 * Object report = commands.executeCommand("mvn test", null, ".", 2048, "UTF-8", projectDir, configurator);
 *
 * ProjectContextFunctionTools.put(projectDir, "lastReport", report);
 * </pre>
 *
 * <h2>Implementation notes</h2>
 * <p>
 * Implementations favor bounded output, explicit character-set handling,
 * project-scoped path resolution, and structured result maps so callers can
 * safely compose tool responses in automated workflows. Security-sensitive
 * operations, especially command execution and external network calls, should be
 * configured and constrained by the host environment.
 * </p>
 *
 * @see org.machanism.machai.ai.tools.FunctionTools
 * @see org.machanism.machai.ai.tools.Tool
 * @see org.machanism.machai.gw.processor.ActProcessor
 */
package org.machanism.machai.gw.tools;
