package org.machanism.machai.gw.tools;

public class MoveToEpisodeException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final Integer episodeId;

	public MoveToEpisodeException(Integer episodeId) {
		super(episodeId == null ? "Move to next episode" : "Move to episode: " + episodeId);
		this.episodeId = episodeId;
	}

	public Integer getEpisodeId() {
		return episodeId;
	}
}