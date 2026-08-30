package org.machanism.machai.gw.tools;

/**
 * Runtime control-flow exception that requests repetition of the current Act
 * episode.
 *
 * <p>Processors catch this intentional signal and restart the current episode
 * while preserving the applicable workflow context.</p>
 *
 * @author Viktor Tovstyi
 */
public class RepeatEpisodeException extends RuntimeException {
	/** Serialization version for this control-flow exception. */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a repeat request for the current episode.
	 */
	public RepeatEpisodeException() {
		super("Repeat current episode requested.");
	}
}
