package org.machanism.machai.cli;

import java.util.Scanner;

import org.apache.commons.lang.StringUtils;

public class Command {

	/**
	 * Continuation marker used by {@link #readText(String)} to allow users to enter
	 * multi-line values via standard input.
	 */
	public static final String MULTIPLE_LINES_BREAKER = "\\";

	/**
	 * Reads multi-line text from stdin until EOF is reached.
	 *
	 * @param prompt message to show before reading input
	 * @return the entered text, or {@code null} if no content was provided
	 */
	public String readText(String prompt) {
		System.out.print(prompt + ": ");

		StringBuilder sb = new StringBuilder();
		try (Scanner scanner = new Scanner(System.in)) {
			while (scanner.hasNextLine()) {
				String nextLine = scanner.nextLine();
				if (StringUtils.endsWith(nextLine, MULTIPLE_LINES_BREAKER)) {
					sb.append(StringUtils.substringBeforeLast(nextLine, MULTIPLE_LINES_BREAKER)).append("\n");
					System.out.print("\t");
				} else {
					sb.append(nextLine);
					break;
				}
			}
		}

		return sb.toString();

	}

}
