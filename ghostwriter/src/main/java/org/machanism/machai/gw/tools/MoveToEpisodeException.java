package org.machanism.machai.gw.tools;

import org.machanism.machai.ai.tools.SpecialException;

/**
 * Exception used to request a jump to another act episode during execution.
 *
 * @author Viktor Tovstyi
 */
public class MoveToEpisodeException extends SpecialException {
	/** Serialization version for this control-flow exception. */
	private static final long serialVersionUID = 1L;
	/** Optional one-based identifier of the requested target episode. */
	private final Integer episodeId;
	/** Optional name of the requested target episode. */
	private final String episodeName;

	/**
	 * Creates a move request for an episode id or name.
	 *
	 * @param episodeId target 1-based episode id, or {@code null}
	 * @param name      target episode name, or {@code null}
	 */
	public MoveToEpisodeException(Integer episodeId, String name) {
		super(episodeId == null ? "Move to next episode" : "Move to episode: " + episodeId);
		this.episodeId = episodeId;
		this.episodeName = name;
	}

	/**
	 * Returns the requested target episode id.
	 *
	 * @return 1-based episode id, or {@code null}
	 */
	public Integer getEpisodeId() {
		return episodeId;
	}

	/**
	 * Returns the requested target episode name.
	 *
	 * @return target episode name, or {@code null}
	 */
	public String getName() {
		return episodeName;
	}
}
