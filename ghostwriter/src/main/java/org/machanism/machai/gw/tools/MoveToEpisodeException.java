package org.machanism.machai.gw.tools;

public class MoveToEpisodeException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final String episodeId;

	public MoveToEpisodeException(String episodeId) {
		super(episodeId == null ? "Move to next episode" : "Move to episode: " + episodeId);
		this.episodeId = episodeId;
	}

	public String getEpisodeId() {
		return episodeId;
	}
}