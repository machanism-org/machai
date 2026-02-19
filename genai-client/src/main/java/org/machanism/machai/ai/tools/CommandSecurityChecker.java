package org.machanism.machai.ai.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Loads and evaluates command deny-list rules for use by host-side command execution tools.
 *
 * <p>
 * The checker reads one or more classpath resources containing rule definitions and then evaluates input
 * commands against those rules. Each non-empty, non-comment line of the resource file must be one of:
 * </p>
 * <ul>
 *   <li>{@code REGEX:...} - a Java regular expression; a match anywhere in the command is considered dangerous</li>
 *   <li>{@code KEYWORD:...} - a case-insensitive substring match</li>
 * </ul>
 *
 * <p>
 * This class performs a best-effort heuristic check and should be used in addition to an allow-list and other
 * host security controls.
 * </p>
 */
public class CommandSecurityChecker {

	private final List<Pattern> denyPatterns = new ArrayList<>();
	private final List<String> denyKeywords = new ArrayList<>();

	/**
	 * Creates a new checker and loads deny-list rules from one or more classpath resources.
	 *
	 * @param resourcePaths classpath resource names containing deny-list rules (for example,
	 *                      {@code denylist-windows.txt} and {@code denylist-unix.txt})
	 * @throws IOException if any resource cannot be found or read
	 */
	public CommandSecurityChecker(String... resourcePaths) throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		for (String resourcePath : resourcePaths) {
			try (InputStream is = classLoader.getResourceAsStream(resourcePath)) {
				if (is == null) {
					throw new IOException("Resource not found: " + resourcePath);
				}
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
					String line;
					while ((line = reader.readLine()) != null) {
						line = line.trim();
						if (line.isEmpty() || line.startsWith("#")) {
							continue;
						}
						if (line.startsWith("REGEX:")) {
							String regex = line.substring(6).trim();
							denyPatterns.add(Pattern.compile(regex));
						} else if (line.startsWith("KEYWORD:")) {
							String keyword = line.substring(8).trim();
							denyKeywords.add(keyword);
						}
					}
				}
			}
		}
	}

	/**
	 * Determines whether the supplied command matches any deny-list rule.
	 *
	 * @param command shell command to check
	 * @return {@code true} if the command matches a deny-list rule; {@code false} otherwise
	 */
	public boolean isDangerous(String command) {
		if (command == null || command.trim().isEmpty()) {
			return false;
		}

		for (Pattern pattern : denyPatterns) {
			if (pattern.matcher(command).find()) {
				return true;
			}
		}

		String lowerCmd = command.toLowerCase();
		for (String keyword : denyKeywords) {
			if (lowerCmd.contains(keyword.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

}
