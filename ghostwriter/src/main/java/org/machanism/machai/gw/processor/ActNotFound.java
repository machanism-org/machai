package org.machanism.machai.gw.processor;

import java.io.IOException;

/**
 * Exception thrown when a requested act definition cannot be found.
 */
public class ActNotFound extends IOException {

	private static final long serialVersionUID = 1L;

	private final String name;

	/**
	 * Creates an exception for the missing act name.
	 *
	 * @param name missing act name
	 */
	public ActNotFound(String name) {
		super("Act: `" + name + "` not found.");
		this.name = name;
	}

	/**
	 * Returns the missing act name.
	 *
	 * @return missing act name
	 */
	public String getName() {
		return name;
	}

}
