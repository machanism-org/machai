package org.machanism.machai.gw.tools;

public class MoveToEpisodeException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final Integer episodeId;
	private String name;

	public MoveToEpisodeException(Integer episodeId, String name) {
		super(episodeId == null ? "Move to next episode" : "Move to episode: " + episodeId);
		this.episodeId = episodeId;
		this.name = name;
	}

	public Integer getEpisodeId() {
		return episodeId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}