/*-
 * @guidance:
 * Use your file-modification tools to update or add Javadoc comments to all Java classes and package-level files in this folder.
 *
 * 1. Class, Interface, Method, and Field Documentation
 *    - If there is no javadoc documentation defined, create it.
 *    - Review each Java file and add/update descriptive Javadoc for all public and protected classes, interfaces, methods, and fields.
 *    - Explain the direct functional purpose, all parameters (`@param`), return values (`@return`), and exceptions thrown (`@throws`).
 *    - **AI Metaprogramming Metadata:** If a class or method is annotated with `@Tool`, `@Prompt`, or `@Resource`, explicitly document its role as a "Functional AI Tool", "Prompt Template", or "Contextual Resource" respectively.
 * 2. Package-Level Documentation (`package-info.java`)
 *    - Analyze all Java files inside this folder.
 *    - Generate or update `package-info.java` with a comprehensive Javadoc block that explains the package's architecture, relationships, and usage.
 *    - Place this Javadoc immediately before the `package` declaration.
 * 3. Formatting, Examples, & Syntax Safety (Crucial)
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
 * Provides Ghostwriter host-side function tools that expose project-scoped
 * automation capabilities to the Machai runtime.
 * <p>
 * The package contains concrete {@link org.machanism.machai.ai.tools.FunctionTools}
 * implementations used by the Ghostwriter runtime to expose file access,
 * command execution, HTTP integration, Act orchestration, guidance processing,
 * and project-context management through
 * {@link org.machanism.machai.ai.tools.Tool}-annotated methods. It also defines
 * helper utilities for bounded logging and patch application plus lightweight
 * exception types that communicate control-flow decisions such as task
 * completion, process termination, episode repetition, and episode navigation.
 * </p>
 * <p>
 * Methods annotated with {@link org.machanism.machai.ai.tools.Tool} are exposed
 * as callable functional AI tools, while methods annotated with
 * {@link org.machanism.machai.ai.tools.Prompt} provide prompt templates for
 * workflow-specific requests. Resource-oriented integrations, when supplied by
 * the host, are used to retrieve project or web content under the same
 * project-bound and security-controlled execution model.
 * </p>
 *
 * <h2>Package overview</h2>
 * <ul>
 * <li><b>Act orchestration:</b>
 * {@link org.machanism.machai.gw.tools.ActFunctionTools} loads Act metadata,
 * executes Acts synchronously or asynchronously, and retrieves persisted Act
 * results. {@link org.machanism.machai.gw.tools.ActSpecFunctionTools} provides
 * Act-specific control tools for jumping to another episode or requesting a
 * repeat.</li>
 * <li><b>Command execution and logging:</b>
 * {@link org.machanism.machai.gw.tools.CommandFunctionTools} runs validated
 * operating-system commands, captures bounded output, persists logs, pages log
 * content, and searches logs with regular expressions. Validation is delegated
 * to {@link org.machanism.machai.gw.tools.CommandSecurityChecker}, while
 * {@link org.machanism.machai.gw.tools.LogBuilder} manages retained output and
 * log-file reports. {@link org.machanism.machai.gw.tools.CommandSpecFunctionTools}
 * exposes task and process termination controls for processors.</li>
 * <li><b>File-system operations:</b>
 * {@link org.machanism.machai.gw.tools.FileFunctionTools} reads, writes, lists,
 * and patches project files, and {@link org.machanism.machai.gw.tools.PatchApplier}
 * applies unified or simplified diff patches. Recursive file and directory
 * discovery is delegated to the project-layout utilities. File operations are
 * intended to remain within the project directory supplied by the host. The
 * {@link org.machanism.machai.gw.tools.FileFunctionTools#getRelativePath(java.io.File, java.io.File, boolean)}
 * helper normalizes paths for tool responses.</li>
 * <li><b>Guidance workflows:</b>
 * {@link org.machanism.machai.gw.tools.GuidanceFunctionTools} discovers files
 * containing guidance tags, processes them with Ghostwriter guidance engines,
 * and retrieves asynchronous processing results.</li>
 * <li><b>Project state and runtime control:</b>
 * {@link org.machanism.machai.gw.tools.ProjectContextFunctionTools} stores and
 * retrieves project-scoped context variables, while
 * {@link org.machanism.machai.gw.tools.CommandSpecFunctionTools} exposes tools
 * for ending a task or terminating execution. The
 * {@link org.machanism.machai.gw.tools.ActSpecFunctionTools} uses
 * {@link org.machanism.machai.gw.tools.MoveToEpisodeException} and
 * {@link org.machanism.machai.gw.tools.RepeatEpisodeException} for episode
 * navigation and repetition. Other specialized exceptions such as
 * {@link org.machanism.machai.gw.tools.EndTaskException},
 * {@link org.machanism.machai.gw.tools.ProcessTerminationException},
 * {@link org.machanism.machai.gw.tools.DenyException} allow the host to
 * distinguish intentional control flow from ordinary failures.</li>
 * <li><b>Reusable support types:</b>
 * {@link org.machanism.machai.gw.tools.LogBuilder} bounds retained output and
 * persists command logs, while
 * {@link org.machanism.machai.gw.tools.PatchApplier} applies validated text
 * patches. {@link org.machanism.machai.gw.tools.CommandSecurityChecker} and
 * the control-flow exception classes are deliberately small integration points
 * that can be reused by host processors without invoking a tool facade.</li>
 * <li><b>Web integration:</b>
 * {@link org.machanism.machai.gw.tools.WebFunctionTools} fetches HTML or text
 * content and executes REST requests with configurable headers, timeouts,
 * character sets, optional selector extraction, and Basic authentication via
 * URL user-info or explicit request headers.</li>
 * </ul>
 *
 * <h2>Control-flow and security contracts</h2>
 * <p>
 * {@link org.machanism.machai.gw.tools.DenyException} reports a command rejected
 * by the configured deny-list. The specialized exceptions
 * {@link org.machanism.machai.gw.tools.EndTaskException},
 * {@link org.machanism.machai.gw.tools.ProcessTerminationException},
 * {@link org.machanism.machai.gw.tools.MoveToEpisodeException}, and
 * {@link org.machanism.machai.gw.tools.RepeatEpisodeException} are intentional
 * signals for the embedding processor; they are not ordinary application
 * failures and should be handled according to the host's workflow policy.
 * </p>
 *
 * <h2>Usage model</h2>
 * <p>
 * Most tools accept a project directory supplied by the host runtime and treat
 * paths relative to that directory. Several tools also accept a
 * {@link org.machanism.macha.core.commons.configurator.Configurator} so command
 * strings, URLs, HTTP headers, Act properties, and related values can be
 * resolved through runtime substitution before execution.
 * </p>
 *
 * <h2>Example</h2>
 * <p>
 * Tool classes are typically discovered reflectively, but they can also be used
 * directly by an integration host or test harness:
 * </p>
 * <pre>
 * File projectDir = new File("C:/workspace/sample-project");
 * Configurator configurator = ...;
 *
 * FileFunctionTools files = new FileFunctionTools();
 * String readme = files.readFile(new File("README.md"), "UTF-8", projectDir);
 *
 * CommandFunctionTools commands = new CommandFunctionTools();
 * Object report = commands.executeCommand("mvn -q test", null, ".", 2048, "UTF-8", projectDir, configurator);
 *
 * ProjectContextFunctionTools.put(projectDir, "lastCommandReport", report);
 * </pre>
 * <p>
 * Tool methods return structured maps or lists when callers need status and
 * metadata, and return strings for text-oriented operations. Methods that
 * represent deliberate workflow transitions use the specialized exception
 * types documented below; an embedding processor should not convert those
 * signals into ordinary error messages.
 * </p>
 *
 * <h2>Design notes</h2>
 * <p>
 * Implementations in this package favor project-bound path resolution, bounded
 * output retention, explicit character-set handling, structured result payloads,
 * and host-controlled security constraints. Because several tools can touch the
 * file system, start subprocesses, or call external services, callers are
 * expected to configure appropriate allow/deny policies in the embedding
 * environment.
 * </p>
 *
 * @see org.machanism.machai.ai.tools.FunctionTools
 * @see org.machanism.machai.ai.tools.Tool
 * @see org.machanism.machai.gw.processor.ActProcessor
 * @see org.machanism.machai.gw.processor.GuidanceProcessor
 */
package org.machanism.machai.gw.tools;
