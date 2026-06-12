package org.machanism.machai.mcp.server;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.help.HelpFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;

/**
 * Entry point for starting the MCP (Model Context Protocol) server.
 * <p>
 * Supports both STDIO and Remote (network) server modes, configurable via
 * command-line options.
 * </p>
 * <ul>
 * <li><b>STDIO mode</b>: Default, starts a server that communicates via
 * standard input/output.</li>
 * <li><b>Remote mode</b>: If the <code>-p</code> or <code>--port</code> option
 * is specified, starts a server that listens on the given port.</li>
 * </ul>
 * <p>
 * Command-line options:
 * <ul>
 * <li><code>-h</code>, <code>--help</code>: Show help message and exit.</li>
 * <li><code>-n</code>, <code>--name</code>: Specify the MCP server name
 * (default: mcp-machai-server).</li>
 * <li><code>-v</code>, <code>--version</code>: Specify the MCP server version
 * (default: implementation version or "latest").</li>
 * <li><code>-p</code>, <code>--port</code>: Specify the port number for Remote
 * MCP Server mode.</li>
 * </ul>
 * </p>
 * 
 * @since 1.2.0
 * @author Viktor Tovstyi
 */
public class McpServer {

	private static final Logger log = LoggerFactory.getLogger(McpServer.class);

	/**
	 * Main entry point for the MCP server application.
	 * <p>
	 * Parses command-line arguments to determine server mode and configuration,
	 * then starts the appropriate server.
	 * </p>
	 *
	 * @param args command-line arguments
	 * @throws Exception if an error occurs during server startup
	 */
	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.addOption(new Option("h", "help", false, "Show this help message and exit."));
		options.addOption(new Option("d", "projectDir", true, "Specify the project directory path."));
		options.addOption(new Option("n", "name", true, "Specify the MCP server name."));
		options.addOption(new Option("v", "version", true, "Specify the MCP server version."));
		options.addOption(
				new Option("s", "session", false, "Use streamable MCP server mode (only for Http MCP Server)."));
		options.addOption(
				Option.builder()
						.option("p")
						.longOpt("port")
						.hasArg()
						.desc("Specify the port number for the MCP server to listen on. This is required when running as a Http MCP Server.")
						.type(Integer.class)
						.get());

		CommandLine cmd = new DefaultParser().parse(options, args);
		if (cmd.hasOption('h')) {
			HelpFormatter.builder().setShowSince(false).get().printOptions(options);
		}

		File projectDir = null;
		if (cmd.hasOption("d")) {
			projectDir = new File(cmd.getOptionValue('d'));
		}

		String name = cmd.getOptionValue('n', "mcp-machai-server");
		String version = cmd.getOptionValue('v',
				Objects.toString(StdioMcpServer.class.getPackage().getImplementationVersion(), "latest"));

		AbstractMcpServer mcpServer;
		if (cmd.hasOption("p")) {
			setConsoleOutputAtRuntime();

			if (projectDir != null) {
				log.info("Project dir: {}", projectDir);
			} else {
				log.warn("Project directory is not set. It will be determined from the client request.");
			}

			AbstractHttpMcpServer mcpHttpServer;
			if (cmd.hasOption("s")) {
				mcpHttpServer = new HttpStreamableMcpServer(name, version);
			} else {
				mcpHttpServer = new HttpStatelessMcpServer(name, version);
			}

			Integer port = cmd.getParsedOptionValue("p");
			mcpHttpServer.setPort(port);
			mcpServer = mcpHttpServer;

		} else {
			mcpServer = new StdioMcpServer(name, version);
		}

		mcpServer.prompts(List.of("site", "sonar-fix"));

		mcpServer.setProjectDir(projectDir);
		mcpServer.tools();

		mcpServer.start();
	}

	public static void setConsoleOutputAtRuntime() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(context);
		encoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
		encoder.start();

		ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
		consoleAppender.setContext(context);
		consoleAppender.setEncoder(encoder);
		consoleAppender.start();

		context.getLogger("ROOT").addAppender(consoleAppender);
	}
}