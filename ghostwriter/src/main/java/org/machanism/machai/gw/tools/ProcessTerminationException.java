package org.machanism.machai.gw.tools;

/**
 * Runtime exception used by {@code terminate_process} to signal early
 * termination to the host.
 */
public class ProcessTerminationException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final int exitCode;

	/**
	 * Creates a termination exception.
	 *
	 * @param message  message to expose to the host
	 * @param exitCode desired process exit code
	 */
	public ProcessTerminationException(String message, int exitCode) {
		super(message);
		this.exitCode = exitCode;
	}

	/**
	 * Creates a termination exception.
	 *
	 * @param message  message to expose to the host
	 * @param cause    underlying cause
	 * @param exitCode desired process exit code
	 */
	public ProcessTerminationException(String message, Throwable cause, int exitCode) {
		super(message, cause);
		this.exitCode = exitCode;
	}

	public ProcessTerminationException(int exitCode) {
		this.exitCode = exitCode;
	}

	/**
	 * Returns the desired exit code.
	 *
	 * @return exit code
	 */
	public int getExitCode() {
		return exitCode;
	}
}
