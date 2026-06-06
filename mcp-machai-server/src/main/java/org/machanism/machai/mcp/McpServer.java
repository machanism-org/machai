package org.machanism.machai.mcp;

import java.util.Objects;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.help.HelpFormatter;
import org.slf4j.LoggerFactory;

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
 * @since 1.1.15
 * @author Viktor Tovstyi
 */
public class McpServer {

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
		options.addOption(new Option("n", "name", true, "Specify the MCP server name."));
		options.addOption(new Option("v", "version", true, "Specify the MCP server version."));
		options.addOption(
				Option.builder()
						.option("p")
						.longOpt("port")
						.hasArg()
						.desc("Specify the port number for the MCP server to listen on. This is required when running as a Remote MCP Server.")
						.type(Integer.class)
						.get());

		CommandLine cmd = new DefaultParser().parse(options, args);
		if (cmd.hasOption('h')) {
			HelpFormatter.builder().setShowSince(false).get().printOptions(options);
			return;
		}

		String name = cmd.getOptionValue('n', "mcp-machai-server");
		String version = cmd.getOptionValue('v',
				Objects.toString(StdioMcpServer.class.getPackage().getImplementationVersion(), "latest"));

		if (cmd.hasOption("p")) {
			setConsoleOutputAtRuntime();

			HttpStatelessMcpServer mcpServer = new HttpStatelessMcpServer(name, version);
			mcpServer.tools();
			mcpServer.build();

			Integer port = cmd.getParsedOptionValue("p");
			mcpServer.start(port);

		} else {
			StdioMcpServer mcpServer = new StdioMcpServer(name, version);
			mcpServer.tools();
			mcpServer.build();
		}
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