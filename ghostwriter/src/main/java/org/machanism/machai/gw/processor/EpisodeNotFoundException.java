package org.machanism.machai.gw.processor;

public class EpisodeNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EpisodeNotFoundException(String episodeName) {
		super(episodeName);
	}

}
