package org.machanism.machai.ai.tools;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads and evaluates command deny-list rules used by host-side command execution tools.
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
 * This class provides a best-effort heuristic check. It should be used in addition to an allow-list and other
 * host security controls.
 * </p>
 */
public class CommandSecurityChecker {
	/**
	 * Configuration property allowing the host to inject/extend the deny-list.
	 */
	private static final String DENYLIST_PROP_NAME = "ft.command.denylist";

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
	 * <p>
	 * In addition, the host may provide {@link #DENYLIST_PROP_NAME} to extend or override the default deny-list.
	 * </p>
	 *
	 * @param configurator configurator used to optionally extend the deny-list
	 * @throws IOException              if the selected resource cannot be found or read
	 * @throws IllegalArgumentException if no deny-list is defined for the current operating system
	 */
	public CommandSecurityChecker(Configurator configurator) throws IOException {
		String resourcePath;

		if (SystemUtils.IS_OS_WINDOWS) {
			resourcePath = "denylist/windows.txt";
		} else if (SystemUtils.IS_OS_UNIX) {
			resourcePath = "denylist/unix.txt";
		} else {
			throw new IllegalArgumentException(
					"No denylist defined for operating system: `" + SystemUtils.OS_NAME + "`." );
		}

		logger.debug("Loading denylist for OS `{}` from resource: {}", SystemUtils.OS_NAME, resourcePath);

		ClassLoader classLoader = getClass().getClassLoader();
		URL systemResource = classLoader.getResource(resourcePath);
		String denylist = IOUtils.toString(systemResource, StandardCharsets.UTF_8);

		String denylistValue = configurator.get(DENYLIST_PROP_NAME, null);
		if (denylistValue != null) {
			denylist = String.format(denylistValue, denylist);
		}
		loadRules(denylist);
	}

	/**
	 * Loads deny-list rules from the provided string.
	 *
	 * <p>
	 * This method is intended for internal initialization.
	 * </p>
	 *
	 * @param rulesString string containing rule definitions, separated by line breaks
	 */
	private void loadRules(String rulesString) {
		if (rulesString == null || rulesString.isEmpty()) {
			logger.warn("Rules string is empty or null.");
			return;
		}
		String[] lines = rulesString.split("\\r?\\n");
		for (String line : lines) {
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

	/**
	 * Checks whether the supplied command matches any deny-list rule.
	 *
	 * <p>
	 * If the command matches a rule, a {@link DenyException} is thrown containing a message identifying the matched
	 * rule.
	 * </p>
	 *
	 * @param command shell command to check
	 * @throws DenyException if the command matches a deny-list rule
	 */
	public void denyCheck(String command) throws DenyException {
		for (Pattern pattern : denyPatterns) {
			if (pattern.matcher(command).find()) {
				throw new DenyException("Pattern: " + pattern.pattern());
			}
		}

		String lowerCmd = command.toLowerCase();
		for (String keyword : denyKeywords) {
			if (lowerCmd.contains(keyword.toLowerCase())) {
				throw new DenyException("Keyword: " + keyword);
			}
		}
	}

}
