package org.machanism.machai.gw.tools;

/**
 * Runtime control-flow exception that requests host application termination.
 *
 * <p>The host should use {@link #getExitCode()} when completing termination.
 * This exception represents an intentional workflow transition rather than an
 * ordinary application failure.</p>
 *
 * @author Viktor Tovstyi
 */
public class ProcessTerminationException extends RuntimeException {
	/** Serialization version for this control-flow exception. */
	private static final long serialVersionUID = 1L;
	/** Exit code that the host should use when terminating execution. */
	private final int exitCode;

	/**
	 * Creates a termination request with a detail message and exit code.
	 *
	 * @param message message to expose to the host
	 * @param exitCode desired process exit code
	 */
	public ProcessTerminationException(String message, int exitCode) {
		super(message);
		this.exitCode = exitCode;
	}

	/**
	 * Creates a termination request with a detail message, cause, and exit code.
	 *
	 * @param message message to expose to the host
	 * @param cause underlying cause
	 * @param exitCode desired process exit code
	 */
	public ProcessTerminationException(String message, Throwable cause, int exitCode) {
		super(message, cause);
		this.exitCode = exitCode;
	}

	/**
	 * Creates a termination request with an exit code and no detail message.
	 *
	 * @param exitCode desired process exit code
	 */
	public ProcessTerminationException(int exitCode) {
		this.exitCode = exitCode;
	}

	/**
	 * Returns the exit code the host should use for termination.
	 *
	 * @return desired process exit code
	 */
	public int getExitCode() {
		return exitCode;
	}
}
