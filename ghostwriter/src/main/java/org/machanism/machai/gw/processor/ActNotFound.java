package org.machanism.machai.gw.processor;

import java.io.IOException;

public class ActNotFound extends IOException {

	private static final long serialVersionUID = 1L;

	private String name;

	public ActNotFound(String name) {
		super("Act: `" + name + "` not found.");
		this.setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
