package org.machanism.machai.ai.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads and evaluates command deny-list rules for use by host-side command execution tools.
 *
 * <p>
 * The checker reads one or more rule resources and evaluates an input command line against those rules. Each
 * non-empty, non-comment line of a deny-list file must use one of the following formats:
 * </p>
 * <ul>
 * <li>{@code REGEX:...} – a Java regular expression; a match anywhere in the command is considered dangerous</li>
 * <li>{@code KEYWORD:...} – a case-insensitive substring match</li>
 * </ul>
 *
 * <p>
 * This class provides a best-effort heuristic check. It should be used in addition to an allow-list and other host
 * security controls.
 * </p>
 */
public class CommandSecurityChecker {
	private static final Logger logger = LoggerFactory.getLogger(CommandSecurityChecker.class);

	private final List<Pattern> denyPatterns = new ArrayList<>();
	private final List<String> denyKeywords = new ArrayList<>();

	/**
	 * Creates a new checker and loads deny-list rules from an operating-system specific classpath resource.
	 *
	 * <p>
	 * The following resources are expected to exist on the classpath:
	 * </p>
	 * <ul>
	 * <li>{@code denylist/windows.txt} when running on Windows</li>
	 * <li>{@code denylist/unix.txt} when running on a Unix-like OS</li>
	 * </ul>
	 *
	 * @throws IOException              if the selected resource cannot be found or read
	 * @throws IllegalArgumentException if no deny-list is defined for the current operating system
	 */
	public CommandSecurityChecker() throws IOException {
		String resourcePath;

		if (SystemUtils.IS_OS_WINDOWS) {
			resourcePath = "denylist/windows.txt";
		} else if (SystemUtils.IS_OS_UNIX) {
			resourcePath = "denylist/unix.txt";
		} else {
			throw new IllegalArgumentException(
					"No denylist defined for operating system: `" + SystemUtils.OS_NAME + "`.");
		}

		logger.info("Loading denylist for OS `{}` from resource: {}", SystemUtils.OS_NAME, resourcePath);

		ClassLoader classLoader = getClass().getClassLoader();
		try (InputStream is = classLoader.getResourceAsStream(resourcePath)) {
			if (is == null) {
				throw new IOException("Resource not found: " + resourcePath);
			}
			loadRules(is);
		}
	}

	/**
	 * Loads deny-list rules from the provided stream.
	 *
	 * <p>
	 * This method is intended for internal initialization.
	 * </p>
	 *
	 * @param is input stream containing rule definitions
	 * @throws IOException if the stream cannot be read
	 */
	private void loadRules(InputStream is) throws IOException {
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

	/**
	 * Loads additional deny-list rules from a file.
	 *
	 * <p>
	 * This method can be used by a host application to extend the built-in deny-list at runtime.
	 * </p>
	 *
	 * @param denylistFile file containing rule definitions
	 * @throws IOException if the file cannot be read
	 */
	public void loadDenylist(File denylistFile) throws IOException {
		loadRules(new FileInputStream(denylistFile));
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
