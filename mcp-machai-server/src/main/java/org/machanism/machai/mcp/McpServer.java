package org.machanism.machai.mcp;

import java.util.Objects;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.help.HelpFormatter;

public class McpServer {

	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.addOption(new Option("h", "help", false, "Show this help message and exit."));
		options.addOption(new Option("n", "name", true, "Specify the MCP server name."));
		options.addOption(new Option("v", "version", true, "Specify the MCP server version."));
		options.addOption(
				Option.builder().option("p").longOpt("port").hasArg().desc("Specify the MCP listened port number.")
						.type(Integer.class).get());

		CommandLine cmd = new DefaultParser().parse(options, args);
		if (cmd.hasOption('h')) {
			HelpFormatter.builder().setShowSince(false).get().printOptions(options);
			return;
		}

		String name = cmd.getOptionValue('n', "mcp-machai-server");
		String version = cmd.getOptionValue('v',
				Objects.toString(StdioMcpServer.class.getPackage().getImplementationVersion(), "latest"));

		if (cmd.hasOption("p")) {
			RemoteMcpServer mcpServer = new RemoteMcpServer(name, version);
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

}
