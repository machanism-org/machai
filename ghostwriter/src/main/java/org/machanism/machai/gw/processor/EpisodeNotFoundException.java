package org.machanism.machai.gw.processor;

/**
 * Exception thrown when an episode cannot be resolved by name.
 */
public class EpisodeNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates an exception for the unresolved episode name.
	 *
	 * @param episodeName unresolved episode name
	 */
	public EpisodeNotFoundException(String episodeName) {
		super(episodeName);
	}

}
