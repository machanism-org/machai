package org.machanism.machai.gw.processor;

import java.io.IOException;

public class ActNotFound extends IOException {

	private static final long serialVersionUID = 1L;

	// Sonar java:S1165 - exception state is immutable after construction.
	private final String name;

	public ActNotFound(String name) {
		super("Act: `" + name + "` not found.");
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
