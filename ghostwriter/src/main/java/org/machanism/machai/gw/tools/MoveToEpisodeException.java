package org.machanism.machai.gw.tools;

/**
 * Exception used to request a jump to another act episode during execution.
 */
public class MoveToEpisodeException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final Integer episodeId;
	private String name;

	/**
	 * Creates a move request for an episode id or name.
	 *
	 * @param episodeId target 1-based episode id, or {@code null}
	 * @param name      target episode name, or {@code null}
	 */
	public MoveToEpisodeException(Integer episodeId, String name) {
		super(episodeId == null ? "Move to next episode" : "Move to episode: " + episodeId);
		this.episodeId = episodeId;
		this.name = name;
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
		return name;
	}
}
