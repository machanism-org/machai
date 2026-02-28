package org.machanism.machai.ai.tools;

/**
 * Exception thrown when a command fails a deny-list security check.
 *
 * <p>
 * This exception is used by {@link CommandSecurityChecker} to signal that the
 * provided command line matches a deny-list rule (keyword or regular
 * expression). Host-side command execution tools can catch this exception and
 * refuse to run the command.
 * </p>
 */
public class DenyException extends Exception {

	private static final long serialVersionUID = 3508879826779125812L;

	/**
	 * Creates a new exception with a human-readable reason.
	 *
	 * @param message description of the deny-list rule that matched
	 */
	public DenyException(String message) {
		super(message);
	}

}
