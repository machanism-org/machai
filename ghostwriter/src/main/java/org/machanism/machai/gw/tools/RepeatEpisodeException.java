package org.machanism.machai.gw.tools;

public class RepeatEpisodeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public RepeatEpisodeException() {
		super("Repeat current episode requested.");
	}
}
