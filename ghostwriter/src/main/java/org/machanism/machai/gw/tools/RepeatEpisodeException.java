package org.machanism.machai.gw.tools;

/**
 * Exception used to request that the current act episode be executed again.
 */
public class RepeatEpisodeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a repeat request for the current episode.
	 */
	public RepeatEpisodeException() {
		super("Repeat current episode requested.");
	}
}
