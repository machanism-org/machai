package org.machanism.machai.ai.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CommandSecurityChecker {

	private final List<Pattern> denyPatterns = new ArrayList<>();
	private final List<String> denyKeywords = new ArrayList<>();

	/**
	 * Loads denylist rules from one or more files.
	 * 
	 * @param filePaths Paths to the denylist files (Windows, Unix, etc.).
	 * @throws IOException If any file cannot be read.
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
	                        if (line.isEmpty() || line.startsWith("#"))
	                            continue;
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
	 * Checks if the given command is dangerous and should be blocked.
	 * 
	 * @param command The shell command to check.
	 * @return true if the command matches the denylist, false otherwise.
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