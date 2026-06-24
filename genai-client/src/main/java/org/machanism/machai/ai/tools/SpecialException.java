package org.machanism.machai.ai.tools;

/**
 * Exception used to signal the end of a task without terminating the
 * application.
 * <p>
 * This exception is typically thrown by function tools to gracefully conclude
 * an interactive session or user-driven task, allowing the application to
 * continue running.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
public class SpecialException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new {@code EndTaskException} with the specified detail message.
	 *
	 * @param message the detail message describing the reason for ending the task
	 */
	public SpecialException(String message) {
		super(message);
	}

	public SpecialException(Exception e) {
		super(e);
	}
}