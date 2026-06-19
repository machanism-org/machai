/*-
 * @guidance:
 *
 * **IMPORTANT: UPDATE THIS `package-info.java`!**
 *
 * - Use Clear and Concise Descriptions:
 *     - Write meaningful summaries that explain the purpose, behavior, and usage of the package and its elements.
 *     - Avoid vague statements; be specific about functionality and intent.
 *
 * - Update `package-info.java`:
 *     - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose, scope, and usage based on package-info.java files located on child folders.
 *     - Place the package-level Javadoc immediately before the `package` declaration.
 *
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` and &gt; in `<pre>` content for Javadoc. 
 *     - Ensure that the code is properly escaped and formatted.
 *     
 * author: Viktor Tovstyi
 * since: 1.2.0
 */

/**
 * Provides the executable Model Context Protocol (MCP) server layer for Machai.
 * <p>
 * This package contains the command-line entry point and concrete server
 * implementations used to expose Machai GenAI tools through MCP transports. The
 * server can run over standard input/output for process-based integrations or
 * over HTTP for remote clients. HTTP deployments support both stateless request
 * handling and streamable, session-aware communication.
 * </p>
 * <p>
 * The package is organized around a shared server abstraction that defines common
 * metadata, project-directory handling, tool registration, and startup behavior.
 * {@code StdioMcpServer}, {@code HttpStatelessMcpServer}, and
 * {@code HttpStreamableMcpServer} provide transport-specific implementations,
 * while {@code AbstractHttpMcpServer} supplies the Jetty servlet hosting logic
 * used by HTTP transports.
 * </p>
 * <p>
 * Tool exposure is handled through {@code GenericGenaiAdapter}, which converts
 * Machai {@code ToolFunction} definitions and parameter descriptors into MCP tool
 * schemas and synchronous tool specifications. The adapter invokes registered
 * functions with request arguments, optional session identifiers, the configured
 * project directory, and runtime configuration, then returns MCP-compliant tool
 * results. HTTP server variants also register MCP prompts where supported.
 * </p>
 * <p>
 * The {@code McpServer} application class parses command-line options for the
 * server name, version, project directory, port, and HTTP session mode. If a port
 * is supplied, an HTTP server is started on that port; otherwise, the server runs
 * in STDIO mode.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
package org.machanism.machai.mcp.server;