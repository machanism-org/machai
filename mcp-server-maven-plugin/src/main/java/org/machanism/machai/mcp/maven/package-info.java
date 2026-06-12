/**
 * Provides Maven plugin Mojos and utilities for managing MCP server lifecycle and configuration.
 * <p>
 * This package contains abstract and concrete Mojo implementations for starting and configuring
 * MCP servers in Maven builds, including stateless and streamable server variants. It also
 * includes shared configuration logic for environment variables and credentials.
 * </p>
 *
 * <ul>
 *   <li>{@link org.machanism.machai.mcp.maven.AbstractMCPServerMojo} - Base class for MCP server Mojos</li>
 *   <li>{@link org.machanism.machai.mcp.maven.HttpStatelessServerMojo} - Mojo for stateless MCP server</li>
 *   <li>{@link org.machanism.machai.mcp.maven.HttpStreamableMcpServerMojo} - Mojo for streamable MCP server</li>
 * </ul>
 */
package org.machanism.machai.mcp.maven;